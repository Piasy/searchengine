package server;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

import search.BM25Searcher;
import util.Constant;
import util.Util;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class RelatedHandler implements HttpHandler
{

	BM25Searcher searcher;
	public RelatedHandler(BM25Searcher searcher)
	{
		this.searcher = searcher;
	}
	
	@Override
	public void handle(HttpExchange exchange) throws IOException
	{
		System.out.println("Receive top request");
		HashMap<String, String> params = Util.getParams(exchange.getRequestURI().getQuery());
		String query = params.get("query");
		
		byte [] responseBody = searcher.getRlated(query, Constant.TOP_RESULT_NUM).toString().getBytes("UTF-8");
		exchange.sendResponseHeaders(200, responseBody.length);
        OutputStream os = exchange.getResponseBody();
        os.write(responseBody);
        os.flush();
        os.close();
	}

}
