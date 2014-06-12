package util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;

public class Util
{
	public static void main(String[] args)
	{
		String url1 = "http://www.tsinghua.edu.cn/publish/news/4205/2013/20130929163638552662413/20130929163638552662413_.html";
		String url2 = "http://news.tsinghua.edu.cn/publish/news/mobile/4205/2013/20130929163638552662413/20130929163638552662413_.html";
		String url3 = "http://news.tsinghua.edu.cn/publish/news/4205/2013/20130929163638552662413/20130929163638552662413_.html";
		String url4 = "http://www.tsinghua.edu.cn/publish/news/4205/2013/20140606102328527213446/20140606102328527213446_.html";
		System.out.println(Util.isSame(url1, url2));
		System.out.println(isSame(url1, url3));
		System.out.println(isSame(url2, url3));
		System.out.println(isSame(url1, url4));
		System.out.println(isSame(url3, url4));
	}
	
	public static HashMap<String, String> getParams(String data)
	{
		HashMap<String, String> params = new HashMap<String, String>();
		try
		{
			String [] datas = data.split("&");
			for (String s : datas)
			{
				String [] values = s.split("=");
				if (values.length == 1 && values[0].equals("query"))
				{
					params.put(values[0], URLDecoder.decode(URLDecoder.decode("", "UTF-8"), "UTF-8"));
				}
				else
				{
					params.put(values[0], URLDecoder.decode(URLDecoder.decode(values[1], "UTF-8"), "UTF-8"));
				}
			}
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
		return params;
	}
	
	public static boolean isSame(String url1, String url2)
	{
		String suffix1 = "1", suffix2 = "2";
		
		if (url1.startsWith("http://news.tsinghua.edu.cn/publish/news/mobile/"))
		{
			suffix1 = url1.substring(48);
		}
		else if (url1.startsWith("http://news.tsinghua.edu.cn/publish/news/"))
		{
			suffix1 = url1.substring(41);
		}
		else if (url1.startsWith("http://www.tsinghua.edu.cn/publish/news/"))
		{
			suffix1 = url1.substring(40);
		}
		
		if (url2.startsWith("http://news.tsinghua.edu.cn/publish/news/mobile/"))
		{
			suffix2 = url2.substring(48);
		}
		else if (url2.startsWith("http://news.tsinghua.edu.cn/publish/news/"))
		{
			suffix2 = url2.substring(41);
		}
		else if (url2.startsWith("http://www.tsinghua.edu.cn/publish/news/"))
		{
			suffix2 = url2.substring(40);
		}
		
		return suffix1.equals(suffix2);
	}
	
}
