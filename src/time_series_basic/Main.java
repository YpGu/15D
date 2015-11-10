/**
	11/09/2015 
	Main.java: implement the ideal point model on voting data with time series infomation 
	What's included: gradient descent algorithm; regularization; fast sigmoid function; multiple dimension IDP
	What's not included: line search; time series regularization
	Time: ~35 min for 1000 gd iterations 
**/

import java.util.*;
import java.io.*;
import java.lang.*;

public class Main
{
	public static final int K = 1;
	public static final double lr = 0.001;
	public static final double reg = 0.0001;
	public static double[][] p, q;
	public static double[] b;
	public static Map<String, Long> billMap;		// bill_name -> bill_id 
	public static Map<Long, Long> personMap;		// person_gov_track_id -> person_new_id 
	public static long NUM_BILLS;
	public static long NUM_PEOPLE;
	public static final int MAX_ITER = 1000;
	// encoding: global_id (long) = person_new_id * bill_map_size + bill_id 
	public static List<Long> votingMatPos;
	public static List<Long> votingMatNeg;

	public static final int SIGMOID_BOUND = 100;
	public static final int sigmoid_table_size = 10000000;
	public static double[] sigmoid_table;

	// sigmoid function
	// may be the speed bottleneck; may try sigmoid_table (hashtable)
	public static double logis(double x) {
		if (x > 100) return 1;
		else return 1/(1+Math.exp(-x));
	}

	public static double fastlogis(double x) {
		if (x > SIGMOID_BOUND) return 1;
		else if (x < -SIGMOID_BOUND) return -1;
		int k = (int)((x + SIGMOID_BOUND) * sigmoid_table_size / SIGMOID_BOUND / 2);
//		double realsig = 1.0/(1+Math.exp(-x));
//		System.out.println("real sigmoid = " + realsig + " approx sigmoid = " + sigmoid_table[k]);
//		Scanner sc = new Scanner(System.in);
//		int gu = sc.nextInt();
		return sigmoid_table[k];
	}

	public static void 
	train(List<Long> votingMatPos, List<Long> votingMatNeg) {
		double oldObj = -1, newObj;

		for (int iter = 0; iter < MAX_ITER; iter++) {
			System.out.println("--- Iter " + iter + " ---");
			double[][] gradP = new double[(int)NUM_PEOPLE][K];
			double[][] gradQ = new double[(int)NUM_BILLS][K];
			double[] gradB = new double[(int)NUM_BILLS];

			// gradient 
			for (long pbid: votingMatPos) {
				int pid = (int)(pbid / NUM_BILLS);
				int bid = (int)(pbid % NUM_BILLS);

				double prob = b[bid];
				for (int k = 0; k < K; k++) 
					prob += p[pid][k] * q[bid][k];
				prob = fastlogis(prob);

				for (int k = 0; k < K; k++) {
					gradP[pid][k] += (1-prob) * q[bid][k];
					gradQ[bid][k] += (1-prob) * p[pid][k];
					gradB[bid] += (1-prob);
				}
			}
			for (long pbid: votingMatNeg) {
				int pid = (int)(pbid / NUM_BILLS);
				int bid = (int)(pbid % NUM_BILLS);

				double prob = b[bid];
				for (int k = 0; k < K; k++) 
					prob += p[pid][k] * q[bid][k];
				prob = fastlogis(prob);

				for (int k = 0; k < K; k++) {
					gradP[pid][k] -= prob * q[bid][k];
					gradQ[bid][k] -= prob * p[pid][k];
					gradB[bid] -= prob;
				}
			}
			// update
			for (int i = 0; i < NUM_PEOPLE; i++) {
				for (int k = 0; k < K; k++) {
					p[i][k] *= (1-reg);
					p[i][k] += gradP[i][k] * lr;
				}
			}
			for (int j = 0; j < NUM_BILLS; j++) {
				for (int k = 0; k < K; k++) {
					q[j][k] *= (1-reg);
					q[j][k] += gradQ[j][k] * lr;
				}
				b[j] *= (1-reg);
				b[j] += gradB[j] * lr;
			}
			// check
			newObj = calcObj(votingMatPos, votingMatNeg);
			System.out.println("obj = " + newObj);
			if (newObj < oldObj && iter > 10) break;
			oldObj = newObj;
		}
		System.out.println("training done");
	}

	public static double 
	calcObj(List<Long> votingMatPos, List<Long> votingMatNeg) {
		double res = 0;
		for (long pbid: votingMatPos) {
			int pid = (int)(pbid / NUM_BILLS);
			int bid = (int)(pbid % NUM_BILLS);
			double prob = b[bid];
			for (int k = 0; k < K; k++) 
				prob += p[pid][k] * q[bid][k];
			res += Math.log(fastlogis(prob) + Double.MIN_VALUE);
		}
		for (long pbid: votingMatNeg) {
			int pid = (int)(pbid / NUM_BILLS);
			int bid = (int)(pbid % NUM_BILLS);
			double prob = b[bid];
			for (int k = 0; k < K; k++) 
				prob += p[pid][k] * q[bid][k];
			res += Math.log(1 - fastlogis(prob) + Double.MIN_VALUE);
		}
		return res;
	}

	public static void
	init(String[] args) {
		billMap = new HashMap<String, Long>();
		personMap = new HashMap<Long, Long>();
		votingMatPos = new ArrayList<Long>();
		votingMatNeg = new ArrayList<Long>();

		FileParser.readBillMap("../../voting_data/bills/bill_dict", billMap);
		NUM_BILLS = billMap.size();
		FileParser.readPersonMap("../../voting_data/people/person_dict", personMap);
		NUM_PEOPLE = personMap.size();

		File dir = new File("../../voting_data/all/");
		Scanner sc = new Scanner(System.in);
		int fileCount = 0;

		for (File f: dir.listFiles()) {
			String path = f.getPath();
			FileParser.readData(path, billMap, personMap, votingMatPos, votingMatNeg);
//			if (fileCount == 10000) break;
			fileCount += 1;
		}

		/* example:
		FileParser.readData("../../voting_data/all/1_1_h1", billMap, personMap, votingMatPos, votingMatNeg);
		System.out.println("pos = " + votingMatPos.size());
		System.out.println("neg = " + votingMatNeg.size());
		*/

		p = new double[(int)NUM_PEOPLE][K];
		q = new double[(int)NUM_BILLS][K];
		b = new double[(int)NUM_BILLS];
		Random r = new Random(0);
		for (int i = 0; i < NUM_PEOPLE; i++) {
			for (int k = 0; k < K; k++) {
				p[i][k] = 0.02 * (r.nextDouble()-0.5);
			}
		}
		for (int j = 0; j < NUM_BILLS; j++) {
			for (int k = 0; k < K; k++) {
				q[j][k] = 0.02 * (r.nextDouble()-0.5);
			}
			b[j] = 0.02 * (r.nextDouble()-0.5);
		}

		sigmoid_table = new double[sigmoid_table_size];
		for (int k = 0; k < sigmoid_table_size; k++) {
			double x = 2 * SIGMOID_BOUND * k / sigmoid_table_size - SIGMOID_BOUND;
			sigmoid_table[k] = 1 / (1 + Math.exp(-x));
		}

		return;
	}

	// Entry
	public static void main(String[] args) {
		init(args);
		train(votingMatPos, votingMatNeg);
		FileParser.output(p, "./res/p.txt");
		FileParser.output(q, "./res/q.txt");
		FileParser.output(b, "./res/b.txt");
	}
}
