package edu.handong.csee.isel.txt;

public class ExtractPDFtxt {
public static void main(String[] args) throws Exception {
		
		String target = "";
		DeleteNewline(target);
	}
	
	public static void DeleteNewline(String target) {
		String transformed_target = target.replaceAll("(\r\n|\r|\n|\n\r)", " ");
		System.out.println(transformed_target);
	}
}
