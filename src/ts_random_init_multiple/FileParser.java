import java.io.*;
import java.util.*;
import java.lang.*;

public class FileParser {

	public static void 
	readBillMap(String fileDir, Map<String, Integer> billMap) {
		try (BufferedReader br = new BufferedReader(new FileReader(fileDir))) {
			String currentLine;
			while ((currentLine = br.readLine()) != null) {
				// each line: bill_name  \t  bill_id
				String[] tokens = currentLine.split("\t");
				String billName = tokens[0];
				int billID = Integer.parseInt(tokens[1]);
				billMap.put(billName, billID);
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Reading done. Length of billMap = " + billMap.size());

		return;
	}

	public static void 
	readPersonMap(String fileDir, Map<Long, Integer> personMap, Map<Integer, Integer> personPartyMap) {
		try (BufferedReader br = new BufferedReader(new FileReader(fileDir))) {
			String currentLine;
			while ((currentLine = br.readLine()) != null) {
				String[] tokens = currentLine.split("\t");
				int newID = Integer.parseInt(tokens[0]);
				long gtID = Integer.parseInt(tokens[tokens.length-1]);
				String party = tokens[4];
				if (party.equals("Republican")) {
					personPartyMap.put(newID, 1);
				} else if (party.equals("Democrat")) {
					personPartyMap.put(newID, 2);
				} else {
					personPartyMap.put(newID, 3);
				}
			//	System.out.printf("%d\t%d\n", newID, gtID);
				personMap.put(gtID, newID);
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Reading done. Length of personMap = " + personMap.size());

		return;
	}

	public static void
	readTime(String fileDir, Map<Integer, List<Integer>> ptime) {
		for (int i = 0; i < Main.NUM_PEOPLE; i++) 
			ptime.put(i, new ArrayList<Integer>());

		try (BufferedReader br = new BufferedReader(new FileReader(fileDir))) {
			String currentLine;
			while ((currentLine = br.readLine()) != null) {
				String[] tokens = currentLine.split("\t");
				int year = Integer.parseInt(tokens[0]);
				String[] pids = tokens[1].split(",");
				for (String pid: pids) {
					int i_pid = Integer.parseInt(pid);
					ptime.get(i_pid).add(year);
				}
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		/*
		for (int i = 0; i < Main.NUM_PEOPLE; i++) 
			System.out.printf("%d\t%d\n", i, ptime.get(i).size());
		Scanner sc = new Scanner(System.in);
		int gu = sc.nextInt();
		*/

		return;
	}


	public static void
	readData(
		String fileDir, 
		Map<String, Integer> billMap, Map<Long, Integer> personMap,
		List<Tuple> votingMatPos, List<Tuple> votingMatNeg
	) {
		long billMapSize = billMap.size();
		String[] fs = fileDir.split("/");
		int billID = billMap.get(fs[fs.length-1]);
		int year = Integer.parseInt(fs[fs.length-1].split("_")[0]);	// n-th congress 
		try (BufferedReader br = new BufferedReader(new FileReader(fileDir))) {
			String currentLine;
			while ((currentLine = br.readLine()) != null) {
				String[] tokens = currentLine.split("\t");
				long gtID = (long)(Integer.parseInt(tokens[0]));
				try {
					int personID = personMap.get(gtID);
					String vote = tokens[1];
					if (vote.toLowerCase().equals("yea") || vote.toLowerCase().equals("aye")) {
					//	System.out.printf("personID = %d, billID = %d, globalID = %d\n", personID, billID, globalID);
						Tuple t = new Tuple(personID, billID, year);
						votingMatPos.add(t);
					} else if (vote.toLowerCase().equals("nay") || vote.toLowerCase().equals("no")) {
					//	System.out.printf("personID = %d, billID = %d, globalID = %d\n", personID, billID, globalID);
						Tuple t = new Tuple(personID, billID, year);
						votingMatNeg.add(t);
					}
				}
				catch (NullPointerException e) {
				//	System.out.println("null id: " + gtID);
					continue;
				}
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		return;
	}

	public static void
	output(double[][] arr, String fileDir) {
		try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(fileDir)))) {
			for (int i = 0; i < arr.length; i++) {
				writer.printf("%d\t", i);
				for (int j = 0; j < arr[0].length; j++) {
					writer.printf("%f\t", arr[i][j]);
				}
				writer.printf("\n");
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void
	output(double[] arr, String fileDir) {
		try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(fileDir)))) {
			for (int i = 0; i < arr.length; i++) {
				writer.printf("%d\t%f\n", i, arr[i]);
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

}
