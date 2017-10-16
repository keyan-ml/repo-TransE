package util;

import java.io.*;

import java.util.ArrayList;

public class BuildTempWordLib {
	public static final String WORKPLACE_PATH = "..";
	public static final String TRANSE_ROOT_PATH = ".";
	public static final String SOURCE_FILE_PATH = TRANSE_ROOT_PATH + "/YuLiao/Pos/yuliao_pos.csv";
	public static final String TARGET_FILE_PATH = WORKPLACE_PATH + "/Util/word_library/temp_word_lib/Word_Library.wordlib";
	
	
	public static void build(String sourcePath, String targetPath) {
		try {
			InputStreamReader isr = new InputStreamReader(new FileInputStream(sourcePath), "UTF-8");
			BufferedReader bufr_source = new BufferedReader(isr);
			
			ArrayList<String> list = new ArrayList<String>();
			list.clear();
			
			String str = null;
			int column = 1;
			while((str = bufr_source.readLine()) != null) {
				String[] parts = str.split("\\|");
				
				if(parts[0].contains("null")) {
					parts[0] = "车";
				}

				for(int i = 0; i < 2; i++) {
					String word_temp = parts[i].trim();
					if(!list.contains(word_temp))
						list.add(word_temp);
				}
				
				System.out.println("processing column #" + column++);
			}
			
			
			OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(targetPath), "UTF-8");
			BufferedWriter bufw_target = new BufferedWriter(osw);
			
			column = 1;
			for(int i = 0; i < list.size(); i++) {
				bufw_target.write(list.get(i) + " " + column);
				bufw_target.flush();
				bufw_target.newLine();
				
				System.out.println("writing column #" + column++);
			}
			
			bufw_target.close();
			osw.close();
			
			System.out.println("Done");
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		build(SOURCE_FILE_PATH, TARGET_FILE_PATH);
		
	}

}
