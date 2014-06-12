package server;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import search.BM25Searcher;
import util.Constant;
import util.Util;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class SearchHandler implements HttpHandler
{

	BM25Searcher searcher;
	public SearchHandler(BM25Searcher searcher)
	{
		this.searcher = searcher;
	}
	
	HashMap<String, String> result = new HashMap<String, String>();
	@Override
	public void handle(HttpExchange exchange) throws IOException
	{
		try
		{
			System.out.println("Receive search request");
			HashMap<String, String> params = Util.getParams(exchange.getRequestURI().getQuery());
			String query = params.get("query");
			
			int start = Integer.parseInt(params.get("from"));
			int end = Integer.parseInt(params.get("until"));
			
			if (!result.containsKey(query))
			{
//				System.out.println("SearchHandler.handle()");
				String ss = searcher.BM25Search(query, Constant.SEARCH_RESULT_NUM);
				System.out.println("ss = " + ss);
				result.put(query, ss);
			}
			
			try
			{
				JSONObject org = new JSONObject(result.get(query));
//				System.out.println(org.toString());
				JSONArray orgResults = org.getJSONArray("result");
				JSONArray results = new JSONArray();
				for (int i = start; i < end; i ++)
				{
					if (orgResults.length() <= i)
					{
						break;
					}
					results.put(orgResults.get(i));
				}
				
//				ArrayList<Integer> passed = new ArrayList<Integer>();
//				for (int i = 0; i < results.length(); i ++)
//				{
//					boolean pp = false;
//					for (Integer p : passed)
//					{
//						if (p == i)
//						{
//							pp = true;
//							break;
//						}
//					}
//					
//					if (pp)
//					{
//						continue;
//					}
//					
//					boolean unique = true;
//					int j;
//					for (j = 0; j < results.length(); j ++)
//					{
//						if (i == j)
//						{
//							continue;
//						}
//						
//						if (Util.isSame(results.getJSONObject(i).getString("url"), 
//								results.getJSONObject(j).getString("url")))
//						{
//							unique = false;
//							break;
//						}
//					}
//					
//					if (unique)
//					{
//						results.getJSONObject(i).put("unique", 1);
//					}
//					else
//					{
//						results.getJSONObject(i).put("unique", 1);
//						results.getJSONObject(j).put("unique", 0);
//						passed.add(j);
//					}
//				}
				
				org.put("result", results);
				
				byte [] responseBody = org.toString().getBytes("UTF-8");
				exchange.sendResponseHeaders(200, responseBody.length);
		        OutputStream os = exchange.getResponseBody();
		        os.write(responseBody);
		        os.flush();
		        os.close();
			}
			catch (JSONException e)
			{
				e.printStackTrace();
			}
		}
		catch (NumberFormatException e) 
		{
			e.printStackTrace();
		}
	}

}
