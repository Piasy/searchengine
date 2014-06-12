package server;

import java.io.IOException;
import java.io.OutputStream;

import search.BM25Searcher;
import util.Constant;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class TopHandler implements HttpHandler
{

	BM25Searcher searcher;
	public TopHandler(BM25Searcher searcher)
	{
		this.searcher = searcher;
	}
	
	@Override
	public void handle(HttpExchange exchange) throws IOException
	{
		System.out.println("Receive top request");
		
		byte [] responseBody = searcher.getTop(Constant.TOP_RESULT_NUM).toString().getBytes("UTF-8");
		exchange.sendResponseHeaders(200, responseBody.length);
        OutputStream os = exchange.getResponseBody();
        os.write(responseBody);
        os.flush();
        os.close();
	}

}
