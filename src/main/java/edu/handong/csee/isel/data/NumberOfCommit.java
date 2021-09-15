package edu.handong.csee.isel.data;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.TreeSet;
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
		BufferedWriter writerOver100 = new BufferedWriter(new FileWriter( new File(resultCSVPath+File.separator+"ProjectListTrain.csv")));
		BufferedWriter writerLess100 = new BufferedWriter(new FileWriter( new File(resultCSVPath+File.separator+"ProjectListTest.csv")));
		
		CSVPrinter csvPrinterOver100 = new CSVPrinter(writerOver100, CSVFormat.DEFAULT.withHeader("Project name","ISSUE KEY","Github","Dev Days","Num of Commit","NumOver100Dev","StartCommit","EndCommit"));
		CSVPrinter csvPrinterLess100 = new CSVPrinter(writerLess100, CSVFormat.DEFAULT.withHeader("Project name","ISSUE KEY","Github","Dev Days","Num of Commit","StartCommit","EndCommit"));

		for (CSVRecord record : records) {
			String githubAddress = record.get("Github");
			String gitRemoteURI = githubAddress + ".git";
			String output = args[0];
			String issueKey = record.get("ISSUE KEY");
			String Dev = record.get("Dev Days");
			HashMap<String, ArrayList<Integer>> developer_commit = new HashMap<>();
			
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
			
//			int numOfCommit = (int) StreamSupport.stream(initialCommits.spliterator(), false).count();
			
			int count = 0;
			TreeSet<String> commitTime = new TreeSet<>();
			
			for (RevCommit initialCommit : initialCommits) {
				String authorId = parseAuthorID(initialCommit.getAuthorIdent().toString());
				commitTime.add(getStringDateTimeFromCommitTime(initialCommit.getCommitTime()));
				
				if(developer_commit.containsKey(authorId)) {
					ArrayList<Integer> commits = developer_commit.get(authorId);
					commits.add(1);
				}else {
					ArrayList<Integer> commits = new ArrayList<>();
					commits.add(1);
					developer_commit.put(authorId, commits);
				}
				count++;
			}
			int numOfCommit = count;
			
			int over100 = 0;
			System.out.println("pojectName : "+pojectName);
			TreeSet<String> NOC_dev = new TreeSet<>();
			for(String authorID : developer_commit.keySet()) {
				ArrayList<Integer> commits = developer_commit.get(authorID);
				System.out.println("authorID : "+authorID + "	commits : "+commits.size());
				NOC_dev.add(commits.size() + "		"+authorID);
				if(commits.size() >= 100) {
					over100++;
				}
			}
			
			NOC_dev.forEach(e -> System.out.println(e));
			
			System.out.println();
			
			if(over100 >= 10) {
				csvPrinterOver100.printRecord(pojectName,issueKey,githubAddress,Dev,numOfCommit,over100,commitTime.first(),commitTime.last());
			}else {
				csvPrinterLess100.printRecord(pojectName,issueKey,githubAddress,Dev,numOfCommit,commitTime.first(),commitTime.last());
			}
			
			System.out.println("pojectName : "+pojectName+"	over100 : "+over100);
			System.out.println("#######################################################");
			System.out.println();
		}
		
		csvPrinterOver100.close();
		csvPrinterLess100.close();
		writerOver100.close();
		writerLess100.close();
		
	}
	private static String parseAuthorID(String authorId) {
		Pattern pattern = Pattern.compile(".+\\[(.+|),\\s([^\\s]+)(\\s.+)?,.+\\]");
		Matcher matcher = pattern.matcher(authorId);
		if(matcher.find()) {
			authorId = matcher.group(2);
		}
		
		if(authorId.startsWith("PersonIdent")) {
			authorId = "Anonymous";
		}
		
		authorId = authorId.replace(" ", "_");
		return authorId;
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
	
	public static String getStringDateTimeFromCommitTime(int commitTime){
		SimpleDateFormat ft =  new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");
		Date commitDate = new Date(commitTime* 1000L);

		TimeZone GMT = TimeZone.getTimeZone("GMT");
		ft.setTimeZone(GMT);

		return ft.format(commitDate);
	}

}
