package edu.handong.csee.isel.data;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class CompairTwoResult {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		File first = new File(args[0]);
		File second = new File(args[1]);
		
		//read
		String content1 = FileUtils.readFileToString(first, "UTF-8");
		
		String content2 = FileUtils.readFileToString(second, "UTF-8");
	
		String[] lines1 = content1.split("\n");
		String[] lines2 = content2.split("\n");
		
		for(int i = 0; i < lines1.length; i++) {
			if(!lines1[i].equals(lines2[i])) {
				System.out.println("not Equal");
				System.out.println(lines1[i]);
				System.out.println(lines2[i]);
			}
		}
		
		
	}


}
