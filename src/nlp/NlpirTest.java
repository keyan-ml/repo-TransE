package nlp;

import java.io.UnsupportedEncodingException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.ArrayList;

import com.sun.jna.Library;
import com.sun.jna.Native;

public class NlpirTest {
	public static final String workplacePath = "..";
	public static final String rootPath = workplacePath + "/Util/NLPIR";
	// public static final String system_charset = "GBK";
	public static final String system_charset = "UTF-8";

	//com.sun.jna.Library
	public interface CLibrary extends Library {
		CLibrary Instance = (CLibrary) Native.loadLibrary(
				rootPath + "/lib/NLPIR", CLibrary.class);
		// CLibrary Instance = (CLibrary) Native.loadLibrary(
				// rootPath + "/lib/NLPIR", CLibrary.class);
		
		public int NLPIR_Init(String sDataPath, int encoding,
				String sLicenceCode);
		
		public String NLPIR_ParagraphProcess(String sSrc, int bPOSTagged);

		public int NLPIR_AddUserWord(String sWord);
		public int NLPIR_DelUsrWord(String sWord);
		
		public String NLPIR_GetLastErrorMsg();
		
		public void NLPIR_Exit();
	}
	
	public NlpirTest() {
		String argu = rootPath;
		int charset_type = 1;
		
		int init_flag = CLibrary.Instance.NLPIR_Init(argu, charset_type, "0");
		String nativeBytes = null;

		if (0 == init_flag) {
			// statue = init_flag;
			nativeBytes = CLibrary.Instance.NLPIR_GetLastErrorMsg();
			System.err.println("初始化失败！fail reason is " + nativeBytes);
			return;
		}
	}
	
	public String transString(String aidString, String ori_encoding, String new_encoding) {
		try {
			return new String(aidString.getBytes(ori_encoding), new_encoding);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void mAddUserWord(String word, String ci_xing) {
		try {
			CLibrary.Instance.NLPIR_AddUserWord(word + " " + ci_xing);

		} catch (Exception e) {
			System.out.println("Error: cannot add user word");
			e.printStackTrace();
		}
	}
	
	public void mDelUserWord(String word) {
		try {
			CLibrary.Instance.NLPIR_DelUsrWord(word);

		} catch (Exception e) {
			System.out.println("Error: cannot delete user word ... " + word);
			e.printStackTrace();
		}
	}
	
	public String mFenCi(String sInput) {
		try {
			String nativeBytes = CLibrary.Instance.NLPIR_ParagraphProcess(sInput, 1);
			
			// statue = 1;
			return nativeBytes;

		} catch (Exception e) {
			// statue = 0;
			return "出错";
		}
		
	}
	
	public String adjust_fenci(String sInput) {
		String[] sInputs = sInput.split(" ");
		String sResult = "";
		for(int i = 0; i < sInputs.length; i++) {
			if(sInputs[i].contains("/")) {
				String str = sInputs[i].substring(0, sInputs[i].indexOf("/"));
				sResult = sResult + str + " ";
			}
		}
		sResult = sResult.trim();
		return sResult;
	}
	
	public String mQuTYC(String sInput) {
		String sResult = "";
		try {
			//分词结果放进一个字符串数组
			String[] sResultArr = sInput.split(" ");

			//读取停用词表 和 保存去掉的词的变量声明，需要对应路径
			BufferedReader bufr = new BufferedReader(new FileReader(rootPath + "/mdata/stop_word_UTF_8.txt"));

			ArrayList<String> stopWords = new ArrayList<String>();
			stopWords.clear();

			String str = null;
			//装载停用词表，也就是把停用词全都读进内存
			while((str = bufr.readLine()) != null) {
				stopWords.add(str);
			}

			
			//逐个匹配刷停用词
			str = null;
			for(int i = 0; i < sResultArr.length; i++) {
				// str = sResultArr[i];
				// for(String stopWord : stopWords) {
					// System.out.println(str + ", " + stopWord);
					// if(str.contains(stopWord)) {
						// sResultArr[i] = null;
					// }
				// }
				if(stopWords.contains(sResultArr[i]))
					sResultArr[i] = null;
			}

			//临时输出去词之后的分词结果，要保存还是干撒再索
			sResult = "";
			for(int i = 0; i < sResultArr.length; i++) {
				if(sResultArr[i] != null) {
					sResult += sResultArr[i] + " ";
				}
			}
			return sResult.trim();

		} catch (FileNotFoundException e) {
			sResult = "出错 : 无法加载停用词表";
		} catch (Exception e) {
			sResult = "出错";
		}

		return sResult;
	}
	
	public void exit() {
		CLibrary.Instance.NLPIR_Exit();
	}
	
	
	
	
	
	//*****************************************
	// public static void main(String[] args) {
		// String argu = "H:/fenci";
		// String system_charset = "GBK";//GBK----0
		// String system_charset = "UTF-8";
		// int charset_type = 1;
		
		// int init_flag = CLibrary.Instance.NLPIR_Init(argu, charset_type, "0");
		// String nativeBytes = null;

		// if (0 == init_flag) {
			// statue = init_flag;
			// String ErrorMsg = CLibrary.Instance.NLPIR_GetLastErrorMsg();
			// System.err.println("初始化失败！fail reason is " + ErrorMsg);
			// return;
		// }

		// try {
			// FileReader fr = new FileReader(
					// "H:/zwm12/Documents/科研训练/语料库/workplace/SecondPile/Neg_YuLiao/Neg_YuLiao.txt");
			// BufferedReader bufr = new BufferedReader(fr);
			// FileWriter fw = new FileWriter(
					// "H:/zwm12/Documents/科研训练/语料库/workplace/SecondPile/Neg_YuLiao/Neg_YuLiao_FenCi.txt");
			// BufferedWriter bufw = new BufferedWriter(fw);
			
			// String str = null;
			// while((str = bufr.readLine()) != null) {
				// String temp = CLibrary.Instance.NLPIR_ParagraphProcess(str, 1);
				// System.out.println(temp);
				// bufw.write(temp);
				// bufw.flush();
				// bufw.newLine();
			// }
			// bufw.close();
			// fw.close();
			// System.out.println("Done");
			
			// CLibrary.Instance.NLPIR_Exit();
			
			// statue = 1;

		// } catch (Exception e) {
			// statue = 0;
			// e.printStackTrace();
		// }
	// }

}
