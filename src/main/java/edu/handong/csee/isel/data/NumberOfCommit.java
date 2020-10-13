package edu.handong.csee.isel.data;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.revwalk.RevCommit;

public class NumberOfCommit {
	/*
	 * args[0] : reference path
	 * args[1] : all project csv
	 * args[2] : output
	 */

	public static void main(String[] args) throws Exception {
		
		// read CSV
		Reader in = new FileReader(args[1]);
		Iterable<CSVRecord> records = CSVFormat.RFC4180.withHeader().parse(in);
		// save CSV
		String resultCSVPath = args[2];
		BufferedWriter writer = new BufferedWriter(new FileWriter( new File(resultCSVPath+File.separator+"ProjectList.csv")));
		CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("Project name","ISSUE KEY","Github","Dev Days","Num of Commit"));

		for (CSVRecord record : records) {
			String githubAddress = record.get("Github");
			String gitRemoteURI = githubAddress + ".git";
			String output = args[0];
			String issueKey = record.get("ISSUE KEY");
			String Dev = record.get("Dev Days");
			
			Pattern p = Pattern.compile(".*/(.+)");
			Matcher m = p.matcher(githubAddress);
			m.find();
			String pojectName = m.group(1);
			
			File gitDirectory = null;
			if (isCloned(pojectName,output) && isValidRepository(pojectName,output)) {
				gitDirectory = getGitDirectory(pojectName,output);
			} else if (isCloned(pojectName,output) && (!isValidRepository(pojectName,output))) {
				File directory = getGitDirectory(pojectName,output);
				directory.delete();
				gitDirectory = GitClone(pojectName,output,gitRemoteURI);
			} else {
				gitDirectory = GitClone(pojectName,output,gitRemoteURI);
			}
			if(!isValidRepository(pojectName,output)) continue;
			String gitRepositoryPath = gitDirectory.getAbsolutePath();
			
			Git git = Git.open(new File(gitRepositoryPath));
			
			Iterable<RevCommit> initialCommits = git.log().all().call();
			
			int numOfCommit = (int) StreamSupport.stream(initialCommits.spliterator(), false).count();
			
			System.out.println(numOfCommit);
			csvPrinter.printRecord(pojectName,issueKey,githubAddress,Dev,numOfCommit);
		}
		
		csvPrinter.close();

		
	}
	private static boolean isValidRepository(String pojectName,String output) {
		File directory = getGitDirectory(pojectName,output);
		try {
			Git git = Git.open(directory);
			return true;
		} catch (IOException e) {
			System.out.println("no Project : " + pojectName);
			return false;
		}
	}
	
	public static String getReferencePath(String output) {
		return output + File.separator + "reference";
	}

	public static File getGitDirectory(String pojectName,String output) {
		String referencePath = getReferencePath(output);
		File clonedDirectory = new File(
				referencePath + File.separator + "repositories" + File.separator + pojectName);
		return clonedDirectory;
	}

	private static File GitClone(String pojectName,String output,String gitRemoteURI) throws InvalidRemoteException, TransportException, GitAPIException {
		String remoteURI = gitRemoteURI;
		String projectName = pojectName;
		File clonedDirectory = getGitDirectory(pojectName,output);
		clonedDirectory.mkdirs();
		System.out.println("cloning " + projectName + "...");
		Git git = Git.cloneRepository().setURI(remoteURI).setDirectory(clonedDirectory).setCloneAllBranches(true)
				.call();
		System.out.println("done");
		return git.getRepository().getDirectory();
	}

	private static boolean isCloned(String pojectName,String output) throws InvalidRemoteException, TransportException, GitAPIException {
		File clonedDirectory = getGitDirectory(pojectName,output);
		return clonedDirectory.exists();
	}

}
