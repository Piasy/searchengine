package search;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.poi.util.Units;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wltea.analyzer.lucene.IKAnalyzer;

import util.Util;


public class BM25Searcher {

	private IndexReader reader;
	private IndexSearcher searcher;
	private Analyzer analyzer;
	private int search_maxnum = 100;
	private static JSONArray queryArrayLog;
	
	private float titleAvgLen = 1.0f;
	private float contentAvgLen = 1.0f;
	private float hAvgLen = 1.0f;
	private float anchorInAvgLen = 1.0f;
	private float anchorOutAvgLen = 1.0f;
	
	public BM25Searcher(String indexdir){
		analyzer = new IKAnalyzer();
		
		try{
			reader = IndexReader.open(FSDirectory.open(new File(indexdir)));
			searcher = new IndexSearcher(reader);
			searcher.setSimilarity(new BM25Similarity());
		}catch(IOException e){
			e.printStackTrace();
		}
		
		//==========训练初始化==========
		BM25MonoScorer.trainGbrt();
		
		//============读入log信息===========
		String filename  = "data/log.txt";
		String logString = null;
		try{
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filename)), "UTF-8"));
			logString = reader.readLine();
			reader.close();
			
			JSONObject dataJson = new JSONObject(logString);
			queryArrayLog = dataJson.getJSONArray("list");
		}catch (Exception e) {
			e.printStackTrace();
			System.out.println("Input Json Error...");
		}
		//System.out.println(logString);
		
	}
	
	public void loadGlobals(String filename){
		try{
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
			
			titleAvgLen = Float.parseFloat(reader.readLine());
			hAvgLen = Float.parseFloat(reader.readLine());
			anchorInAvgLen = Float.parseFloat(reader.readLine());
			contentAvgLen = Float.parseFloat(reader.readLine());
			anchorOutAvgLen = Float.parseFloat(reader.readLine());
			reader.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public TopDocs searchQuery(String queryString, int maxnum){
		try {		
			float[] avgLen = {10,100};
			
	        Query query = new BM25Query(queryString, new IKAnalyzer(true), avgLen);
			query.setBoost(1.0f);
			
			TopDocs results = searcher.search(query, maxnum);
			System.out.println(results);
			return results;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public Document getDoc(int docID){
		try{
			return searcher.doc(docID);
		}catch(IOException e){
			e.printStackTrace();
		}
		return null;
	}
	
	private List<String> tokenize(String queryStr){
		//System.out.println("query = " + queryStr);
		ArrayList<String> tokens = new ArrayList<String>();
		IKAnalyzer analyzer = new IKAnalyzer(true);
		for(String part : queryStr.split(" ")){
			if(part.length() == 0){
				continue;
			}
			int colonIndex = part.indexOf(':');
			if(colonIndex >= 0){
				tokens.add(part.split(":")[1]);		//不同于BM25Query
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
		/*for (String token : tokens){
			System.out.println("token = " + token);
		}*/
		return tokens;
	}
	
	private String genAbstract(String queryString, String content){
		String res = "";
		List<String> tokens = tokenize(queryString);
		//System.out.println("Before..");
		//System.out.println(content);		
		content = content.trim();
		//content = content.replace("\n", " ");
		//content = content.replace("\t", " ");
		//content = content.replace("  ", "");
		//System.out.println("After..");
		//System.out.println(content);
		int len = 15;
		
		for(String token : tokens){
			int pos = 0;
			Pattern pat = Pattern.compile(token, Pattern.CASE_INSENSITIVE);
			Matcher m = pat.matcher(content);
			
			while(m.find(pos)){	
				pos = m.start();
				//System.out.println("pos = " + pos);
				int st, ed;
				for(st = pos; ; st--){
					if(st < 0 || content.charAt(st) == '.' || content.charAt(st) == '.'){
						st++;
						break;
					}
					
					if (st == pos - len) break;
				}
				
				for(ed = pos; ; ed++){
					if(ed >= content.length() - 1 || content.charAt(ed) == '.' || content.charAt(ed) == '.'){					
						break;
					}
					
					if (ed == pos + len) break;
				}
				
				//System.out.println("st = " + st + " ed = " + ed);
				
				res += content.subSequence(st, ed + 1) + "... ";
				pos++;
				if(res.length() > 100){
					break;
				}			
			}
		}
		
		return res;
	}
	
	private String getQuery(String queryString){
		String ansString = queryString;
		while (ansString.indexOf("type:") >= 0){
			int t = ansString.indexOf(" ");
			//System.out.println("t = " + t);
			if (t < 0) ansString = "";
			ansString = ansString.substring(t + 1);
			//System.out.println("ansString = " + ansString);
		}
		System.out.println("getQuery = " + ansString);
		return ansString;
	}
	
	public String BM25Search(String originQueryString, int search_need){
		//=========将返回的Json结果===========
		JSONObject ans = new JSONObject();
		String queryString = getQuery(originQueryString);
		//String queryString = originQueryString;
		
		//=========特殊处理输入query为空=======
		if (queryString == ""){
			try{
				JSONArray picSpecialAns = new JSONArray();
				JSONObject cur = new JSONObject();
				cur.put("text", "Welcome");
				cur.put("url", "http://info.tsinghua.edu.cn");
				cur.put("pic", "http://piasy.luyunyi.com/welcome.gif");
				picSpecialAns.put(cur);
				ans.put("picSpecial", picSpecialAns);
				
				JSONArray relatedAns = new JSONArray();
				ans.put("related", relatedAns);
				JSONArray searchAns = new JSONArray();
				ans.put("result", searchAns);
				JSONArray textSpecialAns = new JSONArray();
				ans.put("textSpecial", textSpecialAns);
			}catch (Exception e) {
				e.printStackTrace();
				System.out.println("Deal Empty Error..");
			}
			return ans.toString();
		}
		
		//==========预处理related部分=========
		String curSub;
		int len, relatedNeed = 10, relatedCount = 0;
		if (queryString.length() >= 2){
			curSub = queryString.substring(0, 2);
			len = 2;
		}else {
			curSub = queryString;
			len = 1;
		}
		JSONArray relatedAns = new JSONArray();
		
		//===========处理log信息============
		JSONObject logList = new JSONObject();
		JSONArray logArray = new JSONArray();
		try {
			JSONArray data = queryArrayLog;
			int queryExist = 0;
			for (int i = 0; i < data.length(); i++){
				JSONObject info = data.getJSONObject(i);
				String curQuery = info.getString("query");
				int num = info.getInt("num");
				//System.out.println(curQuery + " : " + num);
				
				//=========处理logArray=========
				if (queryString.equals(curQuery)){
					queryExist = 1;
					JSONObject cur = new JSONObject();
					cur.put("query", queryString);
					cur.put("num", num + 1);
					logArray.put(cur);
				}else{
					logArray.put(info);
					//========处理related相关========
					if (relatedCount == relatedNeed) continue;
					String sub = curQuery.substring(0, len);
					if (curSub.equals(sub)){
						relatedAns.put(curQuery);
						relatedCount += 1;
					}
				}
			}
			
			if (queryExist == 0){
				JSONObject cur = new JSONObject();
				cur.put("query", queryString);
				cur.put("num", 1);
				logArray.put(cur);
			}
			
			queryArrayLog = logArray;
			logList.put("list", logArray);
			//========加入related相关=========
			ans.put("related", relatedAns);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Deal Json Error...");
		}
		//System.out.println(logList);
		
		//========写回log信息============
		String filename  = "data/log.txt";
		try{
			FileOutputStream writer = new FileOutputStream(new File(filename));
			writer.write(logList.toString().getBytes("UTF-8"));
			writer.close();
		}catch (Exception e) {
			e.printStackTrace();
			System.out.println("Write Json Error...");
		}

		//==========JSON_ANS处理==========
		try{
			//==========JSON 加入搜索结果=======
			TopDocs results = searchQuery(originQueryString, search_maxnum);
			ScoreDoc[] hits = results.scoreDocs;
			JSONArray searchAns = new JSONArray();
			HashMap<String, Integer> titleMap = new HashMap<String, Integer>();
			int count = 0;
			
			for (int i = 0; i < hits.length; i++){
				JSONObject cur = new JSONObject();
				Document doc = getDoc(hits[i].doc);
				
				if (titleMap.get(doc.get("title")) == null){
					titleMap.put(doc.get("title"), count);
					
					int flag = 1;
					for (int j = 0; j < i; j++){
						if (Util.isSame(doc.get("url"), getDoc(hits[j].doc).get("url"))){
							flag = 0;
							cur.put("unique", 0);
							break;
						}
					}
					
					if (flag == 1)
						cur.put("unique", 1);
				}else{
					//System.out.println(doc.get("title"));
					//System.out.println(doc.get("url"));
					//continue;
					cur.put("unique", 0);
				}
				
				cur.put("num", count);
				cur.put("url", doc.get("url"));
				cur.put("time", doc.get("time"));
				//cur.put("title", doc.get("title"));
				//cur.put("text", genAbstract(queryString, doc.get("content") + ' ' + doc.get("anchor_in")));
				//System.out.println("type = " + doc.get("type"));
				if (doc.get("type").equals("rrdata") && doc.get("content").equals("")){	//人人状态特殊
					cur.put("title", doc.get("h"));
					cur.put("text", doc.get("title"));
				}else{
					cur.put("title", doc.get("title"));
					cur.put("text", genAbstract(queryString, doc.get("content") + ' ' + doc.get("anchor_in")));
				}
			
//				cur.put("page_rank", doc.get("page_rank"));
//				cur.put("anchor_in", doc.get("anchor_in"));
//				cur.put("anchor_out", doc.get("anchor_out"));
				searchAns.put(cur);
				count += 1;
				
				if (count == search_need) break;
			}
			ans.put("result", searchAns);
			titleMap = null;
			
			//=========JSON 加入图片垂直搜索========
			//System.out.println("queryString = " + queryString);
			
			JSONArray picSpecialAns = new JSONArray();
			if (queryString.matches(".*校历.*") || queryString.matches(".*放假.*") || queryString.matches(".*假期.*")){
				JSONObject cur = new JSONObject();
				cur.put("text", "2013-2014学年度春季学期和夏季学期");
				cur.put("url", "http://info.tsinghua.edu.cn/html/lmntw/file/xiaoli.htm");
				cur.put("pic", "http://info.tsinghua.edu.cn/html/lmntw/img/2013-2014-2.png");
				picSpecialAns.put(cur);
			}
			if (queryString.matches(".*音乐会.*")){
				JSONObject cur = new JSONObject();
				cur.put("text", "2014国际迷你古典”系列音乐会之五——“城堡印象”比利时钢琴家斯蒂文·范豪沃特独奏音乐会。2014-06-06（周五）19:00");
				cur.put("url", "http://www.hall.tsinghua.edu.cn/info/pwzx_hdap/1391#");
				cur.put("pic", "http://www.hall.tsinghua.edu.cn/upload_files/image/1399261290548_51.jpg");
				picSpecialAns.put(cur);
			}
			
			ans.put("picSpecial", picSpecialAns);
			
			//=========JSON 加入文本垂直搜索========
			//System.out.println("queryString = " + queryString);
			
			JSONArray textSpecialAns = new JSONArray();
			if (queryString.matches(".*搜索引擎.*") && queryString.matches(".*课.*")){
				JSONObject cur = new JSONObject();
				cur.put("title", "2013-2014学年度搜索引擎上课信息");
				JSONArray contentArray = new JSONArray();
				contentArray.put("上课教师: 刘奕群");
				contentArray.put("上课时间: 每周二下午第一大节(13:30-15:05)");
				contentArray.put("上课地点: 六教6A211");
				cur.put("content", contentArray);
				textSpecialAns.put(cur);
			}
			
			ans.put("textSpecial", textSpecialAns);
			
		} catch (JSONException e){
			System.out.println("Json Erorr..");
			e.printStackTrace();
		}
		return ans.toString();
	}
	
	public String getTop(int search_need){
		JSONObject ans = new JSONObject();
		JSONArray topAns = new JSONArray();
		try {
			JSONArray data = queryArrayLog;
			
			int[] check = new int[data.length()];
			for (int i = 0; i < data.length(); i++){
				check[i] = 0;
			}
			if (data.length() < search_need)
				search_need = data.length();
			
			for (int k = 0; k < search_need; k++){
				int max = 0;
				int maxIndex = -1;

				for (int i = 0; i < data.length(); i++){
					JSONObject info = data.getJSONObject(i);
					if ((check[i] == 0) && (info.getInt("num") > max)){
						max = info.getInt("num");
						maxIndex = i;
					}
				}
				
				check[maxIndex] = 1;
				topAns.put(data.getJSONObject(maxIndex).getString("query"));
			}
			ans.put("result", topAns);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("getTop() Json Error...");
		}
		
		return ans.toString();
	}
	
	//============补全问题============
	public String getRlated(String originQueryString, int search_need){
		String querString = getQuery(originQueryString);
		JSONObject ans = new JSONObject();
		JSONArray relatedAns = new JSONArray();
		try {
			JSONArray data = queryArrayLog;
			int searchNum = 0;
			
			for (int i = 0; i < data.length(); i++){
				if (searchNum == search_need) break;
				
				JSONObject info = data.getJSONObject(i);
				String curQuery = info.getString("query");
				if (curQuery.length() <= querString.length()){
					continue;
				}
				
				if (querString.equals(curQuery.substring(0, querString.length()))){
					relatedAns.put(curQuery);
					searchNum += 1;
				}
			}

			ans.put("related", relatedAns);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("getRelated() Json Error...");
		}
		
		return ans.toString();
	}
	
	//============清空历史============
	static public void newLog(){
		String filename = "data/log.txt";
		String logString = "{\"list\":[]}";
		try{
			FileOutputStream writer = new FileOutputStream(new File(filename));
			writer.write(logString.getBytes("UTF-8"));
			writer.close();
		}catch (Exception e) {
			e.printStackTrace();
			System.out.println("New Log Error...");
		}
	}

	//============测试的Main函数===========
	public static void main(String[] args){
		//BM25Searcher.newLog();
		BM25Searcher search = new BM25Searcher("index/index");
		search.loadGlobals("index/global.txt");
		/*String ansString0 = search.BM25Search("type:pdf 校庆", 40);
		String ansString1 = search.BM25Search("校庆", 40);
		String ansString2 = search.BM25Search("校庆", 40);
		String ansString3 = search.BM25Search("自主招生", 40);
		String ansString4 = search.BM25Search("选课", 40);
		String ansString7 = search.BM25Search("选课时间", 40);
		String ansString8 = search.BM25Search("选课规则", 40);
		String ansString9 = search.BM25Search("选课老师", 40);
		String ansString5 = search.BM25Search("校历", 40);
		String ansString6 = search.BM25Search("校历", 40);
		String ansString10 = search.BM25Search("清华情 神州景", 40);
		String ansString11 = search.BM25Search("清明假期", 40);
		String ansString12 = search.BM25Search("搜索引擎上课时间", 40);
		String ansString13 = search.BM25Search("type:pdf 选课", 40);
		String ansString14 = search.BM25Search("type:rrdata 校庆", 40);
		String ansString15 = search.BM25Search("type:rrdata 关于网上借用教室", 40);*/
		//System.out.println(ansString);
		String ansString = search.BM25Search("校历", 40);
		System.out.println(ansString);
		/*String ansString = search.BM25Search("PX", 100);
		System.out.println(ansString);
		String topString = search.getTop(10);
		System.out.println(topString);
		String relatedString = search.getRlated("选课", 10);
		System.out.println(relatedString);*/
		
		//String getString = search.getQuery("type:人人 type:pdf ");
		//System.out.println(getString);
	}
}
