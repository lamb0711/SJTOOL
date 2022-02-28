package edu.handong.csee.isel.gumtree;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.collections4.IterableUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import com.github.gumtreediff.actions.ActionGenerator;
import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.client.Run;
import com.github.gumtreediff.gen.python.PythonTreeGenerator;
import com.github.gumtreediff.matchers.Matchers;
import com.github.gumtreediff.matchers.optimal.rted.InfoTree;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.matchers.Matcher;

public class Gumtree {
	//args[0] : githubURL
	//args[1] : output
	//args[2] : pkl file for commitHash
	String githubURL;
	String projectName;
	String output;
	Repository repo;
	
	public static void main(String[] args) throws Exception {
		Gumtree main = new Gumtree();
		main.run(args);
	}
	
	private void run(String[] args) throws Exception {
		githubURL = args[0];
		output =  args[1];
		projectName = getProjectName(githubURL);
		
		List<RevCommit> commitList = getAllCommitList();
		Git git = Git.open(getGitDirectory());
		repo = git.getRepository();
		
		int i = 0; 
		for (RevCommit commit : commitList) {
			System.out.println(commit.getId().getName());
			
			if (commit.getParentCount() < 1) {
				System.err.println("WARNING: Parent commit does not exist: " + commit.name());
				continue;
			}
			
			RevCommit parent = commit.getParent(0);
			
			List<DiffEntry> diffs = Utils.diff(parent, commit, repo);

			for (DiffEntry diff : diffs) {
				
				String oldPath = diff.getOldPath();
				String newPath = diff.getNewPath();
				
				System.out.println(newPath);
				
				String prevFileSource = Utils.fetchBlob(repo, commit.getId().getName() + "~1", oldPath);
				String fileSource = Utils.fetchBlob(repo, commit.getId().getName(), newPath);
				
				List<Action> vector = null;
				
				try {
					vector = getCharacteristicVector(prevFileSource, fileSource);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		System.out.println(vector);
		for (Action element : vector) {
			System.out.println(element.getName() +" " +String.valueOf(element.getNode().getType()));
			System.out.println(element.getNode().toTreeString());
		}
//				for (Action element : vector) {
//					switch (element.getName()) {
//					case "INS":
//					case "DEL":
//					case "UPD":
//					case "MOV":
//
//						System.out.println(element.getName() +" " +String.valueOf(element.getNode().getType()));
//						System.out.println(element.getNode().toTreeString());
//						
//						
//						System.out.println();
//						break;
//
//					default:
//						continue;
//					}
//				}
			}
			if(i > 50) 
				break;
			i++;
			System.out.println();
		}
		
	}
	
	public List<Action> getCharacteristicVector(String prevFileSource, String fileSource) throws IOException {

		Run.initGenerators();

		ITree src = new PythonTreeGenerator().generateFromString(prevFileSource).getRoot();
		ITree dst = new PythonTreeGenerator().generateFromString(fileSource).getRoot();

		Matcher m = Matchers.getInstance().getMatcher(src, dst); // retrieve the default matcher
		m.match();

		ActionGenerator g = new ActionGenerator(src, dst, m.getMappings());
		g.generate();

		List<Action> actions = g.getActions();
		return actions;
	}
	
	File GitClone() throws InvalidRemoteException, TransportException, GitAPIException {
		String remoteURI = githubURL;
		File clonedDirectory = getGitDirectory();
		clonedDirectory.mkdirs();
		System.out.println("cloning " + projectName + "...");
		Git git = Git.cloneRepository().setURI(remoteURI).setDirectory(clonedDirectory).setCloneAllBranches(true)
				.call();
		System.out.println("done");
		return git.getRepository().getDirectory();
	}
	
	String getReferencePath() {
		return output + File.separator + "reference";
	}

	File getGitDirectory() {
		String referencePath = getReferencePath();
		File clonedDirectory = new File(
				referencePath + File.separator + "repositories" + File.separator + projectName);
		return clonedDirectory;
	}
	
	List<RevCommit> getAllCommitList() throws InvalidRemoteException, TransportException, GitAPIException, IOException{
		File gitDirectory = null;
		if (isCloned() && isValidRepository()) {
			gitDirectory = getGitDirectory();
		} else if (isCloned() && (!isValidRepository())) {
			File directory = getGitDirectory();
			directory.delete();
			gitDirectory = GitClone();
		} else {
			gitDirectory = GitClone();
		}
		return getCommitListFrom(gitDirectory);
		
	}
	
	List<RevCommit> getCommitListFrom(File gitDir) throws IOException, NoHeadException, GitAPIException {
		Git git = Git.open(gitDir);
		Iterable<RevCommit> walk = git.log().all().call();
		List<RevCommit> commitList = IterableUtils.toList(walk);

		return commitList;
	}
	
	private boolean isCloned() {
		File clonedDirectory = getGitDirectory();
		return clonedDirectory.exists();
	}
	
	boolean isValidRepository() {
		File directory = getGitDirectory();
		try {
			Git git = Git.open(directory);  //여기가 쓰이는데 왜안쓰인다고 뜨는지 모르겠다.
			return true;
		} catch (IOException e) {
			return false;
		}
	}
	
	public static String getProjectName(String gitURI) {
		String projectName = gitURI.substring(gitURI.lastIndexOf(File.separator)+1, gitURI.length());
		return projectName;

	}

}
