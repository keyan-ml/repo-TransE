// 编码 utf-8
package util;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class GetTable {
	public static final String WORKPLACE_PATH = "..";
	public static final String TRANSE_ROOT_PATH = ".";
	public static final String SOURCE_FILE_PATH = TRANSE_ROOT_PATH + "/YuLiao/Pos/yuliao_pos.csv";
	public static final String TARGET_FILE_PATH = TRANSE_ROOT_PATH + "/YuLiao/Table/transe_table.csv";
	
	public static void main(String[] args) {
		try {
			InputStreamReader isr = new InputStreamReader(new FileInputStream(SOURCE_FILE_PATH), "UTF-8");
			BufferedReader bufr = new BufferedReader(isr);
			
			OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(TARGET_FILE_PATH), "UTF-8");
			BufferedWriter bufw = new BufferedWriter(osw);
			
			ArrayList<String> list = new ArrayList<String>();
			list.clear();
			
			String str = "";
			int column = 1;
			while((str = bufr.readLine()) != null) {
				if(!str.equals("")) {
					String[] parts = str.split("\\|");
					for(int i = 0; i < parts.length; i++) {
						parts[i] = parts[i].trim();
					}
					
					if(parts[0].contains("null")) {
						parts[0] = "车";
					}

					if(parts.length > 2) {
						if(!list.contains(parts[0] + "|" + parts[1] + "|" + parts[2])) {
								list.add(parts[0] + "|" + parts[1] + "|" + parts[2]);
						}
					}
					else {
						System.out.print("Error: wrong format >_> " + str);
						// pause, and "press any key to continue..."
						Scanner input = new Scanner(System.in);
						input.next();
					}
				}
				
				System.out.println("processing column #" + column++);
			}
			
			// the first line: entity1|entity2|senword
			// bufw.write("entity1|entity2|senword");
			// bufw.flush();
			// bufw.newLine();
			for(int i = 0; i < list.size(); i++) {
				bufw.write(list.get(i));
				bufw.flush();
				bufw.newLine();
				
				System.out.println("writing column #" + i + "\t" + list.get(i));
			}
			bufw.close();
			
			System.out.println("Done");
		} catch(Exception e) {
			System.out.println("Error: unknown error as following: ");
			e.printStackTrace();
		}
		
	}
}