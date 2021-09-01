package edu.handong.csee.isel.data;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

public class MakeShellScript {
	public static void main(String[] args) throws Exception {
		
		Reader in = new FileReader(args[0]);
		Iterable<CSVRecord> records = CSVFormat.RFC4180.withHeader().parse(in);

		
		for (CSVRecord record : records) {
			String projectName = null;
			Pattern pattern1 = Pattern.compile("(.+)/(.+)");

			Matcher matcher = pattern1.matcher(record.get(2));
			while(matcher.find()) {
				projectName = matcher.group(2);
			}
			
			System.out.println("/home/yangsujin/git/DPDP/app/build/distributions/app/bin/app -test -i /data/AllBICMetric/"+projectName+"-data.arff -o /data/sujinyang/DPDP/test3 -bow -cm /data/sujinyang/DPDP/train3 -dm /data/sujinyang/DPDP/train3/ClusterModel/ibk");
		}
		
//		ArrayList<String> gitLogCall = new ArrayList<>();
//		ArrayList<String> gitLogAllCall = new ArrayList<>();
//		
//		Reader in1 = new FileReader(args[0]);//gitLogCall csv
//		Reader in2 = new FileReader(args[1]);//gitLogAllCall csv
//		
//		Iterable<CSVRecord> records1 = CSVFormat.RFC4180.withHeader().parse(in1);
//		Iterable<CSVRecord> records2 = CSVFormat.RFC4180.withHeader().parse(in2);
//
//		for (CSVRecord record : records1) {
//			gitLogCall.add(record.get("Key"));
//		}
//		
//		for (CSVRecord record : records2) {
//			gitLogAllCall.add(record.get("Key"));
//		}
//		
//		for(String all : gitLogAllCall) {
//			if(gitLogCall.contains(all)) {
//				gitLogCall.remove(all);
//			}
//		}
//		
//		System.out.println(gitLogCall.size());
//		System.out.println(gitLogCall);
		
	}
}
