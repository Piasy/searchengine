package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.jsoup.Jsoup;

import extractor.Page;

public class Fucker {

	public static void main(String[] args) 
	{
//		File pdf = new File("mirror");
//		index(pdf);
		try
		{
			File file = new File("test.html");
			org.jsoup.nodes.Document htmlTmp = Jsoup.parse(file, "UTF-8");
			org.jsoup.nodes.Element times = htmlTmp.getElementById("title_detail_picwriter");
			String str = times.ownText();
			System.out.println(str);
			Pattern p = Pattern.compile("(.*)([0-9]{4})(-)([0-9]{2})(-)([0-9]{2})(.*)");
			Matcher matcher = p.matcher(str);
			if (matcher.find())
			{
				System.out.println(matcher.group(2) + " " + matcher.group(4) + " " + matcher.group(6));
			}
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		
		
	}

	static protected void index(File dir)
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
				if (file.getName().toLowerCase().endsWith("doc") || file.getName().toLowerCase().endsWith("docx"))
				{
					System.out.println(file.getAbsolutePath());
					file.delete();
				}
			}
		}
	}
	
}
