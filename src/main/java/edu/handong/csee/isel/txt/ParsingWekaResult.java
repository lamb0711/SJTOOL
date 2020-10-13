package edu.handong.csee.isel.txt;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

public class ParsingWekaResult {
	private final static String numberPatternStr = "(-?\\d.\\d+)|(\\?)";
	private final static Pattern numberPattern = Pattern.compile(numberPatternStr);

	
	//args[0] :  
	public static void main(String[] args) throws Exception {
		ArrayList<String> project_algorithm = new ArrayList<String>();
		ArrayList<String> precision = new ArrayList<String>();
		ArrayList<String> recall = new ArrayList<String>();
		ArrayList<String> f_measure = new ArrayList<String>();
		
		String inputPath = args[0];
		File dir = new File(inputPath);
		File []fileList = dir.listFiles();
		
		for(File file : fileList) {
			String fileName = file.getName();
			if(!fileName.endsWith(".txt")) continue;
			
			project_algorithm.add(fileName);
			
			String content = FileUtils.readFileToString(file, "UTF-8");
			String[] lines = content.split("\n");
			
			for (String line : lines) {
				if(line.endsWith("buggy")) {
//					System.out.println(line);
					
					Matcher matcher = numberPattern.matcher(line);
					int i = 1; 
					while(matcher.find()) {
						if(i == 3) {
							precision.add(matcher.group(0));
						}else if(i == 4) {
							recall.add(matcher.group(0));
						}else if(i == 5) {
							f_measure.add(matcher.group(0));
						}
						i++;
					}
				}
			}
		}
		
		System.out.println("bayesNet");
		System.out.println("precision | recall | f_measure");
		for(int i = 0; i < project_algorithm.size(); i++) {
			if(project_algorithm.get(i).contains("bayesNet")) {
				System.out.println(precision.get(i)+","+recall.get(i)+","+f_measure.get(i));
			}
		}
		System.out.println();
		System.out.println("NaiveBayes");
		System.out.println("precision | recall | f_measure");
		for(int i = 0; i < project_algorithm.size(); i++) {
			if(project_algorithm.get(i).contains("naive")) {
				System.out.println(precision.get(i)+","+recall.get(i)+","+f_measure.get(i));
			}
		}
		System.out.println();
		System.out.println("random");
		System.out.println("precision | recall | f_measure");
		for(int i = 0; i < project_algorithm.size(); i++) {
			if(project_algorithm.get(i).contains("random")) {
				System.out.println(precision.get(i)+","+recall.get(i)+","+f_measure.get(i));
			}
		}
		System.out.println();
		System.out.println("LMT");
		System.out.println("precision | recall | f_measure");
		for(int i = 0; i < project_algorithm.size(); i++) {
			if(project_algorithm.get(i).contains("lmt")) {
				System.out.println(precision.get(i)+","+recall.get(i)+","+f_measure.get(i));
			}
		}
		System.out.println();
		System.out.println("J48");
		System.out.println("precision | recall | f_measure");
		for(int i = 0; i < project_algorithm.size(); i++) {
			if(project_algorithm.get(i).contains("j48")) {
				System.out.println(precision.get(i)+","+recall.get(i)+","+f_measure.get(i));
			}
		}
		System.out.println();
		System.out.println("IBK");
		System.out.println("precision | recall | f_measure");
		for(int i = 0; i < project_algorithm.size(); i++) {
			if(project_algorithm.get(i).contains("ibk")) {
				System.out.println(precision.get(i)+","+recall.get(i)+","+f_measure.get(i));
			}
		}
	}
}
