// First objective function
#include <iostream>
#include <cstdio>
#include <cstdlib>
#include <ctime>
#include <cmath>
#include <fstream>
#include <float.h>

using namespace std;

#define NUM_BILL 12605
#define NUM_TERM 13664
#define NUM_TOPIC 5
#define NUM_ITERATION 1000
#define NUM_BILL_TERM 746761		// num of (non-zero) bill-term pairs ("./bill_term.txt")
#define LAMBDA_B 0
#define INIT_TRIALS 10

#define OUTPUTPATH_ZW "./res/plsa_zw.txt"
#define OUTPUTPATH_ZD "./res/plsa_dz.txt"

double **dmatrix(int row, int col)
{
	double **matrix = (double**)calloc(row, sizeof(double*));
	if (matrix == NULL)
		return NULL;
	for (int i = 0; i < row; i++) {
		matrix[i] = (double*)calloc(col, sizeof(double));
		if (matrix[i] == NULL)
			return NULL;
	}
	return matrix;
}

void free_dmatrix(double** mat, int row)
{
	for (int i = 0; i < row; i++)
		free(mat[i]);
	free(mat);
}

double calcLikelihood(double **p_z_d, double **p_w_z, int **n_dw, double lambda_B)
{
	double likelihood = 0;
	for (int d = 0; d < NUM_BILL; d++) {
		for (int i = 0; i < n_dw[d][0]; i++) {
			int w = n_dw[d][1+i];
			int freq = n_dw[d][1+i+n_dw[d][0]];

			double sumz = 0;
			for (int z = 0; z < NUM_TOPIC; z++)
				sumz += p_w_z[w][z] * p_z_d[z][d];
			double logV = sumz + DBL_MIN;

			if (logV != 0)
				likelihood += (double)freq * log(logV);
		}
	}
	return likelihood;
}

// n_dw: word frequency in each document
// p_B: word freq in background model
double plsa(double **p_z_d, double **p_w_z, int **n_dw, int iterations, double lambda_B)
{
	double *norm_z_d = (double*)calloc(NUM_BILL, sizeof(double));
	double *norm_w_z = (double*)calloc(NUM_TOPIC, sizeof(double));
	double *new_norm_z_d = (double*)calloc(NUM_BILL, sizeof(double));
	double *new_norm_w_z = (double*)calloc(NUM_TOPIC, sizeof(double));

	double *p_z_dw = (double*)calloc(NUM_TOPIC, sizeof(double));
	double norm_z_dw;
	int gyp;

	double **tmp_pzd = dmatrix(NUM_TOPIC, NUM_BILL);
	double **tmp_pwz = dmatrix(NUM_TERM, NUM_TOPIC);

	double like[2] = {-1,-1};			// likelihood
	int inctimes = 0;

	for (int iter = 0; iter < iterations; iter++) {
		cout << "\tIter " << iter;
	// S1	/// Initialization
		for (int d = 0; d < NUM_BILL; d++) {
			norm_z_d[d] = 0;				// sum_{z}{p(z|d)}
			new_norm_z_d[d] = 0;
			for (int z = 0; z < NUM_TOPIC; z++)
				norm_z_d[d] += p_z_d[z][d];
		}
		for (int z = 0; z < NUM_TOPIC; z++) {
			norm_w_z[z] = 0;				// sum_{w}{p(w|z)}
			new_norm_w_z[z] = 0;
			for (int w = 0; w < NUM_TERM; w++)
				norm_w_z[z] += p_w_z[w][z];
		}

		// Normalization
		for (int d = 0; d < NUM_BILL; d++) if (norm_z_d[d] != 0) {
			for (int z = 0; z < NUM_TOPIC; z++)
				p_z_d[z][d] /= norm_z_d[d];
		}
		for (int z = 0; z < NUM_TOPIC; z++) if (norm_w_z[z] != 0) {
			for (int w = 0; w < NUM_TERM; w++)
				p_w_z[w][z] /= norm_w_z[z];
		}

		// make tmp theta\beta zero
		for (int d = 0; d < NUM_BILL; d++)
			for (int z = 0; z < NUM_TOPIC; z++)
				tmp_pzd[z][d] = 0;
		for (int z = 0; z < NUM_TOPIC; z++)
			for (int w = 0; w < NUM_TERM; w++)
				tmp_pwz[w][z] = 0;

	// S2	/// Step1: update p(w|z)
		for (int d = 0; d < NUM_BILL; d++) {
			for (int i = 0; i < n_dw[d][0]; i++) {
				int w = n_dw[d][1+i];
				int freq = n_dw[d][1+i+n_dw[d][0]];

				norm_z_dw = 0;
				for (int z = 0; z < NUM_TOPIC; z++) {
					p_z_dw[z] = p_w_z[w][z] * p_z_d[z][d];		// p(w|z)*p(z|d)
					norm_z_dw += p_z_dw[z];
				}
				if (norm_z_dw != 0) {					// sum_{z}{p(w|z)*p(z|d)}
					for (int z = 0; z < NUM_TOPIC; z++) {
						p_z_dw[z] /= norm_z_dw;			// p(z|d,w) (normalized)
						double inc = (double)freq * p_z_dw[z];	// n(d,w)*p(z|d,w)

						// p(z|d): original plsa
						tmp_pzd[z][d] += inc;			// update p(z|d) (un-normalized)
						new_norm_z_d[d] += inc;			// update sum_{z}{p(z|d)}

						// p(w|z): NOT using background model
						tmp_pwz[w][z] += inc;
						new_norm_w_z[z] += inc;
					}
				}
				else printf("sum{z}{p(w|z)p(z|d)} = 0!\n");
			}
		}

	// S3	/// update and normalize p(z|d)
		for (int z = 0; z < NUM_TOPIC; z++)
			for (int d = 0; d < NUM_BILL; d++)
				p_z_d[z][d] = tmp_pzd[z][d];
		for (int d = 0; d < NUM_BILL; d++)
			norm_z_d[d] = new_norm_z_d[d];

		for (int w = 0; w < NUM_TERM; w++)
			for (int z = 0; z < NUM_TOPIC; z++)
				p_w_z[w][z] = tmp_pwz[w][z];
		for (int z = 0; z < NUM_TOPIC; z++)
			norm_w_z[z] = new_norm_w_z[z];

		for (int d = 0; d < NUM_BILL; d++) if (norm_z_d[d] != 0) {
			for (int z = 0; z < NUM_TOPIC; z++) 
				p_z_d[z][d] /= norm_z_d[d];
		}
		for (int z = 0; z < NUM_TOPIC; z++) if (norm_w_z[z] != 0) {
			for (int w = 0; w < NUM_TERM; w++) 
				p_w_z[w][z] /= norm_w_z[z];
		}

		// calc likelihood
		if (iter%1 == 0) {
			like[1] = calcLikelihood(p_z_d, p_w_z, n_dw, lambda_B);
			double rate = -(like[1]-like[0])/like[0];
			cout << " old likelihood = " << like[0] << " newlikelihood = " << like[1];
			cout << " rate = " << rate << endl;
			if (fabs(rate) < pow(10,-6))
				break;
			like[0] = like[1];
		}
	}

	free(norm_z_d);
	free(norm_w_z);
	free(new_norm_z_d);
	free(new_norm_w_z);
	free(p_z_dw);
	free_dmatrix(tmp_pzd, NUM_TOPIC);
	free_dmatrix(tmp_pwz, NUM_TERM);

	return like[1];
}

int main()
{
	/// Initialization
	srand(0);
//	srand((unsigned)time(NULL));
	cout << "Program starts..." << endl;
	int gyp;

	double lambda_B = LAMBDA_B;

	/*
	int perc = 1;
	int *train_d = (int*)calloc(NUM_BILL - NUM_BILL/perc, sizeof(int));
	int *test_d = (int*)calloc(NUM_BILL/perc, sizeof(int));
	// select training dataset randomly
	int train_d_index = 0, test_d_index = 0;
	for (int i = 0; i < NUM_BILL/perc; i++) {
		int test_j = rand() % perc;
		for (int j = 0; j < 10; j++) {
			if (j != test_j) {
				train_d[train_d_index] = i*10+j;
				train_d_index++;
			} else {
				test_d[test_d_index] = i*10+j;
				test_d_index++;
			}
		}
	}
	for (int j = 0; j < NUM_BILL - NUM_BILL/10 * 10; j++) {
		train_d[train_d_index] = (NUM_BILL/10)*10 + j;
		train_d_index++;
	}
	*/
	
	/*
	FILE *fp_out_train = fopen("../Result/tf_ipm/predict/d_train", "w");
	for (int i = 0; i < NUM_BILL - NUM_BILL/10; i++)
		fprintf(fp_out_train, "%d\n", train_d[i]);
	FILE *fp_out_test = fopen("../Result/tf_ipm/predict/d_test", "w");
	for (int i = 0; i < NUM_BILL/10; i++)
		fprintf(fp_out_test, "%d\n", test_d[i]);
	fclose(fp_out_train);
	fclose(fp_out_test);
	*/


	int **n_dw = (int**)calloc(NUM_BILL, sizeof(int*));

	FILE *fin = fopen("./bill_term.txt", "r");
	int *bill_word_len = (int*)calloc(NUM_BILL, sizeof(int));
	int billID, wordID, freq;
	while (!feof(fin)) {
		fscanf(fin, "%d\t%d\t%d\n", &billID, &wordID, &freq);
		bill_word_len[billID] += 1;
	}
	fclose(fin);
	fin = fopen("./bill_term.txt", "r");
	for (int d = 0; d < NUM_BILL; d++) {
		int length = bill_word_len[d];
		n_dw[d] = (int*)calloc(2*length+1, sizeof(int));
		n_dw[d][0] = length;
		for (int w = 0; w < length; w++) {
			fscanf(fin, "%d\t%d\t%d\n", &billID, &wordID, &freq);
			n_dw[d][1+w] = wordID;
			n_dw[d][1+w+length] = freq;
			if (billID != d) {
				cout << "wrong index " << d << endl;
				cin >> gyp;
			}
		}
	}
	fclose(fin);

	
	int testNum = INIT_TRIALS;
	double ***p_z_d = (double***)calloc(testNum, sizeof(double**));
	for (int k = 0; k < testNum; k++)
		p_z_d[k] = dmatrix(NUM_TOPIC, NUM_BILL);
	double ***p_w_z = (double***)calloc(testNum, sizeof(double**));
	for (int k = 0; k < testNum; k++)
		p_w_z[k] = dmatrix(NUM_TERM, NUM_TOPIC);
	
	for (int k = 0; k < testNum; k++)
		for (int z = 0; z < NUM_TOPIC; z++)
			for (int d = 0; d < NUM_BILL; d++)
				p_z_d[k][z][d] = (rand()+1)/(double)RAND_MAX;
	for (int k = 0; k < testNum; k++)
		for (int w = 0; w < NUM_TERM; w++)
			for (int z = 0; z < NUM_TOPIC; z++)
				p_w_z[k][w][z] = (rand()+1)/(double)RAND_MAX;

	// plsa
	int maxK = 0; double maxV = -DBL_MAX;
	for (int k = 0; k < testNum; k++) {
		cout << "--- " << k << " ---" << endl;
		double v = plsa(p_z_d[k], p_w_z[k], n_dw, 50, lambda_B);
		if (v > maxV) {
			maxV = v;
			maxK = k;
			cout << "maxK becomes " << maxK << endl;
		}
	}
	cout << "maxK = " << maxK << endl;
	plsa(p_z_d[maxK], p_w_z[maxK], n_dw, NUM_ITERATION, lambda_B);

	/// Output
	double *norm_z_d = (double*)calloc(NUM_BILL, sizeof(double));
	double *norm_w_z = (double*)calloc(NUM_TOPIC, sizeof(double));
	for (int d = 0; d < NUM_BILL; d++) {
		for (int z = 0; z < NUM_TOPIC; z++)
			norm_z_d[d] += p_z_d[maxK][z][d];
	}
	for (int z = 0; z < NUM_TOPIC; z++) {
		for (int w = 0; w < NUM_TERM; w++)
			norm_w_z[z] += p_w_z[maxK][w][z];
	}

	FILE *fout = fopen(OUTPUTPATH_ZD, "w");
	cout << "p(z|d)" << endl;
	for (int d = 0; d < NUM_BILL; d++) if (norm_z_d[d] != 0) {
		for (int z = 0; z < NUM_TOPIC; z++) {
			fprintf(fout, "%.12lf", p_z_d[maxK][z][d]/norm_z_d[d]);
			if (z != NUM_TOPIC-1)
				fprintf(fout, "%s", "\t");
		}
		fprintf(fout, "%s", "\n");
	}
	fclose(fout);

	fout = fopen(OUTPUTPATH_ZW, "w");
	cout << "p(w|z)" << endl;
	for (int z = 0; z < NUM_TOPIC; z++) if (norm_w_z[z] != 0) {
		for (int w = 0; w < NUM_TERM; w++) {
			fprintf(fout, "%.12lf", p_w_z[maxK][w][z]/norm_w_z[z]);
			if (w != NUM_TERM-1)
				fprintf(fout, "%s", "\t");
		}
		fprintf(fout, "%s", "\n");
	}
	fclose(fout);


	/// Free memory
	free(bill_word_len);
	for (int k = 0; k < testNum; k++) {
		free_dmatrix(p_z_d[k], NUM_TOPIC);
		free_dmatrix(p_w_z[k], NUM_TERM);
	}
	free(p_z_d);	free(p_w_z);
	for (int i = 0; i < NUM_BILL; i++)
		free(n_dw[i]);
	free(n_dw);

	return 0;
}
