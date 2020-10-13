package edu.handong.csee.isel.txt;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

public class CheckArffRedundancy {

	public static void main(String[] args) throws Exception {
		File arff = new File(args[0]);
		
		ArrayList<String> allData = new ArrayList<String>();
		HashMap<String,ArrayList<String>> arffs = new HashMap<>(); //line,key
		
		String content = FileUtils.readFileToString(arff, "UTF-8");

		String[] lines = content.split("\n");
		boolean tf = false;
		for (String line : lines) {
			if(line.compareTo("@data") == 0) {
				tf = true;
				continue;
			}
			
			if(tf == false) continue;
			
			String commit = line.substring(line.lastIndexOf(","),line.length()-1);
			if(commit.startsWith(",20")) {
				commit = commit.substring(commit.lastIndexOf(" ")+1, commit.length());
			}
			line = line.substring(0,line.lastIndexOf(","));
			ArrayList<String> commits;
			
			if(arffs.containsKey(line)) {
				commits = arffs.get(line);
				commits.add(commit);
			}else {
				commits = new ArrayList<String>();
				commits.add(commit);
				arffs.put(line, commits);
			}
			
		}
		
		
		ArrayList<String> key = new ArrayList<>();
		
		for(String line : arffs.keySet()) {
			ArrayList<String> commits = arffs.get(line);
			
			if(commits.size() > 1) {
				key.addAll(commits);
//				System.out.println(commits);
//				System.out.println("   line : " + line);
//				System.out.println();
			}
		}
		
		Git git = Git.open(new File(args[1]));
		Repository repo = git.getRepository();
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		
		Iterable<RevCommit> initialCommits = git.log().all().call();
		
		HashMap<String,String> csvPrint = new HashMap<>();

		for (RevCommit commit : initialCommits) {
			String commitHash = commit.getName();
			
			if (commit.getParentCount() == 0) continue;
			RevCommit parent = commit.getParent(0);
			if (parent == null)
				continue;
			
			List<DiffEntry> diff = Utils.diff(parent, commit, repo);
			
			for (DiffEntry entry : diff) {
				String sourcePath = entry.getNewPath().toString();
				
				if (sourcePath.indexOf("Test") >= 0 || !sourcePath.endsWith(".java"))
					continue;
				
				String keySourcePath = sourcePath.replaceAll("/", "-");
				String akey = commitHash+"-"+keySourcePath;
				
				if(key.contains(akey)) {
					try (DiffFormatter formatter = new DiffFormatter(byteStream)) { //한 소스파일의 diff 읽기(코드 보기)
						formatter.setRepository(repo);
						formatter.format(entry);
						
						String diffContent = byteStream.toString();
						csvPrint.put(akey, diffContent);
						
						byteStream.reset();
					}
					//diff print
					
				}
			}
			
		}
		
		String resultCSVPath = "/Users/yangsujin/Desktop/compareResult.csv";
		BufferedWriter writer = new BufferedWriter(new FileWriter( new File(resultCSVPath)));
		CSVPrinter csvPrinter = new CSVPrinter(writer, 
				CSVFormat.DEFAULT.withHeader("Key","contents"));

		Set<Map.Entry<String, String>> entries = csvPrint.entrySet();

		for (Map.Entry<String,String> entry : entries) {
			String aaakey = entry.getKey();
			String contents = entry.getValue();

			csvPrinter.printRecord(aaakey,contents);
		}

		csvPrinter.close();
	}

}
