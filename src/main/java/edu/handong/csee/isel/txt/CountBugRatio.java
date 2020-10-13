package edu.handong.csee.isel.txt;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import edu.handong.csee.isel.data.CSVinfo;


public class CountBugRatio {
	static int allBugNum = 0;
	
	private final static String timePatternStr = "(\\d\\d\\d\\d-\\d\\d)-\\d\\d(\\s.+)";
	private final static Pattern timePattern = Pattern.compile(timePatternStr);
	
	public static void main(String[] args) throws Exception {
		TreeMap<String, ArrayList<CSVinfo>> metrics = new TreeMap<>();///
		HashMap<String, Integer> bugRatio = new HashMap<>();
		HashMap<String, Integer> allNum = new HashMap<>();
		
		Reader in = new FileReader(args[0]);
		Iterable<CSVRecord> records = CSVFormat.RFC4180.withHeader().parse(in);
		
		for (CSVRecord record : records) {
			CSVinfo metaDataInfo = new CSVinfo(record);
			String commitTime = metaDataInfo.getCommitTime();
			String isBuggy = metaDataInfo.getIsBuggy();
			
			if(isBuggy.compareTo("clean") == 0) continue;
			
			if(metrics.containsKey(commitTime)) {
				ArrayList<CSVinfo> arr = metrics.get(commitTime);
				arr.add(metaDataInfo);
			}else {
				ArrayList<CSVinfo> arr = new ArrayList<>();
				arr.add(metaDataInfo);
				metrics.put(commitTime, arr);
			}
			allBugNum++;
		}
		
		String front = null;
		int numOfBug = 0;
		
		for(String key : metrics.keySet()) {
			Matcher m = timePattern.matcher(key);
			
			while(m.find()) {
				front = m.group(1);
			}
			
			if(bugRatio.containsKey(front)) {
				numOfBug = bugRatio.get(front) + metrics.get(key).size();
			}else {
				numOfBug = metrics.get(key).size();
			}
			bugRatio.put(front, numOfBug);
		}
		
		int numall = 0;
		for(String key : bugRatio.keySet()) {
			System.out.println(key + " : "+bugRatio.get(key));
			numall = numall + bugRatio.get(key);
		}
		
		System.out.println(allBugNum);
		System.out.println(numall);
		
		
		Save2CSV(bugRatio);
	}

	private static void Save2CSV(HashMap<String, Integer> bugRatioInfos) throws Exception {
		
		String resultCSVPath = "/Users/yangsujin/Desktop/tez-Real.csv";
		BufferedWriter writer = new BufferedWriter(new FileWriter( new File(resultCSVPath)));
		CSVPrinter csvPrinter = new CSVPrinter(writer, 
				CSVFormat.DEFAULT.withHeader("Date","Bug Ratio %"));

		
		for(String date : bugRatioInfos.keySet()) {
			int bugNum = bugRatioInfos.get(date);
			float bugRatio = (float)bugNum / (float) allBugNum;
			bugRatio = bugRatio * 100;
			
			csvPrinter.printRecord(date,bugRatio);

		}
		
		csvPrinter.close();
	}
}
