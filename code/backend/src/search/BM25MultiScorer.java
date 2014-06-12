package search;

import java.io.IOException;

import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Similarity;

public class BM25MultiScorer extends Scorer{
	
	private Scorer[] subScorers;
	private int doc;
	public BM25MultiScorer(Similarity similarity,Scorer[] subScorer){
		super(similarity);
		this.subScorers = subScorer;
		this.doc = 0;
		//System.out.println("sub count:"+String.valueOf(subScorer.length));
	}
	
	@Override
	public float score() throws IOException {
		float res = 1;
		for(int i=0; i < subScorers.length; i++){
			res *= subScorers[i].score();
		}
		return res;
	}

	@Override
	public int advance(int target) throws IOException {
		int next;
		while((next = nextDoc()) != NO_MORE_DOCS && next<target){	}
		return next;
	}

	@Override
	public int docID() {
		return this.doc;
	}

	@Override
	public int nextDoc() throws IOException {
		int max_doc = doc + 1;
		int eqn_cnt = 0;
		if(this.doc == NO_MORE_DOCS){
			return NO_MORE_DOCS;
		}
		
		//System.out.println("outer next");
		while(true){			
			for(int i = 0; i < subScorers.length; i++){
				int nextDoc;
				while(subScorers[i].docID() < max_doc){
					nextDoc = subScorers[i].nextDoc();
					if(nextDoc == NO_MORE_DOCS){
						doc = NO_MORE_DOCS;
						return NO_MORE_DOCS;
					}
				}				
								
				if(subScorers[i].docID() > max_doc){
					max_doc = subScorers[i].docID();
					eqn_cnt = 1;
				} else {
					eqn_cnt++;
				}
			}
			if(eqn_cnt >= subScorers.length){
				doc = max_doc;
				return max_doc;
			}
		}
	}
}
