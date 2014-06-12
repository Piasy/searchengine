package search;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Similarity;

import gbt.*;

public class BM25MonoScorer extends Scorer{
	private IndexReader reader;
	private int fields_num;
	private String[] fields;
	private float[] boosts;
	private Term[] terms;
	private TermDocs termDocs[];
	private boolean valid[];
	private float avgLen[];
	private float[] idf;
	private int doc;
	private float field_score[];
	static private Gbrt scoreGbrt;
	final private float K1 = 2.0f;
	final private float b = 0.75f;
	//static final private double vacancyNum = 0;
	static final private double vacancyNum = 1350000000000.0;
	//FileOutputStream MyWriter;
	
	static public void trainGbrt(){
		int num = 110;
		double[][] x = new double [num][7];
		double[] y = new double[num];
		String filename  = "data/train.txt";
		
		try{
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filename)), "UTF-8"));
			String cur;
			int count = 0;
			while ((cur = reader.readLine()) != null){
				if (cur.substring(0, 1).equals("#")){
					continue;
				}
				//System.out.println(cur);
				String[] parts = cur.split(" ");
				for (int i = 0; i < 7; i++){
					x[count][i] = new Double(parts[i]);
				}
				if (x[count][5] == 0){
					x[count][5] = vacancyNum;
				}
				
				//System.out.println(parts[7]);
				y[count] = new Double(parts[7]);
				
				count += 1;
				//System.out.println("ok");
			}
			reader.close();
		}catch (Exception e) {
			e.printStackTrace();
			System.out.println("Input Error...");
		}
		
		scoreGbrt = new Gbrt();
		scoreGbrt.trainParameters(x, y);
		
		try {
			Gbrt.writeToFile(scoreGbrt, "data/ScoreGbrt.txt");
		} catch (IOException e) {
			System.out.println("Gbrt witeToFile Error..");
			e.printStackTrace();
		}
	}
	
	static public void loadGbrt(){
		String filename = "data/ScoreGbrt.txt";
		
		try {
			scoreGbrt = Gbrt.readFromFile(filename);
		} catch (Exception e) {
			System.out.println("Gbrt readFromFile Error..");
			e.printStackTrace();
		}
	}
	
	public BM25MonoScorer(IndexReader reader, String word, String[] fields, float[] boosts, float[] avgLen, Similarity similarity){
		super(similarity);		
		fields_num = fields.length;
		this.reader = reader;
		this.fields = fields;
		this.boosts = boosts;
		this.terms = new Term[fields_num];
		this.termDocs = new TermDocs[fields_num];
		this.valid = new boolean[fields_num];
		this.avgLen = avgLen;
		this.field_score = new float[fields_num];
		this.idf = new float[fields_num];
		
		try{
			for(int i = 0; i < fields_num; i++){
				//System.out.println("cur field "+fields[i]+' '+word);
				terms[i] = new Term(fields[i], word);
				this.termDocs[i] = reader.termDocs(terms[i]);
				idf[i] = similarity.idf(reader.docFreq(terms[i]), reader.numDocs());
				valid[i] = termDocs[i].next();
			}
		} catch(Exception e){
			e.printStackTrace();
		}
		
		/*try {
			MyWriter = new FileOutputStream(new File("tmp.txt"), true);
		} catch (FileNotFoundException e) {
			System.out.println("MyWriter Failed..");
			e.printStackTrace();
		}*/
		
		//trainGbrt();
	}
	
	private float calPR(float cur){
		if (cur >= 1e-6) return (1f);
		//System.out.println((int)Math.log10(1e-6));
		int t = (int)Math.log10(cur);
		double ans = Math.exp(-(double)(t + 7));
		return (float)ans;
	}
	
	private float calTime(long cur){
		if (cur == 0){
			return (0.1f);
		}
		
		long yearTime = 1000 * 60 * 60 * 24 * 365l;
		long curYear = (System.currentTimeMillis() - cur) / yearTime;
		if (curYear == 0) return (1f);
		if (curYear == 1) return (0.5f);
		double ans = 0.3 * Math.exp(-(double)(curYear - 2));
		return (float)ans;
	}
	
	@Override
	public float score() throws IOException {
		try{
			//float relev = 0;
			Document document = reader.document(doc);
			//String outString;
			double[] cur = new double[7];
			//System.out.println("type field:"+document.get("type"));
			for(int i = 0; i < fields_num; i++){
				if(termDocs[i].doc() == docID()){
					float len = document.getField(fields[i]).stringValue().length();
					float tf = termDocs[i].freq();		
					float fscore = (K1 + 1) * tf / (K1 * (1 - b + b * len / avgLen[i]) + tf);
					cur[i] = (double)fscore;
					//relev += fscore * boosts[i];
					
					//outString = fields[i] + " : " + Float.toString(fscore) + '\n';
					//outString = Float.toString(fscore) + ' ';
					//MyWriter.write(outString.getBytes("UTF-8"));
				} else {
					cur[i] = 0;
					//outString = fields[i] + " : " + Float.toString(0) + '\n';
					//outString = Float.toString(0) + ' ';
					//MyWriter.write(outString.getBytes("UTF-8"));
					//field_score[i] = 0;
				}
			}
			//outString = "time = " + document.getField("time").stringValue() + '\n';
			//outString = document.getField("time").stringValue() + ' ';
			//MyWriter.write(outString.getBytes("UTF-8"));
			cur[5] = new Double(document.getField("time").stringValue());
			if (cur[5] == 0){
				cur[5] = vacancyNum;
			}
			
			//outString = "pagerank = " + document.getField("page_rank").stringValue() + '\n';
			//outString = document.getField("page_rank").stringValue() + '\n';
			//MyWriter.write(outString.getBytes("UTF-8"));
			cur[6] = new Double(document.getField("page_rank").stringValue());
			
			//outString = document.get("title") + " " + document.get("url") + '\n' + '\n';
			//MyWriter.write(outString.getBytes("UTF-8"));
			
			//System.out.println("url = " + document.getField("url").stringValue());
			//System.out.println("res = " + res);
			//System.out.println("pagerank = " + document.getField("page_rank").stringValue());
			//System.out.println("time = " + (baseTime - Long.valueOf(document.getField("time").stringValue())));
			//System.out.println("cur = " + System.currentTimeMillis());
			//writer.write(document.getField("url").stringValue() + "\n");
			//writer.write("res = " + res + "\n");
			//writer.write("pagerank = " + document.getField("page_rank").stringValue() + "\n");
			//writer.write("time = " + Long.valueOf(document.getField("time").stringValue()) + "\n");
			//writer.close();
			double ans;
			ans = scoreGbrt.test(cur);
			//ans = relev * (1 + 1 * calPR(Float.valueOf(document.getField("page_rank").stringValue())) + 0.8 * calTime(Long.valueOf(document.getField("time").stringValue())));
			return (float)ans;
		} catch(Exception e){
			e.printStackTrace();
			System.out.println("MonoScore Error..");
			return 0;
		}
	}
	
	@Override
	public int advance(int target) throws IOException {
		while(docID() < target){
			if(nextDoc() == NO_MORE_DOCS){
				return NO_MORE_DOCS;
			}
		}
		return docID();		
	}

	@Override
	public int docID() {
		return this.doc;
	}

	@Override
	public int nextDoc() throws IOException {
		//System.out.println("inner next");
		int nextDoc = Integer.MAX_VALUE;
		for(int i = 0; i < fields_num; i++){
			if(valid[i]){
				if(termDocs[i].doc() == this.doc){
					if(!termDocs[i].next()){
						valid[i] = false;
						continue;
					}
				}
				if(termDocs[i].doc() < nextDoc)
					nextDoc = termDocs[i].doc();
			}
		}
		
		if(nextDoc == Integer.MAX_VALUE){
			return doc = NO_MORE_DOCS;
		} else {
			return doc = nextDoc;
		}
	}
}
