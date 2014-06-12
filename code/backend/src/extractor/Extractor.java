package extractor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.poi.POIXMLDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.xmlbeans.XmlException;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.wltea.analyzer.lucene.IKAnalyzer;

public class Extractor
{
	public static void main(String[] args)
	{
		Extractor extractor = new Extractor();
		File mirror = new File("mirror");
		extractor.start(mirror);
	}
	
	public Extractor()
	{
		instance = this;
	}
	
	Extractor instance;
//	ArrayList<TraverseThread> traverseThreads = new ArrayList<TraverseThread>();
	final int MAX_THREAD_NUM = 20;
	int threadCount = 0;
	int pageCount = 0;
	
	File mirror;
	String workPath = "";
	String SPLITTER = "@#$%$#@";
	File indexSaveDir = new File("index/index");
	Analyzer luceneAnalyzer;
	IndexWriter indexWriter;
	
	float titleAvgLen = 1.0f, anchorInAvgLen = 1.0f, anchorOutAvgLen = 1.0f;
    int nAnchorOut = 0, nh = 0;
    float contentAvgLen = 1.0f, hAvgLen = 1.0f;
	
	public void start(File dir)
	{		
		synchronized (this)
		{

			try
			{
				luceneAnalyzer = new IKAnalyzer();
				indexWriter = new IndexWriter(FSDirectory.open(indexSaveDir), 
						new IndexWriterConfig(Version.LUCENE_35, luceneAnalyzer));
				
				//step 1
				workPath = System.getProperty("user.dir");
				mirror = dir;
//				asignID(dir);
//				
//				//end of step 1, store pages
//				File pagesFile = new File("pages.txt");
//				FileOutputStream fout = new FileOutputStream(pagesFile);
//				
//				for (String url : pages.keySet())
//				{
//					fout.write(("" + pages.get(url).intValue() + "\t" + url + "\n").getBytes());
//				}
//				
//				fout.close();
//				System.out.println("Step 1: asign id finished!");
//
//				//step 2
//				genLinks(dir);
//				
//				//end of step 2.1, store links
//				File linksFile = new File("links.txt");
//				fout = new FileOutputStream(linksFile);
//				
//				for (Integer from : links.keySet())
//				{
//					ArrayList<Integer> tos = links.get(from);
//					for (Integer to : tos)
//					{
//						fout.write(("" + from.intValue() + "\t" + to.intValue() + "\n").getBytes());
//					}
//				}
//				
//				fout.close();
//				
//				//end of step 2.2, store anchor in
//				File anchorInFile = new File("anchorin.txt");
//				fout = new FileOutputStream(anchorInFile);
//
//				for (Integer from : anchorIn.keySet())
//				{
//					ArrayList<String> ins = anchorIn.get(from);
//					String text = "";
//					for (int i = 0; i < ins.size(); i ++)
//					{
//						text += ins.get(i);
//						if (i < ins.size() - 1)
//						{
//							//text += SPLITTER;
//							text += " ";
//						}
//					}
//					text += "\n";
//					
//					fout.write(("" + from.intValue() + "\t" + text).getBytes());
//				}
//				
//				fout.close();
//
//				System.out.println("Step 2: gen links & anchor in finished!");
				
				getAllFromFile();
				System.out.println("Step 1&2: get all from file finished!");
				
				//step 3, index
				indexRRData();
				index(dir);
				
				contentAvgLen /= indexWriter.numDocs();			
				titleAvgLen /= indexWriter.numDocs();
				hAvgLen /= nh;
				anchorInAvgLen /=indexWriter.numDocs();			
				anchorOutAvgLen /= nAnchorOut;
				saveGlobals("index/global.txt");
				
				indexWriter.close();
				System.out.println("Step 3: index finished!");
				
			}
			catch (FileNotFoundException e)
			{
				e.printStackTrace();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			
			System.out.println("Extractor finished!");
		}
	}
	
	//id, anchor in, pagerank
	protected void getAllFromFile() 
	{
		try 
		{
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File("pages.txt"))));
			
			String line;
			while ((line = reader.readLine()) != null)
			{
				String [] strs = line.split("\t");
				try
				{
					pages.put(strs[1], Integer.valueOf(strs[0]));
				}
				catch (NumberFormatException e)
				{
					e.printStackTrace();
				}
			}
			reader.close();
			
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File("anchorin.txt"))));
			while ((line = reader.readLine()) != null)
			{
				String [] strs = line.split("\t");
				if (strs.length == 2)
				{
					try
					{
						ArrayList<String> ins = new ArrayList<String>();
						ins.add(strs[1]);
						anchorIn.put(Integer.valueOf(strs[0]), ins);
					}
					catch (NumberFormatException e)
					{
						e.printStackTrace();
					}
				}
			}
			reader.close();
			
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File("pagerank.txt"))));
			while ((line = reader.readLine()) != null)
			{
				try
				{
					String [] strs = line.split("\t");
					int id = Integer.parseInt(strs[0]);
					float pr = Float.parseFloat(strs[1]);
					pageranks.put(id, pr);
				}
				catch (NumberFormatException e)
				{
					e.printStackTrace();
				}
			}
			reader.close();
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

	HashMap<Integer, Float> pageranks = new HashMap<Integer, Float>();
	protected void getPageRank(String filename)
	{
		try
		{
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filename))));
			
			String line;
			while ((line = reader.readLine()) != null)
			{
				try
				{
					String [] strs = line.split("\t");
					int id = Integer.parseInt(strs[0]);
					float pr = Float.parseFloat(strs[1]);
					pageranks.put(id, pr);
				}
				catch (NumberFormatException e)
				{
					e.printStackTrace();
				}
			}
			
			reader.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	HashMap<String, Integer> pages = new HashMap<String, Integer>();
	protected void asignID(File dir)
	{
		ArrayList<File> dirs = new ArrayList<File>();
		ArrayList<File> files = new ArrayList<File>();
		
		File [] mfiles = dir.listFiles();
		if (mfiles != null)
		{
			for (File file : mfiles)
			{
				if (file.isDirectory())
				{
					dirs.add(file);
				}
				else if (file.isFile())
				{
					files.add(file);
				}
			}
			
			for (File file : dirs)
			{
				asignID(file);
			}
			
			for (File file : files)
			{
				//step 1, allocate page id
				String url = file2url(mirror, file);
				if (!pages.containsKey(url))
				{
					synchronized (this)
					{
						pages.put(url, Integer.valueOf(pageCount));
						pageCount ++;
					}
				}
			}
		}
	}

	HashMap<Integer, ArrayList<Integer>> links = new HashMap<Integer, ArrayList<Integer>>();
	HashMap<Integer, ArrayList<String>> anchorIn = new HashMap<Integer, ArrayList<String>>();
	protected void genLinks(File dir)
	{
		ArrayList<File> dirs = new ArrayList<File>();
		ArrayList<File> files = new ArrayList<File>();
		
		File [] mfiles = dir.listFiles();
		if (mfiles != null)
		{
			for (File file : mfiles)
			{
				if (file.isDirectory())
				{
					dirs.add(file);
				}
				else if (file.isFile())
				{
					files.add(file);
				}
			}
			
			for (File file : dirs)
			{
				genLinks(file);
			}
			
			for (File file : files)
			{
				//step 2, gen link map and anchor in
				String url = file2url(mirror, file);
				links.put(pages.get(url), getAnchorOutIDs(file));
			}
		}
	}

	protected void indexRRData()
	{
		try
		{
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File("data.txt")), "UTF-8"));
			
			
			String line;
			while ((line = reader.readLine()) != null)
			{
				try
				{
					JSONObject data = new JSONObject(line);

					org.apache.lucene.document.Document document  =  
							new  org.apache.lucene.document.Document();
					Field urlField = new Field("url", data.getString("url"), Field.Store.YES, Field.Index.NO);		
					document.add(urlField);
					Field pageRankField = new Field("page_rank", "" + 1, Field.Store.YES, Field.Index.NO);
					document.add(pageRankField);
					Field timeField = new Field("time", "" + (data.getLong("time") * 1000), Field.Store.YES, Field.Index.NO);
					document.add(timeField);
					Field titleField = new Field("title", data.getString("title"), Field.Store.YES, Field.Index.ANALYZED);
					document.add(titleField);
					Field contentField = new Field("content", "", Field.Store.YES, Field.Index.ANALYZED);
					document.add(contentField);
					Field hField = new Field("h", data.getString("name"), Field.Store.YES, Field.Index.ANALYZED);
					document.add(hField);
					Field anchorInField = new Field("anchor_in", "", Field.Store.YES, Field.Index.ANALYZED);
					document.add(anchorInField);
					Field anchorOutField = new Field("anchor_out", "", Field.Store.YES, Field.Index.ANALYZED);
					document.add(anchorOutField);
					document.setBoost((float) 1);
					document.add(new Field("type", "rrdata", Field.Store.YES, Field.Index.NOT_ANALYZED));
					
					indexWriter.addDocument(document);
					
					titleAvgLen += data.getString("title").length();
					
					count ++;
					if (count % 500 == 0)
					{
						System.out.println(count);
					}
				}
				catch (JSONException e)
				{
					System.out.println(line);
					e.printStackTrace();
				}
			}
			
			reader.close();
			
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File("datablog.txt")), "UTF-8"));
			while ((line = reader.readLine()) != null)
			{
				try
				{
					JSONObject data = new JSONObject(line);

					org.apache.lucene.document.Document document  =  
							new  org.apache.lucene.document.Document();
					Field urlField = new Field("url", data.getString("url"), Field.Store.YES, Field.Index.NO);		
					document.add(urlField);
					Field pageRankField = new Field("page_rank", "" + 1, Field.Store.YES, Field.Index.NO);
					document.add(pageRankField);
					Field timeField = new Field("time", "" + (data.getLong("time") * 1000), Field.Store.YES, Field.Index.NO);
					document.add(timeField);
					Field titleField = new Field("title", data.getString("title"), Field.Store.YES, Field.Index.ANALYZED);
					document.add(titleField);
					Field contentField = new Field("content", data.getString("content"), Field.Store.YES, Field.Index.ANALYZED);
					document.add(contentField);
					Field hField = new Field("h", data.getString("name"), Field.Store.YES, Field.Index.ANALYZED);
					document.add(hField);
					Field anchorInField = new Field("anchor_in", "", Field.Store.YES, Field.Index.ANALYZED);
					document.add(anchorInField);
					Field anchorOutField = new Field("anchor_out", "", Field.Store.YES, Field.Index.ANALYZED);
					document.add(anchorOutField);
					document.setBoost((float) 1);
					document.add(new Field("type", "rrdata", Field.Store.YES, Field.Index.NOT_ANALYZED));
					
					indexWriter.addDocument(document);
					
					titleAvgLen += data.getString("title").length();
					contentAvgLen += data.getString("content").length();
					
					count ++;
					if (count % 500 == 0)
					{
						System.out.println(count);
					}
				}
				catch (JSONException e)
				{
					System.out.println(line);
					e.printStackTrace();
				}
			}
			
			reader.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	int count = 0;
	HashMap<Integer, Page> indexPages = new HashMap<Integer, Page>();
	protected void index(File dir)
	{
		ArrayList<File> dirs = new ArrayList<File>();
		ArrayList<File> files = new ArrayList<File>();
		
		File [] mfiles = dir.listFiles();
		if (mfiles != null)
		{
			for (File file : mfiles)
			{
				if (file.isDirectory())
				{
					dirs.add(file);
				}
				else if (file.isFile())
				{
					files.add(file);
				}
			}
			
			for (File file : dirs)
			{
				index(file);
			}
			
			for (File file : files)
			{
				//step 3, index
				Page page = extract(file);
				if (page != null)
				{
					org.apache.lucene.document.Document document  =  
							new  org.apache.lucene.document.Document();
					
					Field urlField = new Field("url", page.url, Field.Store.YES, Field.Index.NO);		
					document.add(urlField);
					Field pageRankField = new Field("page_rank", "" + page.pagerank, Field.Store.YES, Field.Index.NO);
					document.add(pageRankField);
					Field timeField = new Field("time", "" + page.time, Field.Store.YES, Field.Index.NO);
					document.add(timeField);
					Field titleField = new Field("title", page.title, Field.Store.YES, Field.Index.ANALYZED);
					document.add(titleField);
					Field contentField = new Field("content", page.content, Field.Store.YES, Field.Index.ANALYZED);
					document.add(contentField);
					Field hField = new Field("h", page.subtitle, Field.Store.YES, Field.Index.ANALYZED);
					document.add(hField);
					Field anchorInField = new Field("anchor_in", page.anchorIn, Field.Store.YES, Field.Index.ANALYZED);
					document.add(anchorInField);
					Field anchorOutField = new Field("anchor_out", page.anchorOut, Field.Store.YES, Field.Index.ANALYZED);
					document.add(anchorOutField);
					document.setBoost((float) page.pagerank);
					document.add(new Field("type", page.type, Field.Store.YES, Field.Index.NOT_ANALYZED));
					
					try
					{
						indexWriter.addDocument(document);
						
						titleAvgLen += page.title.length();
						contentAvgLen += page.content.length();
						hAvgLen += page.subtitle.length();
						anchorInAvgLen += page.anchorIn.length();
						anchorOutAvgLen += page.anchorOut.length();
						
						count ++;
						if (count % 500 == 0)
						{
							System.out.println(count);
						}
					}
					catch (CorruptIndexException e)
					{
						e.printStackTrace();
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	protected ArrayList<Integer> getAnchorOutIDs(File file)
	{
		String name = file.getName().toLowerCase();
		ArrayList<Integer> ret = new ArrayList<Integer>();
		
		try
		{
			if (name.endsWith(".html") ||
					name.endsWith(".htm"))
			{
				org.jsoup.nodes.Document html = Jsoup.parse(file, "UTF-8");
				
				//anchor out and anchor in of other pages
				for(org.jsoup.nodes.Element e : html.getElementsByTag("a"))
				{
					String url = anchor2url(mirror, file, e.attr("href"));
					if (pages.containsKey(url))
					{
						ret.add(pages.get(url));
						
						//add anchor in text of this url
						add2AnchorIn(url, e.ownText());
					}
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return ret;
	}
	
	protected void add2AnchorIn(String url, String text)
	{
		if (anchorIn.containsKey(pages.get(url)))
		{
			ArrayList<String> texts = anchorIn.get(pages.get(url));
			boolean exist = false;
			for (String tt : texts)
			{
				if (text.equals(tt))
				{
					exist = true;
					break;
				}
			}
			if (!exist)
			{
				texts.add(text);
			}
		}
		else
		{
			ArrayList<String> texts = new ArrayList<String>();
			texts.add(text);
			anchorIn.put(pages.get(url), texts);
		}
	}
	
	protected Page extract(File file)
	{
		String name = file.getName().toLowerCase();
		String url = file2url(mirror, file);
		float pr = 1.0f;
		if (pageranks.containsKey(pages.get(url)))
		{
			pr = pageranks.get(pages.get(url));
		}
//		System.out.println("Extract " + file.getAbsolutePath());
		Page page = null;
		
		try
		{
			if (name.endsWith(".doc"))
			{
				org.apache.poi.hwpf.extractor.WordExtractor word = new
						WordExtractor(new org.apache.poi.hwpf.HWPFDocument(new FileInputStream(file)));
				
				String content = word.getText();
				String title = file.getName();
				
				String insStr = "";
				ArrayList<String> ins = anchorIn.get(pages.get(url));
				if (ins != null)
				{
					for (String in : ins)
					{
						insStr += in + " ";
					}
				}
				page = new Page("doc", url, title, content, "", pr, insStr, "", 0);
			}
			else if (name.endsWith(".docx"))
			{
				org.apache.poi.POIXMLTextExtractor word = new 
						org.apache.poi.xwpf.extractor.XWPFWordExtractor(POIXMLDocument.openPackage(file.getAbsolutePath()));

				String content = word.getText();
				String title = file.getName();
				
				String insStr = "";
				ArrayList<String> ins = anchorIn.get(pages.get(url));
				if (ins != null)
				{
					for (String in : ins)
					{
						insStr += in + " ";
					}
				}
				
				page = new Page("doc", url, title, content, "", pr, insStr, "", 0);
			}
			else if (name.endsWith(".pdf"))
			{
				PDFParser p = new PDFParser(new FileInputStream(file));
				p.parse();        
		        PDFTextStripper ts = new PDFTextStripper();        
		        
		        String content = ts.getText(p.getPDDocument());
		        String title = file.getName();
				
				String insStr = "";
				ArrayList<String> ins = anchorIn.get(pages.get(url));
				if (ins != null)
				{
					for (String in : ins)
					{
						insStr += in + " ";
					}
				}
				
		        page = new Page("pdf", url, title, content, "", pr, insStr, "", 0);
			}
			else if (name.endsWith(".txt"))
			{
				BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
				String content = "";
				String str;
				while ((str = reader.readLine()) != null)
				{
					content += str + " ";
				}
				reader.close();
				
				String title = file.getName();
				
				String insStr = "";
				ArrayList<String> ins = anchorIn.get(pages.get(url));
				if (ins != null)
				{
					for (String in : ins)
					{
						insStr += in + " ";
					}
				}
				
				page = new Page("txt", url, title, content, "", pr, insStr, "", 0);
			}
			else if (name.endsWith(".html") || name.endsWith(".htm"))
			{
				org.jsoup.nodes.Document htmlTmp = Jsoup.parse(file, "UTF-8");
				org.jsoup.select.Elements metas = htmlTmp.getElementsByTag("meta");
				for (org.jsoup.nodes.Element meta : metas)
				{
					String contentMeta = meta.attr("content");
					if (contentMeta.contains("charset="))
					{
						String charset = contentMeta.substring(contentMeta.lastIndexOf("charset=") + 8, 
										contentMeta.length());
						
						org.jsoup.nodes.Document html = Jsoup.parse(file, charset);
						//title
						String title = "";					
						org.jsoup.select.Elements titleEles = html.getElementsByTag("title"); 
						if(0 < titleEles.size())
						{
							title = titleEles.get(0).text();
						}
						
						//content
						String content = "";
						for(org.jsoup.nodes.Element e : html.select("p,span,td,th,li,pre,code,em,strong,b,i"))
						{
							content += ' ' + e.ownText();
						}
						
						//subtitle
						String subtitle = "";
						for(org.jsoup.nodes.Element e : html.getElementsByTag("h1,h2,h3,h4,h5,h6"))
						{
							subtitle += e.ownText();
							if (0 < e.ownText().length())
							{
								nh ++;
							}
						}
						
						//anchor out
						String anchorOut = "";
						for(org.jsoup.nodes.Element e : html.getElementsByTag("a"))
						{
							anchorOut += ' ' + e.text();
							if (0 < e.text().length())
							{
								nAnchorOut ++;
							}
						}
						
						String insStr = "";
						ArrayList<String> ins = anchorIn.get(pages.get(url));
						if (ins != null)
						{
							for (String in : ins)
							{
								insStr += in + " ";
							}
						}
						
						//time field, only for news.tsinghua.edu.cn
						long time = 0;
						org.jsoup.nodes.Element times = htmlTmp.getElementById("title_detail_picwriter");
						if (times != null)
						{
							String str = times.ownText();
//							System.out.println(str);
							Pattern p = Pattern.compile("(.*)([0-9]{4})(-)([0-9]{1,2})(-)([0-9]{1,2})(.*)");
							Matcher matcher = p.matcher(str);
							if (matcher.find())
							{
//								System.out.println(matcher.group(2) + " " + matcher.group(4) + " " + matcher.group(6));
								Calendar calendar = Calendar.getInstance();
								calendar.set(Integer.parseInt(matcher.group(2)), 
											 Integer.parseInt(matcher.group(4)) - 1, 
											 Integer.parseInt(matcher.group(6)));
								time = calendar.getTimeInMillis();
							}
						}
						
						page = new Page("htm", url, title, content, 
								subtitle, pr, insStr, anchorOut, time);
						
						break;
					}
				}
			}
		}
		catch (IllegalCharsetNameException e)
		{
			e.printStackTrace();
		}
		catch (UnsupportedCharsetException e)
		{
			e.printStackTrace();
		}
		catch (NullPointerException e)
		{
			e.printStackTrace();
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (XmlException e)
		{
			e.printStackTrace();
		}
		catch (OpenXML4JException e)
		{
			e.printStackTrace();
		}
				
		return page;
	}

	public void saveGlobals(String filename)
	{
		try
		{
			PrintWriter pw=new PrintWriter(new File(filename));
    		pw.println(titleAvgLen);
    		pw.println(hAvgLen);
    		pw.println(anchorInAvgLen);
    		pw.println(contentAvgLen);
    		pw.println(anchorOutAvgLen);
    		pw.close();
    	}
		catch(IOException e)
		{
    		e.printStackTrace();
    	}
    }
	
	protected String file2url(File base, File file)
	{
		String dir = base.getAbsolutePath();
		String page = file.getAbsolutePath();
		//TODO !Platform dependent
		return "http://" + page.substring(page.indexOf(dir) + dir.length() + 1).replaceAll("\\\\", "/");
	}
	
	protected String anchor2url(File base, File file, String url)
	{
		String ret;
		if (url.startsWith("http://"))
		{
			ret = url;
		}
		else
		{
			String dir = base.getAbsolutePath();
			String page = file.getAbsolutePath();
			String str = page.substring(page.indexOf(dir) + dir.length() + 1);
			
			ret = "http://" + str.substring(0, str.indexOf("\\")) + url;
		}
		return ret;
	}
}
