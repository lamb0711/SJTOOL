package edu.handong.csee.isel.data;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.MultiSearch;
import weka.classifiers.meta.multisearch.DefaultEvaluationMetrics;
import weka.classifiers.meta.multisearch.DefaultSearch;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.LMT;
import weka.classifiers.trees.RandomForest;
import weka.core.AttributeStats;
import weka.core.Instances;
import weka.core.SelectedTag;
import weka.core.Utils;
import weka.core.converters.ConverterUtils.DataSource;

public class WekaClassify {

	static String args0;
	static String args1;
	
	public static void main(String[] args) throws Exception {
		String projectname = null;
		System.out.println(args[0]);
		System.out.println(args[1]);
		String projectNamePatternStr = ".+/(.+)\\.arff";
		Pattern projectNamePattern = Pattern.compile(projectNamePatternStr);
		Matcher m = projectNamePattern.matcher(args[0]);
		
		while(m.find()) {
			projectname = m.group(1);
		}

		try {
			DataSource source = new DataSource(args[0]);
			Instances Data = source.getDataSet();
			Data.setClassIndex(0);
			System.out.println(Data.classAttribute());
			
			AttributeStats attStats = Data.attributeStats(0);
			
//			DataSource testSource = new DataSource(args[3]);
//			Instances testData = testSource.getDataSet();
//			testData.setClassIndex(testData.numAttributes() - 1);
//			System.out.println(testData.classAttribute());
			
			ArrayList<String> algorithms = new ArrayList<String>(Arrays.asList("random","j48","lmt"));
			

			File resultDir = new File(args[1] +File.separator + projectname);
			resultDir.mkdir();
			String output = resultDir.getAbsolutePath();
					
			for(String algorithm : algorithms) {
			Classifier classifyModel = null;
			
			if(algorithm.compareTo("random") == 0) {
				classifyModel = new RandomForest();
			}else if(algorithm.compareTo("naive") == 0){
				classifyModel = new NaiveBayes();
			}else if(algorithm.compareTo("j48") == 0){
				classifyModel = new J48();
			}else if(algorithm.compareTo("bayesNet") == 0){
				classifyModel = new BayesNet();
			}else if(algorithm.compareTo("lmt") == 0){
				classifyModel = new LMT();
			}else if (algorithm.compareTo("ibk") == 0) {
				classifyModel = new IBk();
			}
			
//			classifyModel.buildClassifier(Data);
			
//			MultiSearch multi = new MultiSearch();
//
//		    SelectedTag tag = new SelectedTag(DefaultEvaluationMetrics.EVALUATION_AUC, new DefaultEvaluationMetrics().getTags());
//		    multi.setEvaluation(tag);
//		    multi.setAlgorithm(new DefaultSearch());
//		    
//		    multi.setClassifier(classifyModel);
//		    multi.buildClassifier(Data);
//			
//
			 // configure multisearch
//		    MathParameter conf = new MathParameter();
//		    conf.setProperty("confidenceFactor");
//		    conf.setBase(10);
//		    conf.setMin(0.05);
//		    conf.setMax(0.75);
//		    conf.setStep(0.05);
//		    conf.setExpression("I");
		    MultiSearch multi = new MultiSearch();
		    multi.setClassifier(classifyModel);
//		    multi.setSearchParameters(new AbstractParameter[]{
//		      conf
//		    });
		    multi.setAlgorithm(new DefaultSearch());
		    SelectedTag tag = new SelectedTag(
		      DefaultEvaluationMetrics.EVALUATION_AUC,
		      new DefaultEvaluationMetrics().getTags());
		    multi.setEvaluation(tag);
//		    multi.setAlgorithm(new DefaultSearch());
		    // output configuration
		    System.out.println("\nMultiSearch commandline:\n"+ Utils.toCommandLine(multi));

		    // optimize
		    System.out.println("\nOptimizing...\n");
		    multi.buildClassifier(Data);
		    System.out.println("Best setup:\n" + multi.getBestClassifier());
		    System.out.println("Best setup:\n" + Utils.toCommandLine(multi.getBestClassifier()));
		    System.out.println("Best parameter: " + multi.getGenerator().evaluate(multi.getBestValues()));
		    
		    Thread.sleep(5000);
			Evaluation evaluation = new Evaluation(Data);
			
			for(int i = 1; i < 11; i++) {
				evaluation.crossValidateModel(classifyModel, Data, 10, new Random(i));
				
		//		evaluation.evaluateModel(classifyModel, testData);
				
				BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(output +File.separator + projectname + "-" + algorithm + "-" +i+"-10-fold.txt")));
				
				String strSummary = evaluation.toSummaryString();
				String detail = evaluation.toClassDetailsString();
				
				bufferedWriter.write(Data.attribute(0).toString());
				bufferedWriter.write("\n");
				bufferedWriter.write(attStats.toString());
				bufferedWriter.write(strSummary);
				bufferedWriter.write(detail);
				bufferedWriter.close();
				}
			}
			
			System.out.println("Finish "+projectname);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
