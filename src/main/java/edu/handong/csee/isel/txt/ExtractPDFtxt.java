package edu.handong.csee.isel.txt;

public class ExtractPDFtxt {
public static void main(String[] args) throws Exception {
		
		String target = "This paper presents ErrHunter, which is a systematic static\n" + 
				"analysis approach to detect error miss-handling bugs in\n" + 
				"the Linux kernel. After error-handling path exploration,\n" + 
				"ErrHunter employs cross-control-flow static taint analysis to\n" + 
				"construct the CCFG of every critical variable, which includes\n" + 
				"both control flow and data propagation information of the\n" + 
				"target variable. By analyzing the CCFG, ErrHunter can\n" + 
				"detect four kinds of the most common error miss-handling\n" + 
				"bugs in the Linux kernel, based on the analysis of the root\n" + 
				"cause of these bugs. The experimental results show that\n" + 
				"ErrHunter is able to detect different kinds of error-handling\n" + 
				"bugs in the Linux kernel. In future, we are going to apply\n" + 
				"ErrHunter to more systems such as other kinds of kernels\n" + 
				"and C libraries.";
		DeleteNewline(target);
	}
	
	public static void DeleteNewline(String target) {
		String transformed_target = target.replaceAll("(\r\n|\r|\n|\n\r)", " ");
		System.out.println(transformed_target);
	}
}
