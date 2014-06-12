package extractor;

public class Page
{
	final int maxLen = 100; 
	public String url, title, content, subtitle;
	public double pagerank;
	public String anchorIn, anchorOut;
	public String type;
	public long time;
	
	public Page(String type, String url, String title, String content, String subtitle,
			double pagerank, String anchorIn, String anchorOut, long time)
	{
		this.type = type;
		this.url = url;
		this.title = title;
		this.content = content;
		this.subtitle = subtitle;
		this.pagerank = pagerank;
		this.anchorIn = anchorIn;
		this.anchorOut = anchorOut;
		this.time = time;
	}
	
	@Override
	public String toString()
	{
		String str = "";
		
		str += "Url : ";
		if (maxLen < url.length())
		{
			str += url.substring(0, maxLen);
		}
		else
		{
			str += url;
		}
		
		str += "\nTitle : ";
		if (maxLen < title.length())
		{
			str += title.substring(0, maxLen);
		}
		else
		{
			str += title;
		}
		
		str += "\nContent : ";
		if (maxLen < content.length())
		{
			str += content.substring(0, maxLen);
		}
		else
		{
			str += content;
		}
		
		str += "\nSubtitle : ";
		if (maxLen < subtitle.length())
		{
			str += subtitle.substring(0, maxLen);
		}
		else
		{
			str += subtitle;
		}
		
		str += "\nPagerank : " + pagerank;
		
		str += "\nanchorIn : ";
		if (maxLen < anchorIn.length())
		{
			str += anchorIn.substring(0, maxLen);
		}
		else
		{
			str += anchorIn;
		}
		
		str += "\nanchorOut : ";
		if (maxLen < anchorOut.length())
		{
			str += anchorOut.substring(0, maxLen);
		}
		else
		{
			str += anchorOut;
		}
		
		str += "\n\n";
		return str;
	}
}
