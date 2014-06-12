package search;

import java.io.*;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



public class Test {
	public static void main(String[] args) throws JSONException{
		/*System.out.println(System.currentTimeMillis());
		long a = new Date().getTime(); 
		long d = 1000 * 60 * 60 * 24;
		long y = d * 356; 
		System.out.println(a); 
		System.out.println(a / d);
		System.out.println(a / y);
		
		System.out.println(a - y * 3);*/
		/*long baseTime = 1300000000000l;
		float divisionTime = 1000 * 60 * 60 * 24 * 365 * 1000f;
		long nowTime = 1398927389514l;
		//long nowTime = 1333868074914l;
		System.out.println((nowTime - baseTime) / divisionTime);*/
		
		/*String s = "选课时间";
		System.out.println(s.substring(0, 3));*/

		/*JSONObject dataJson=new JSONObject("{\"response\":{\"data\":[{\"address\":\"南京市游乐园\",\"province\":\"江苏\",\"district\":\"玄武区\",\"city\":\"南京\"}]},\"status\":\"ok\"}");
		System.out.println(dataJson);
		JSONObject response=dataJson.getJSONObject("response");
		JSONArray data=response.getJSONArray("data");
		JSONObject info=data.getJSONObject(0);
		String province=info.getString("province");
		String city=info.getString("city");
		String district=info.getString("district");
		String address=info.getString("address");
		System.out.println(province+city+district+address);*/
		
		/*String jsonString = "{\"list\":[{\"query\":\"校庆\", \"num\":4}, {\"query\":\"选课\", \"num\":3}]}";
		System.out.println(jsonString);
		JSONObject dataJson=new JSONObject(jsonString);
		System.out.println(dataJson);
		JSONArray data = dataJson.getJSONArray("list");
		System.out.println(data);
		for (int i = 0; i < data.length(); i++){
			JSONObject info=data.getJSONObject(i);
			String query=info.getString("query");
			int num = info.getInt("num");
			System.out.println(query + " : " + num);
		}*/
		
		String s = "搜索引擎上课地点";
		System.out.println(s.matches(".*搜索引擎.*"));
		System.out.println(s.matches(".*上课.*"));
		
	}
}
