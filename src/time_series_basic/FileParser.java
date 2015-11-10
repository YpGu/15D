import java.io.*;
import java.util.*;
import java.lang.*;

public class FileParser {

	public static void 
	readBillMap(String fileDir, Map<String, Long> billMap) {
		try (BufferedReader br = new BufferedReader(new FileReader(fileDir))) {
			String currentLine;
			while ((currentLine = br.readLine()) != null) {
				// each line: bill_name  \t  bill_id
				String[] tokens = currentLine.split("\t");
				String billName = tokens[0];
				long billID = Integer.parseInt(tokens[1]);
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
	readPersonMap(String fileDir, Map<Long, Long> personMap) {
		try (BufferedReader br = new BufferedReader(new FileReader(fileDir))) {
			String currentLine;
			while ((currentLine = br.readLine()) != null) {
				String[] tokens = currentLine.split("\t");
				long newID = Integer.parseInt(tokens[0]);
				long gtID = Integer.parseInt(tokens[tokens.length-1]);
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
	readData(
		String fileDir, 
		Map<String, Long> billMap, Map<Long, Long> personMap,
		List<Long> votingMatPos, List<Long> votingMatNeg
	) {
		long billMapSize = billMap.size();
		String[] fs = fileDir.split("/");
		long billID = billMap.get(fs[fs.length-1]);
		try (BufferedReader br = new BufferedReader(new FileReader(fileDir))) {
			String currentLine;
			while ((currentLine = br.readLine()) != null) {
				String[] tokens = currentLine.split("\t");
				long gtID = (long)(Integer.parseInt(tokens[0]));
				try {
					long personID = personMap.get(gtID);
					long globalID = personID * billMapSize + billID;
					String vote = tokens[1];
					if (vote.toLowerCase().equals("yea")) {
					//	System.out.printf("personID = %d, billID = %d, globalID = %d\n", personID, billID, globalID);
						votingMatPos.add(globalID);
					} else if (vote.toLowerCase().equals("nay")) {
					//	System.out.printf("personID = %d, billID = %d, globalID = %d\n", personID, billID, globalID);
						votingMatNeg.add(globalID);
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
				writer.printf("%f\n", arr[i]);
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

}
