// 编码 utf-8
import nlp.*;

public class Test {
	public static void main(String[] args) {
		String sent = "奥迪的外观真好看";
		NlpirTest nlpir = new NlpirTest();
		
		
		try {
			sent = nlpir.mFenCi(sent);
			System.out.println("fenci:\t" + sent);
			
			System.out.print("/n: ");
			String[] words = sent.split(" ");
			for(String w :words) {
				if(w.contains("/n"))
					System.out.print(nlpir.adjust_fenci(w) + " ");
				
			}
			System.out.println();
			
			// sent = nlpir.adjust_fenci(sent);
			// System.out.println("adjust:\t" + sent);
			
			// sent = nlpir.mQuTYC(sent);
			// System.out.println("QuTYC:\t" + sent);
			
			
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		nlpir.exit();
	}
}