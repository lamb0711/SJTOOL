package edu.handong.csee.isel.data;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

public class SortArff {
	static String projectName;
	
	private final static String firstcommitTimePatternStr = "'(\\d\\d\\d\\d-\\d\\d-\\d\\d\\s\\d\\d:\\d\\d:\\d\\d)'";
	private final static Pattern firstcommitTimePattern = Pattern.compile(firstcommitTimePatternStr);
	
	private final static String firstKeyPatternStr = "@attribute\\sKey\\s\\{([^,]+)";
	private final static Pattern firstKeyPattern = Pattern.compile(firstKeyPatternStr);
	
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		Pattern pattern = Pattern.compile("(.+)/(.+)-data.arff");
		
		Matcher matcher = pattern.matcher(args[0]);
		while(matcher.find()) {
			projectName = matcher.group(2);
		}
		
		ArrayList<String> attributeLineList = new ArrayList<>();
		ArrayList<String> dataLineList = new ArrayList<>();
	
		String content = FileUtils.readFileToString(new File(args[0]), "UTF-8");
		String[] lines = content.split("\n");
		
		//use this value when parsing developerID and commitTime in data line
		String firstCommitTime = null;
		int indexOfCommitTime = 0;
		//추가
		String firstKey = null;
		int indexOfKey = 0;
		//추가
		boolean dataPart = false;
		for (String line : lines) {
			if (dataPart) {
				dataLineList.add(line);
				continue;

			}else if(!dataPart){
				attributeLineList.add(line);
				//추가 - parsing first commit id 
				if(line.startsWith("@attribute Key {")) {
					Matcher m = firstKeyPattern.matcher(line);
					m.find();
					firstKey = m.group(1);
					indexOfKey = attributeLineList.size()-3;
				}
				//추가
				if(line.startsWith("@attribute meta_data-commitTime")) {
					Matcher m = firstcommitTimePattern.matcher(line);
					m.find();
					firstCommitTime = m.group(1);
					indexOfCommitTime = attributeLineList.size()-3;
				}
				if (line.startsWith("@data")) {
					dataPart = true;
				}
			}
		}
		
		TreeMap<String,ArrayList<String>> sortedData = new TreeMap<>();  //commitTime - data

		for(String line : dataLineList) { //data line 하나씩 읽기 
			String commitTime = parsingCommitTime(line,firstCommitTime,indexOfCommitTime);
			String data = parsingDataLine(line,indexOfCommitTime,indexOfKey);
			//추가- 각 feature에 해당하는 commit id 
			String commitid = parsingCommitHash(line,firstKey,indexOfKey);
			//추가
			ArrayList<String> dataLine;
			
			if(sortedData.containsKey(commitTime)) {
				dataLine = sortedData.get(commitTime);
				dataLine.add(data);
			}else {
				dataLine = new ArrayList<String>();
				dataLine.add(data);
				sortedData.put(commitTime, dataLine);
			}
		}
		
		//read sortedData
		for(String commitTime : sortedData.keySet()) {
			System.out.println(commitTime);
			
			ArrayList<String> dataLine = sortedData.get(commitTime);
			for(String data : dataLine) {
				System.out.println(data);
			}
		}
		
//		//save result to arff
//		File newDeveloperArff = new File("/Users/yangsujin/Desktop/sort" +File.separator+ projectName+"_sort.arff");
//		StringBuffer newContentBuf = new StringBuffer();
//		
//		//write attribute
//		for (String line : attributeLineList) {
//			if(line.startsWith("@attribute meta_data-commitTime"))continue;
//			newContentBuf.append(line + "\n");
//		}
//		
//		//write data
//		for(String commitTime : sortedData.keySet()) {
//			ArrayList<String> dataLine = sortedData.get(commitTime);
//			for(String data : dataLine) {
//				newContentBuf.append(data+ "\n");
//			}
//		}
//		
//		FileUtils.write(newDeveloperArff, newContentBuf.toString(), "UTF-8");
	}
	//추가
	
	private static String parsingCommitHash(String line, String firstKey, int indexOfKey) {
		String key = null;
	
		if((line.contains(","+indexOfKey+" "))) {
			key = line.substring(line.lastIndexOf(","),line.lastIndexOf("}"));
			key = key.substring(key.lastIndexOf(" ")+1,key.length());
		}else {
			key = firstKey;
		}
		return key.substring(0,key.indexOf("-"));
	}
	
	private static String parsingDataLine(String line, int indexOfCommitTime,int indexOfKey) {
		if((line.contains(","+indexOfKey+" "))) {
			if((line.contains(","+indexOfCommitTime+" "))) { //index previous,index commitTime, index key} 
				line = line.substring(0,line.lastIndexOf(","+indexOfCommitTime));
				line = line + "}";
				return line;
			}else {											//index previous,index key}
				line = line.substring(0,line.lastIndexOf(","+indexOfKey));
				line = line + "}";
				return line;
			}
		}else {
			if((line.contains(","+indexOfCommitTime+" "))) {//index previous,index commitTime} 
				line = line.substring(0,line.lastIndexOf(","+indexOfCommitTime));
				line = line + "}";
				return line;
			}else {											//index previous,index commitTime} 
				return line;
			}
		}
	}
	//추가 
	private static String parsingCommitTime(String line, String firstCommitTime, int indexOfCommitTime) {
		if((line.contains(","+indexOfCommitTime+" "))) {
			String commitTime = line.substring(line.lastIndexOf(indexOfCommitTime+" '"),line.lastIndexOf("'"));
			commitTime = commitTime.substring(commitTime.lastIndexOf("'")+1,commitTime.length());
			return commitTime;
		}else {
			return firstCommitTime;
		}
	}
	
	private static int calDateBetweenAandB(String preTime, String commitTime) throws Exception {

		SimpleDateFormat format = new SimpleDateFormat("yyyy-mm-dd");
        // date1, date2 두 날짜를 parse()를 통해 Date형으로 변환.
		
        Date FirstDate = format.parse(preTime);
        Date SecondDate = format.parse(commitTime);
        
        // Date로 변환된 두 날짜를 계산한 뒤 그 리턴값으로 long type 변수를 초기화 하고 있다.
        // 연산결과 -950400000. long type 으로 return 된다.
        long calDate = FirstDate.getTime() - SecondDate.getTime(); 
        
        // Date.getTime() 은 해당날짜를 기준으로1970년 00:00:00 부터 몇 초가 흘렀는지를 반환해준다. 
        // 이제 24*60*60*1000(각 시간값에 따른 차이점) 을 나눠주면 일수가 나온다.
        long calDateDays = calDate / ( 24*60*60*1000); 
 
        calDateDays = Math.abs(calDateDays);
//        System.out.println(commitTime);
//        System.out.println("두 날짜의 날짜 차이: "+calDateDays);
        
        return (int)calDateDays;
		
	}

}