package edu.handong.csee.isel.data;

import java.io.File;

import weka.core.Attribute;
import weka.core.Instances;
import weka.core.Instance;
import weka.core.converters.ArffSaver;
import weka.core.converters.ConverterUtils.DataSource;

public class AdjustWekaFile {
	//arge[0] : arff file
	//output : same arff file
	public static void main(String[] args) throws Exception {
		DataSource source = new DataSource(args[0]);
		Instances data = source.getDataSet();
		data.setClassIndex(0);
		
		Attribute numOfBIC = data.attribute("meta_data-numOfBIC");
		
		for(Instance instance : data) {
			if(instance.value(numOfBIC) > 0) {
				instance.setValue(numOfBIC, instance.value(numOfBIC)-1);
			}
		}
		
		ArffSaver saver = new ArffSaver();
		saver.setInstances(data);
		saver.setFile(new File(args[0]));
		saver.writeBatch();
	}

}
