package driver;

import java.io.IOException;
import java.net.InetSocketAddress;

import search.BM25Searcher;
import server.RelatedHandler;
import server.SearchHandler;
import server.TopHandler;

import com.sun.net.httpserver.HttpServer;


public class SearchDriver
{

	public static void main(String[] args)
	{
		try
		{
			HttpServer server = HttpServer.create(new InetSocketAddress(5020), 0);
			BM25Searcher search = new BM25Searcher("index/index");
			server.createContext("/search", new SearchHandler(search));
			server.createContext("/top", new TopHandler(search));
			server.createContext("/related", new RelatedHandler(search));
			server.start();
			System.out.println("Server start!");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

}
