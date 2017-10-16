// 编码 utf-8
package util;

import java.io.*;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Set;

// import com.sun.corba.se.pept.encoding.OutputObject;

public class Prog2_train {
	public static final String WORKPLACE_PATH = "..";
	public static final String TRANSE_ROOT_PATH = ".";
	public static final String WORD_LIB_PATH = WORKPLACE_PATH + "/Util/word_library/temp_word_lib/Word_Library.wordlib";
	public static final String SOURCE_FILE_PATH = TRANSE_ROOT_PATH + "/YuLiao/Table/transe_table.csv";
	public static final String TARGET_FILE_PATH = TRANSE_ROOT_PATH + "/YuLiao/InputFile/train.txt";
		
	public static final int STRING_WIDTH = 8;
	
	public static HashMap<String, String> loadWordLibMap(String wordLibPath) {
		HashMap<String, String> wordLib_map = new HashMap<String, String>();
		wordLib_map.clear();
		
		try {
			InputStreamReader isr = new InputStreamReader(new FileInputStream(wordLibPath), "UTF-8");
			BufferedReader bufr_wordLib = new BufferedReader(isr);
		
			String word = null;
			// int word_id = 1;
			while((word = bufr_wordLib.readLine()) != null) {
				if(!word.equals("") && word != null) {
					String[] strs = word.split(" ");
					wordLib_map.put(strs[0], strs[1]);
				}
			}
			
		} catch(Exception e) {
			System.out.println("Error: cannot load word library ... details are as follows:");
			e.printStackTrace();
		}
		return wordLib_map;
	}
	
	public static void main(String[] args) {
		try {
			HashMap<String, String> wordLib_map = loadWordLibMap(WORD_LIB_PATH);
			
			BufferedWriter bufw = new BufferedWriter(new FileWriter(TARGET_FILE_PATH));
			BufferedReader bufr = new BufferedReader(new FileReader(SOURCE_FILE_PATH));
			
			String str = null;
			int column = 1;
			while((str = bufr.readLine()) != null) {
				String[] parts = str.split("\\|");
				String line = "";
				for(int i = 0; i < 2; i++) {
					parts[i] = parts[i].trim();
					
					if(!wordLib_map.containsKey(parts[i])) {
						// parts[i] is not in wordLib, then add it into the word lib
						System.out.println("Error: no such word in wordLib ... " + parts[i]);
						System.out.print("Press any Key and Enter to continue...");
						Scanner input = new Scanner(System.in);
						input.next();
						
						OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(WORD_LIB_PATH, true), "UTF-8"); // append
						BufferedWriter bufw_add = new BufferedWriter(osw);
						bufw_add.write(parts[i] + " " + (wordLib_map.keySet().size() + 1));
						bufw_add.flush();
						bufw_add.newLine();
						bufw_add.close();
						osw.close();
						
						wordLib_map = loadWordLibMap(WORD_LIB_PATH);
					} // if
					else {
						String temp = String.valueOf(wordLib_map.get(parts[i]));
						int restTimes = STRING_WIDTH - temp.length();
						for(int j = 0; j < restTimes; j++)
							temp = "0" + temp;
						line += temp + " ";
						// list.add(temp);
					}
				} // for
				
				
				bufw.write(line + "_has_a");
				bufw.flush();
				bufw.newLine();
					
				System.out.println("processing and writing column #" + column++);
			} // while

			bufw.close();
			
			System.out.println("train.txt Done");
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}
}
