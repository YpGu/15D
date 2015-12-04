/**
	11/09/2015 
	Runner.java: implement the ideal point model on voting data with time series infomation 
	What's included: gradient descent algorithm; regularization; fast sigmoid function; multiple dimension IDP
	What's not included: line search
	Time: ~35 min for 1000 gd iterations 
**/

import java.util.*;
import java.io.*;
import java.lang.*;

public class Main
{
	public static final int K = 5;
//	public static double lr = 0.0005;
	public static double lr = 0.0001;			// w/ topic model? 
								// a larger init range (p/q/b) and a smaller learning rate
//	public static final double reg1 = 1;
	public static final double reg1 = 0;
	public static double reg2 = 250;

	public static Map<String, Integer> billMap;		// bill_name -> bill_id 
	public static Map<Long, Integer> personMap;		// person_gov_track_id -> person_new_id 
	public static Map<Integer, Integer> personPartyMap;	// person_new_id -> party (1=R; 2=D; 3=Other) 
	public static int NUM_BILLS;
	public static int NUM_PEOPLE;
	public static final int MAX_ITER = 100;
	public static final int MAX_YEAR = 115;			// note: year starts from 1
	public static List<Tuple> votingMatPos;
	public static List<Tuple> votingMatNeg;
	public static Map<Integer, List<Integer>> ptime;	// person_new_id -> List<year> 
	public static Map<Integer, List<Integer>> candidates;	// year -> List(<person_new_id>)
	public static double[][] theta;				// topic distribution for bills 

	public static final int SIGMOID_BOUND = 30;
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

	public static void copyArray(
		double[][][] des1, double[][][] src1,
		double[][] des2, double[][] src2,
		double[] des3, double[] src3
	) {
		// 1
		for (int i = 0; i < des1.length; i++) {
			for (int j = 0; j < des1[i].length; j++) {
				System.arraycopy(src1[i][j], 0, des1[i][j], 0, src1[i][j].length);
			}
		}
		// 2
		for (int i = 0; i < des2.length; i++) {
			System.arraycopy(src2[i], 0, des2[i], 0, src2[i].length);
		}
		// 3
		System.arraycopy(src3, 0, des3, 0, src3.length);

		return;
	}

	public static double
	train(double[][][] p, double[][] q, double[] b, List<Tuple> votingMatPos, List<Tuple> votingMatNeg, int maxIteration) {
		double oldObj = -1, newObj;

		for (int iter = 0; iter < maxIteration; iter++) {
			System.out.println("--- Iter " + iter + " ---");
			double[][][] gradP = new double[MAX_YEAR][(int)NUM_PEOPLE][K];
			double[][] gradQ = new double[NUM_BILLS][K];
			double[] gradB = new double[NUM_BILLS];

			// gradient 
			for (Tuple t: votingMatPos) {
				int pid = t.pid; int bid = t.bid; int year = t.year;

				double prob = b[bid];
				for (int k = 0; k < K; k++) 
					prob += p[year][pid][k] * q[bid][k] * theta[bid][k];
				prob = fastlogis(prob);

				for (int k = 0; k < K; k++) {
					gradP[year][pid][k] += (1-prob) * q[bid][k] * theta[bid][k];
					gradQ[bid][k] += (1-prob) * p[year][pid][k] * theta[bid][k];
					gradB[bid] += (1-prob);
				}
			}
			for (Tuple t: votingMatNeg) {
				int pid = t.pid; int bid = t.bid; int year = t.year;

				double prob = b[bid];
				for (int k = 0; k < K; k++) {
					prob += p[year][pid][k] * q[bid][k] * theta[bid][k];
				}
				prob = fastlogis(prob);

				for (int k = 0; k < K; k++) {
					gradP[year][pid][k] -= prob * q[bid][k] * theta[bid][k];
					gradQ[bid][k] -= prob * p[year][pid][k] * theta[bid][k];
					gradB[bid] -= prob;
				}
			}
			// update
			// time regulatization (on p) 
			for (int i = 0; i < NUM_PEOPLE; i++) {
				List<Integer> ys = ptime.get(i);
				int numOfTerms = 0;
				if (ys.size() == 0 || ys.size() == 1) 
					continue;
				for (int y: ys) {
					if (ys.contains(y+1)) numOfTerms += 1;
					if (ys.contains(y+1)) for (int k = 0; k < K; k++) {
						gradP[y][i][k] -= reg2 * (p[y][i][k] - p[y+1][i][k]) / numOfTerms;
						gradP[y+1][i][k] -= reg2 * (p[y+1][i][k] - p[y][i][k]) / numOfTerms;
					}
				}
			}
			// l2 regularization 
			for (int year = 1; year < MAX_YEAR; year++) {
				for (int i = 0; i < NUM_PEOPLE; i++) {
					for (int k = 0; k < K; k++) {
						p[year][i][k] *= (1 - lr * reg1);
						p[year][i][k] += gradP[year][i][k] * lr;
					}
				}
			}
			for (int j = 0; j < NUM_BILLS; j++) {
				for (int k = 0; k < K; k++) {
					q[j][k] *= (1 - lr * reg1);
					q[j][k] += gradQ[j][k] * lr;
				}
				b[j] *= (1 - lr * reg1);
				b[j] += gradB[j] * lr;
			}

			// check
			newObj = calcObj(p, q, b, votingMatPos, votingMatNeg);
			if (newObj < oldObj && iter > 10) {
				System.out.printf("newObj = %f, oldObj = %f, break\n", newObj, oldObj);
				break;
			}
			oldObj = newObj;
		}

		return oldObj;
	}

	public static double 
	calcObj(double[][][] p, double[][] q, double[] b, List<Tuple> votingMatPos, List<Tuple> votingMatNeg) {
		double res = 0;
		double i_res = 0; int i_pid = 11144;
		for (Tuple t: votingMatPos) {
			int pid = t.pid; int bid = t.bid; int year = t.year;
			double prob = b[bid];
			for (int k = 0; k < K; k++) 
				prob += p[year][pid][k] * q[bid][k] * theta[bid][k];
			res += Math.log(fastlogis(prob) + Double.MIN_VALUE);

			if (pid == i_pid) i_res += Math.log(fastlogis(prob) + Double.MIN_VALUE);
		}
		for (Tuple t: votingMatNeg) {
			int pid = t.pid; int bid = t.bid; int year = t.year;
			double prob = b[bid];
			for (int k = 0; k < K; k++) 
				prob += p[year][pid][k] * q[bid][k] * theta[bid][k];
			res += Math.log(1 - fastlogis(prob) + Double.MIN_VALUE);

			if (pid == i_pid) i_res += Math.log(1 - fastlogis(prob) + Double.MIN_VALUE);
		}
		System.out.printf("i_res = %f (probability part)\n", i_res);
		// reg 1 
		double r1 = res;
		double i_r1 = i_res;
		for (int i = 0; i < NUM_PEOPLE; i++) {
			for (int y: ptime.get(i)) {
				for (int k = 0; k < K; k++) {
					res -= 0.5 * reg1 * p[y][i][k] * p[y][i][k];
				}
			}
			if (i == i_pid) for (int y: ptime.get(i)) for (int k = 0; k < K; k++) {
				i_res -= 0.5 * reg1 * p[y][i][k] * p[y][i][k];
			}
		}
		for (int j = 0; j < NUM_BILLS; j++) {
			for (int k = 0; k < K; k++) {
				res -= 0.5 * reg1 * q[j][k] * q[j][k];
			}
			res -= 0.5 * reg1 * b[j];
		}
		double l1 = r1 - res;
		double i_l1 = i_r1 - i_res;
		// reg 2: time regulatization (on p) 
		double r2 = res;
		double i_r2 = i_res;
		for (int i = 0; i < NUM_PEOPLE; i++) {
			List<Integer> ys = ptime.get(i);
			if (ys.size() == 0 || ys.size() == 1) 
				continue;
			int numOfTerms = 0;
			for (int y: ys) {
				if (ys.contains(y+1)) numOfTerms += 1;
				if (ys.contains(y+1))  for (int k = 0; k < K; k++) {
					res -= 0.5 * reg2 * (p[y][i][k] - p[y+1][i][k]) * (p[y][i][k] - p[y+1][i][k]) / numOfTerms;
				}
			}
			if (i == i_pid) for (int y: ys) for (int k = 0; k < K; k++) {
				double v = 0.5 * reg2 * (p[y][i][k] - p[y+1][i][k]) * (p[y][i][k] - p[y+1][i][k]) / numOfTerms;
				i_res -= v;
			}
		}
		double l2 = r2 - res;
		double i_l2 = i_r2 - i_res;
		System.out.println("obj = " + res + " l2_reg = " + l1 + " time_reg = " + l2);
		System.out.println("l2 reg = " + i_l1 + ", time reg = " + i_l2);

		return res;
	}

	public static void
	config(String[] args) {
		billMap = new HashMap<String, Integer>();
		personMap = new HashMap<Long, Integer>();
		personPartyMap = new HashMap<Integer, Integer>();
		ptime = new HashMap<Integer, List<Integer>>();
		candidates = new HashMap<Integer, List<Integer>>();
		votingMatPos = new ArrayList<Tuple>();
		votingMatNeg = new ArrayList<Tuple>();

		FileParser.readBillMap("../../voting_data/bills/bill_dict", billMap);
		NUM_BILLS = billMap.size();
		FileParser.readPersonMap("../../voting_data/people/person_dict", personMap, personPartyMap);
		NUM_PEOPLE = personMap.size();
		FileParser.readTime("../../voting_data/people/person_year", ptime);

		File dir = new File("../../voting_data/all/");
		Scanner sc = new Scanner(System.in);
		int fileCount = 0;

//		/* all bills
		for (File f: dir.listFiles()) {
			String path = f.getPath();
			String[] ps = path.split("/");
			FileParser.readData(path, billMap.get(ps[ps.length-1]), personMap, votingMatPos, votingMatNeg);
			if (fileCount % 10000 == 0)
				System.out.println(fileCount);
			fileCount += 1;
		}
//		*/

		/* selected bills (w/ topic model)
		for (Map.Entry<String, Integer> e: billMap.entrySet()) {
			String path = e.getKey();
			FileParser.readData(path, billMap, personMap, votingMatPos, votingMatNeg);
			if (fileCount % 10000 == 0)
				System.out.println(fileCount);
			fileCount += 1;
		}
		*/

		sigmoid_table = new double[sigmoid_table_size];
		for (int k = 0; k < sigmoid_table_size; k++) {
			double x = 2 * SIGMOID_BOUND * k / sigmoid_table_size - SIGMOID_BOUND;
			sigmoid_table[k] = 1 / (1 + Math.exp(-x));
		}

		theta = new double[NUM_BILLS][K];
		FileParser.readTheta("", theta);

		System.out.println("Reading Done");

		return;
	}

	// with topic model 
	public static void
	config2(String[] args) {
		System.out.println("Using topic model! K = " + K);
		billMap = new HashMap<String, Integer>();
		personMap = new HashMap<Long, Integer>();
		personPartyMap = new HashMap<Integer, Integer>();
		ptime = new HashMap<Integer, List<Integer>>();
		candidates = new HashMap<Integer, List<Integer>>();
		votingMatPos = new ArrayList<Tuple>();
		votingMatNeg = new ArrayList<Tuple>();

		FileParser.readBillMap("../topic_model/bill_dict_tss.txt", billMap);
		NUM_BILLS = billMap.size();
		FileParser.readPersonMap("../../voting_data/people/person_dict", personMap, personPartyMap);
		NUM_PEOPLE = personMap.size();
		FileParser.readTime("../../voting_data/people/person_year", ptime);

//		File dir = new File("../../voting_data/all/");
		Scanner sc = new Scanner(System.in);
		int fileCount = 0;

//		/* selected bills (w/ topic model)
		int nonExist = 0;
		for (Map.Entry<String, Integer> e: billMap.entrySet()) {
			String path = e.getKey();
			String[] ps = path.split("/", -1);
			String cong = ps[4], billNum = ps[7];
			if (billNum.split("r").length == 2) 
				billNum = billNum.split("r")[0] + billNum.split("r")[1];	// hr -> h 
			String year1 = Integer.toString(Integer.parseInt(cong) * 2 + 1787);
			String year2 = Integer.toString(Integer.parseInt(cong) * 2 + 1788);
			String vote1 = "../../voting_data/all/" + cong + "_" + year1 + "_" + billNum;
			String vote2 = "../../voting_data/all/" + cong + "_" + year2 + "_" + billNum;
			int billID = billMap.get(path);
			if ((new File(vote1)).exists()) {
				FileParser.readData(vote1, billID, personMap, votingMatPos, votingMatNeg);
			} else if ((new File(vote2)).exists()) {
				FileParser.readData(vote2, billID, personMap, votingMatPos, votingMatNeg);
			} else {
				System.out.println(vote1 + " " + vote2);
				nonExist += 1;
			}

			if (fileCount % 10000 == 0)
				System.out.println(fileCount);
			fileCount += 1;
		}
		System.out.println("Num of positive votes = " + votingMatPos.size());
		System.out.println("Num of negative votes = " + votingMatNeg.size());
//		*/

		sigmoid_table = new double[sigmoid_table_size];
		for (int k = 0; k < sigmoid_table_size; k++) {
			double x = 2 * SIGMOID_BOUND * k / sigmoid_table_size - SIGMOID_BOUND;
			sigmoid_table[k] = 1 / (1 + Math.exp(-x));
		}

		theta = new double[NUM_BILLS][K];
		FileParser.readTheta("../topic_model/res/plsa_dz.txt", theta);

		// read all people appeared in this particular period of time 
		FileParser.readPeople("../../voting_data/people/person_year_82_114", candidates);

		System.out.println("Reading Done");

		return;
	}


	public static void
	init (String[] args, double[][][] p, double[][] q, double[] b, int seed) {
		/* example:
			FileParser.readData("../../voting_data/all/1_1_h1", billMap, personMap, votingMatPos, votingMatNeg);
			System.out.println("pos = " + votingMatPos.size());
			System.out.println("neg = " + votingMatNeg.size());
		*/

		// random initialization 
		Random r = new Random(seed);
		double range = 2;
		for (int year = 1; year < MAX_YEAR; year++) {
			for (int i = 0; i < NUM_PEOPLE; i++) {
				for (int k = 0; k < K; k++) {
					p[year][i][k] = range * (r.nextDouble()-0.5);
				}
			}
		}
		for (int j = 0; j < NUM_BILLS; j++) {
			for (int k = 0; k < K; k++) {
				q[j][k] = range * (r.nextDouble()-0.5);
			}
			b[j] = range * (r.nextDouble()-0.5);
		}

		return;
	}

	// Entry
	public static void main(String[] args) {
		if (args.length < 2) {
			System.out.println("Usage: java Main <learning_rate> <time_regularization>");
			System.out.printf("Using default: lr = %f; time reg = %f\n", lr, reg2);
		} else {
			lr = Double.parseDouble(args[0]);
			reg2 = Double.parseDouble(args[1]);
			System.out.println("lr = " + Double.toString(lr) + ", time reg = " + reg2);
		}

		config2(args);

		int numOfTrials = 1;
		int[] seeds = new int[numOfTrials];
		Random r = new Random(0);
		for (int i = 0; i < numOfTrials; i++) seeds[i] = r.nextInt();

		int maxI = -1; double maxObj = -Double.MAX_VALUE;
		double[][][] bestP = new double[MAX_YEAR][NUM_PEOPLE][K];
		double[][] bestQ = new double[NUM_BILLS][K];
		double[] bestB = new double[NUM_BILLS];

		long t1 = System.currentTimeMillis();
		for (int i = 0; i < numOfTrials; i++) {
			System.out.printf("------ Trial %d ------\n", i);
			double[][][] p = new double[MAX_YEAR][NUM_PEOPLE][K];
			double[][] q = new double[NUM_BILLS][K];
			double[] b = new double[NUM_BILLS];
			int seed = seeds[i];
			init(args, p, q, b, seed);
			double curObj = train(p, q, b, votingMatPos, votingMatNeg, 100);
			if (curObj > maxObj) {
				maxI = i;
				maxObj = curObj;
				copyArray(bestP, p, bestQ, q, bestB, b);
			}
		}
		System.out.println("Best i = " + maxI + ", best seed = " + seeds[maxI]);
		train(bestP, bestQ, bestB, votingMatPos, votingMatNeg, MAX_ITER - 100);
		long t2 = System.currentTimeMillis();
		double dif = (t2-t1)/1000.0;
		System.out.println("Total training time = " + dif + " seconds.");

		int d_reg = (int)reg2;
		for (int year = 1; year < MAX_YEAR; year++) {
			String outputPath = "./res/p_" + Integer.toString(year) + "_" + Integer.toString(d_reg) + ".txt";
			FileParser.output(candidates, bestP[year], outputPath, year);
		}
		FileParser.output(candidates, bestQ, "./res/q_" + Integer.toString(d_reg) + ".txt", -1);
		FileParser.output(candidates, bestB, "./res/b_" + Integer.toString(d_reg) + ".txt");
	}
}
