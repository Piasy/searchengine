package search;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.Weight;

public class BM25Query extends Query{
	private String[] tokens;
	private float[] avgLen;
	
	public BM25Query(String queryString, Analyzer analyzer, float[] avgLen){
		//==========对queryString进行分词============
		List<String> tokens = new ArrayList<String>();
		String[] parts = queryString.split(" ");
		for(String part:parts){
			if(part.length() == 0){
				continue;
			}
			int colonIndex = part.indexOf(':');
			if(colonIndex >= 0){
				tokens.add(part);
			} else {						
				TokenStream ts = analyzer.tokenStream("content", new StringReader(part));
				try{
					while(ts.incrementToken()){
						tokens.add(ts.getAttribute(TermAttribute.class).term());			
					}
				} catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		
		this.tokens = new String[tokens.size()];
		this.tokens = tokens.toArray(this.tokens);
		this.avgLen = new float[avgLen.length];
		this.avgLen = avgLen;
	}
	
	public class BM25Weight extends Weight{
		@Override
		public Explanation explain(IndexReader arg0, int arg1)
				throws IOException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Query getQuery() {			
			return null;
		}

		@Override
		public float getValue() {			
			return 0;
		}

		@Override
		public void normalize(float arg0) {
			
		}

		@Override
		public Scorer scorer(IndexReader reader, boolean scoreDocsInOrder, boolean topScorer)
				throws IOException {
			BM25MonoScorer[] sub = new BM25MonoScorer[tokens.length];
			//BM25MonoScorer.trainGbrt();		//初始化Gbrt模型
			//System.out.println("tokens length = " + tokens.length);
			for(int i = 0; i < tokens.length; i++){
				String[] fields;
				float[] boosts;
				float[] avgLen;
				String word = "";
				//System.out.println("cur token = " + tokens[i]);
				if(tokens[i].indexOf(':') >= 0){
					//System.out.println(": token = " + tokens[i]);
					String[] keyValue = tokens[i].split(":");
					String field = keyValue[0];
					word = keyValue[1];
					fields = new String[] {field};
					if(field.equals("type")){
						//System.out.println("type:" + word);						
						boosts = new float[]{1};
						avgLen = new float[]{3};
					} else {
						boosts = new float[]{1};
						avgLen = new float[]{BM25Config.fieldAvgLenMap.get(field)};
					}
				} else {
					word = tokens[i];
					fields = BM25Config.fieldList;
					boosts = BM25Config.weight;
					avgLen = BM25Config.avgLen;
				}
				
				sub[i] = new BM25MonoScorer(reader, word, fields, boosts, avgLen, new BM25Similarity());
			}
			BM25MultiScorer res = new BM25MultiScorer(new BM25Similarity(), sub);
			return res;
		}

		@Override
		public float sumOfSquaredWeights() throws IOException {
			// TODO Auto-generated method stub
			return 0;
		}
	}
	
	public Weight createWeight(Searcher searcher){
		return new BM25Weight();
	}
	
	@Override
	public String toString(String arg0) {
		// TODO Auto-generated method stub
		return "BM25Query";
	}
}
