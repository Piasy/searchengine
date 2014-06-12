package search;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class BM25Config {
	public static String[] fieldList={"title", "h", "anchor_in", "content", "anchor_out"};
	public static float[] weight = {0.35f, 0.25f, 0.15f, 0.2f, 0.05f};
	public static float[] avgLen = new float[5];
	public static Map<String, Float> fieldWeightMap = new HashMap<String, Float>();
	public static Map<String, Float> fieldAvgLenMap = new HashMap<String, Float>();
	
	static {
		try{
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("index/global.txt")));
			for(int i = 0; i < fieldList.length; i++){
				avgLen[i] = Float.valueOf(reader.readLine());
				fieldWeightMap.put(fieldList[i], weight[i]);
				fieldAvgLenMap.put(fieldList[i], avgLen[i]);
			}
		} catch (Exception e){
			System.out.println("Fail Input Config");
			e.printStackTrace();
		}
	}
}
