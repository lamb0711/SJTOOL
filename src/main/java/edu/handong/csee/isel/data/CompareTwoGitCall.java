package edu.handong.csee.isel.data;

import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

public class CompareTwoGitCall {
	public static void main(String[] args) throws Exception {
		ArrayList<String> gitLogCall = new ArrayList<>();
		ArrayList<String> gitLogAllCall = new ArrayList<>();
		
		Reader in1 = new FileReader(args[0]);//gitLogCall csv
		Reader in2 = new FileReader(args[1]);//gitLogAllCall csv
		
		Iterable<CSVRecord> records1 = CSVFormat.RFC4180.withHeader().parse(in1);
		Iterable<CSVRecord> records2 = CSVFormat.RFC4180.withHeader().parse(in2);

		for (CSVRecord record : records1) {
			gitLogCall.add(record.get("Key"));
		}
		
		for (CSVRecord record : records2) {
			gitLogAllCall.add(record.get("Key"));
		}
		
		for(String all : gitLogAllCall) {
			if(gitLogCall.contains(all)) {
				gitLogCall.remove(all);
			}
		}
		
		System.out.println(gitLogCall.size());
		System.out.println(gitLogCall);
		
	}
}
