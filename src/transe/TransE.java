// 编码 utf-8
package transe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;
import java.lang.Math;
import java.io.*;

import nlp.NlpirTest;

public class TransE {
	public static final String WORKPLACE_PATH = "..";
	public static final String TRANSE_ROOT_PATH = ".";
	public static final String WORD_LIB_PATH = WORKPLACE_PATH + "/Util/word_library/temp_word_lib/Word_Library.wordlib";
	// public static final String wordVecPath = WORKPLACE_PATH + "/Util/word_library/transe/yuliao_vector_utf_8.txt";
	public static final String RELATION_VECTOR_PATH = TRANSE_ROOT_PATH + "/model/relation2vec.bern";
	public static final String ENTITY_VECTOR_PATH = TRANSE_ROOT_PATH + "/model/entity2vec.bern";
	// public static final String relation_vec_path = "";

	// TransE 参数
	public static final int STRING_WIDTH = 8;

	public static final int VECTOR_N = 10;	// 嵌入维度
	public static final int MARGIN = 3; // 阈值
	// TransE 参数


	private HashMap<String, String> wordLib_map;
	// private HashMap<String, double[]> wordVec_map;
	private HashMap<String, double[]> entity_id2vec_map;
	private double[] relation_vec;


	public TransE() {
		this.wordLib_map = loadWordLibMap(WORD_LIB_PATH);
		this.entity_id2vec_map = loadEntityId2VecMap(ENTITY_VECTOR_PATH);
		this.relation_vec = loadRelationVector(RELATION_VECTOR_PATH);

		System.out.println("TransE has been called...");
	}

//	public double rand(double min, double max) {
//		return min + (max - min) * Math.random();
//	}
//	// 返回x的概率密度 
//	public double normal(double x, double miu,double sigma) {
//		return 1.0 / Math.sqrt(2 * Math.PI) / sigma * Math.pow(Math.E, -1 * (x - miu) * (x - miu) / (2 * sigma * sigma));
//	}
//	// 返回一个大于或等于均值miu的概率密度并且属于[min,max]的数
//	public double randn(double miu,double sigma, double min ,double max) {
//		double x, y, dScope;
//		do{
//			x = rand(min, max);
//			y = normal(x, miu, sigma);
//			dScope = rand(0.0, normal(miu, miu, sigma));
//		}while(dScope > y);
//		return x;
//	}
	// 限制实体向量a的模在1以内
    public void norm(double[] a) {
        double x = vec_len(a);
        if (x > 1)
        for (int ii = 0; ii < a.length; ii++)
                a[ii] /= x;
        // return a;
    }
	// 限制实体向量a的模在1以内
	// 返回向量a的模
	public double vec_len(double[] a) {
		double res = 0;
		for (int i = 0; i < a.length; i++)
			res += a[i] * a[i];
		return Math.sqrt(res);
	}
	// 返回向量a的模
	
    public void process(String sent)
    {
        int n = VECTOR_N;		// 嵌入维数
		int margin = MARGIN;	// 阈值
		
		try {
			// initialize entity_vec
			String[] entity = getNoun(sent);
			String[] entity_id = new String[entity.length];
			double[][] entity_vec = new double[entity.length][n];
			for(int i = 0; i < entity.length; i++) {
				if(!wordLib_map.containsKey(entity[i])) {
					// the word is not in wordLib, then add it into the word lib
					System.out.println("Error: no such word in wordLib >_> \"" + entity[i] + "\"");
					// OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(WORD_LIB_PATH, true)); // append
					// BufferedWriter bufw_add = new BufferedWriter(osw);
					// bufw_add.write(entity[i] + " " + (wordLib_map.keySet().size() + 1));
					// bufw_add.flush();
					// bufw_add.newLine();
					// bufw_add.close();
					// osw.close();
					
					// wordLib_map = loadWordLibMap(WORD_LIB_PATH);
				} // if
				
				// entity_id[i] = wordLib_map.get(entity[i]);
				String temp = String.valueOf(wordLib_map.get(entity[i]));
				int restTimes = STRING_WIDTH - temp.length();
				for(int j = 0; j < restTimes; j++)
					temp = "0" + temp;
				entity_id[i] = temp;
			} // for
			
			// 对实体向量进行归一化处理
			for (int i = 0; i < entity_vec.length; i++) {
				// entity_vec[i] = wordVec_map.get(entity[i]);

				entity_vec[i] = entity_id2vec_map.get(entity_id[i]);
				// 限制每个实体向量的模在1以内
				norm(entity_vec[i]);
			}
			// initialize entity_vec
			
			
			boolean flag = false;
			ArrayList<String> mList = new ArrayList<String>();
			mList.clear();
			for(int i = 0; i < entity_vec.length; i++) {
				for(int j = 0; j < entity_vec.length; j++) {
					if(i != j) {
						double dist = calc_sum(entity_vec[i], entity_vec[j], relation_vec);
						if(dist < MARGIN) {
							flag = true;
							mList.add(entity[i] + " " + entity[j]);
						}
						
						System.out.println(entity[i] + ", " + entity[j] + " " + dist);
					}
				}
			}
			if(flag) {
				System.out.println("\nThere they are : ");
				for(int i = 0; i < mList.size(); i++) {
					String[] temp = mList.get(i).split(" ");
					System.out.println(temp[0] + ", " + temp[1]);
				}
				
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	// 计算实体e2和e1+rel的距离
    public double calc_sum(double[] e1, double[] e2, double[] rel)
    {
        double sum = 0;
        for(int ii = 0; ii < e1.length; ii++)
            	 sum += Math.abs(e2[ii] - e1[ii] - rel[ii]);//L1距离
//           	sum += Math.pow(e2[ii] - e1[ii] - rel[ii], 2);
//		sum = Math.sqrt(sum);
        // if(L1_flag)
        	// for(int ii=0; ii<n; ii++)
            	// sum+=fabs(entity_vec[e2][ii]-entity_vec[e1][ii]-relation_vec[rel][ii]);//L1距离
        // else
        	// for(int ii=0; ii<n; ii++)
            	// sum+=sqr(entity_vec[e2][ii]-entity_vec[e1][ii]-relation_vec[rel][ii]);//L2距离
        return sum;
    }
	// 计算实体e2和e1+rel的距离
	
    // 获取一个句子中的所有名词
	public String[] getNoun(String sent) {
		NlpirTest nlpir = new NlpirTest();
		
		Set<String> all_word = this.wordLib_map.keySet();
		for(String word : all_word) {
			nlpir.mAddUserWord(word, "n");
		}
		
		ArrayList<String> list = new ArrayList<String>();
		list.clear();
		
		sent = nlpir.mFenCi(sent);
		String[] words = sent.split(" ");
		for(String w :words) {
			if(!w.equals("") && w.contains("/n"))
				list.add(nlpir.adjust_fenci(w));
				
		}
		
		String[] temp = new String[list.size()];
		for(int i = 0; i < temp.length; i++)
			temp[i] = list.get(i);
		
		nlpir.exit();
		return temp;
	}
    // 获取一个句子中的所有名词
	
	// load word_lib_map
	public HashMap<String, String> loadWordLibMap(String wordLibPath) {
		HashMap<String, String> wordLib_map_temp = new HashMap<String, String>();
		wordLib_map_temp.clear();
		
		try {
			InputStreamReader isr = new InputStreamReader(new FileInputStream(wordLibPath), "UTF-8");
			BufferedReader bufr_wordLib = new BufferedReader(isr);
		
			String word = null;
			int word_id = 1;
			while((word = bufr_wordLib.readLine()) != null) {
				if(!word.equals("")) {
					String[] strs = word.split(" ");
					wordLib_map_temp.put(strs[0], strs[1]);

	        		System.out.println("load vector #" + word_id++);
				} // if
			} // while
			
		} catch(Exception e) {
			System.out.println("Error: cannot load word library ... details are as follows:");
			e.printStackTrace();
		}
		return wordLib_map_temp;
	}
	// load word_lib_map
	
	// load entity_vec ************
	public HashMap<String, double[]> loadEntityId2VecMap(String entity_vector_path) {
		try {
			HashMap<String, double[]> entity_id2vec_map = new HashMap<String, double[]>();
			entity_id2vec_map.clear();

			InputStreamReader isr = new InputStreamReader(new FileInputStream(entity_vector_path), "UTF-8");
			BufferedReader bufr_entity = new BufferedReader(isr);
			String str = null;
			while((str = bufr_entity.readLine()) != null) {
				if(!str.equals("")) {
					String[] strs = str.split("\t");
					String entity_temp = strs[0];
					double[] entity_vec_temp = new double[VECTOR_N];
					for(int i = 0; i < entity_vec_temp.length; i++) {
						entity_vec_temp[i] = Double.parseDouble(strs[i + 1]);
					}
					entity_id2vec_map.put(entity_temp, entity_vec_temp);
				}
			}
			
			return entity_id2vec_map;
		} catch(Exception e) {
			e.printStackTrace();
		}

		return null;
	}
	// load entity_vec ************
	
	// load relation_vec ************
	public double[] loadRelationVector(String relation_vector_path) {
		try {
			double[] relation_vec = new double[VECTOR_N];

			InputStreamReader isr = new InputStreamReader(new FileInputStream(relation_vector_path), "UTF-8");
			BufferedReader bufr_rel = new BufferedReader(isr);
			String str = bufr_rel.readLine();
			String[] relation_vec_strs = str.split("\t");
			for(int i = 0; i < relation_vec.length; i++) {
				relation_vec[i] = Double.parseDouble(relation_vec_strs[i + 1]);
			}
			
			return relation_vec;
		} catch(Exception e) {
			e.printStackTrace();
		}

		return null;
	}
	// load relation_vec end ********
	

	// public HashMap<String, double[]> loadWordVecMap(String wordVecPath) {
	// 	HashMap<String, double[]> wordVec_map_temp = new HashMap<String, double[]>();
	// 	wordVec_map_temp.clear();
		
	// 	try {
	// 		BufferedReader bufr_wordVec = new BufferedReader(new FileReader(wordVecPath));
		
	// 		String word = null;
	// 		// 跳过两行
	// 		bufr_wordVec.readLine();
	// 		bufr_wordVec.readLine();
	// 		// 跳过两行
	// 		int word_id = 3;
	// 		while((word = bufr_wordVec.readLine()) != null && !word.equals("")) {
	// 			String[] strs = word.split(" ");

	// 			String entity = strs[0];
	// 			double[] entity_vec = new double[VECTOR_N];
	// 			for(int i = 0; i < entity_vec.length; i++) {
	// 				entity_vec[i] = Double.parseDouble(strs[i + 1]);
	// 			}

	// 			wordVec_map_temp.put(entity, entity_vec);

    //     		System.out.println("load vector #" + word_id++);
	// 		}
			
	// 	} catch(Exception e) {
	// 		System.out.println("Error: cannot load word Vector ... details are as follows:");
	// 		e.printStackTrace();
	// 	}
	// 	return wordVec_map_temp;
	// }
	
	
	public static void main(String[] args) {
		TransE transe_tool = new TransE();

		System.out.print("Input here: \n(quit with 'EXIT') >_> ");
		Scanner input = new Scanner(System.in);
		String sInput = input.next();
		while(!sInput.equals("EXIT")) {
			transe_tool.process(sInput);
			
			System.out.print("\n(quit with 'EXIT') >_> ");
			sInput = input.next();
		}
		
	}
}
