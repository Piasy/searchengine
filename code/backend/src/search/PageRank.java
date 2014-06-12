package search;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;

class Edge{
	public int source;
	public int target;
	public Edge(int from,int to) {
		source = from;
		target = to;
	}
}

public class PageRank {	
	private ArrayList<Edge> edges;
	private int inDeg[];
	private int outDeg[];
	private int nodeCount;
	private final int ALL_NODE_COUNT = 100;
	
	public PageRank(){		
		edges = new ArrayList<Edge>();		
		nodeCount = 0;
	}
	
	//===============读入链接文件(links.txt)===============
	public void readInput(){	
		try{
			//BufferedReader reader = new BufferedReader(new FileReader("links.txt"));
			BufferedReader reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(new File("links.txt"))),"utf-8"),5*1024*1024);	//用5M的缓冲读取文本文件
			String line = null;
			while((line = reader.readLine()) != null){
				String[] e = line.split("\t");
				int src = Integer.valueOf(e[0]);
				int des = Integer.valueOf(e[1]);
				//System.out.println(src + "-->" + des);
				nodeCount = Math.max(src, nodeCount);
				nodeCount = Math.max(des, nodeCount);
				edges.add(new Edge(src,des));
			}
			nodeCount++;
			//nodeCount = ALL_NODE_COUNT;
			reader.close();
			
			constructGraph();
		} catch (Exception e){
			System.out.println("Fail input [links.txt]");
			e.printStackTrace();
		}
	}
	
	//==============构建图结构===============
	public void constructGraph(){
		inDeg = new int[nodeCount];
		outDeg = new int[nodeCount];
		
		for(int i = 0; i < nodeCount; i++){
			inDeg[i] = 0;
			outDeg[i] = 0;
		}
		
		for(int i = 0; i < edges.size(); i++){
			Edge e = edges.get(i);
			inDeg[e.target]++;
			outDeg[e.source]++;
		}
	}
	
	//============计算PageRank并输出到文本============
	public void calPageRank(){		
		double PR[] = new double[nodeCount];
		double I[] = new double[nodeCount];
		double S=0;
		final double alpha = 0.15;
		final int TN = 30;
		
		//============计算PageRank===========
		for(int i = 0; i < nodeCount; i++){
			PR[i] = 1.0 / nodeCount;
		}
		
		for(int k = 0; k < TN; k++){
			S = 0;
			for(int i = 0; i < nodeCount; i++){
				I[i] = alpha / nodeCount;
				if(outDeg[i] == 0){
					S += PR[i]; 
				}
			}
			
			for(int i = 0; i < edges.size(); i++){
				Edge edge = edges.get(i);
				int src = edge.source;
				int tar = edge.target;
				I[tar] += (1 - alpha) * PR[src] / outDeg[src];				
			}
			
			for(int i = 0; i < nodeCount; i++){
				PR[i] = I[i] + (1 - alpha) * S / nodeCount;
			}
		}
		
		/*for (int i = 0; i < nodeCount; i++){
			System.out.println("PR[ " + i + " ] = " + PR[i]);
		}*/
		
		//============输出到文本(pagerank.txt)=============
		try{
			BufferedWriter writer = new BufferedWriter(new FileWriter("pagerank.txt"));
			
			for(int i = 0; i < nodeCount; i++){				
				writer.write(String.valueOf(i) + '\t' + String.valueOf(PR[i]) + "\n");				
			}
			
			writer.close();
			}
		catch(Exception e){
			System.out.println("Fail output [pagerank.txt]");
			e.printStackTrace();
		}
	}
	
	//==============主函数==============
	public static void main(String[] argv){
		PageRank pagerank = new PageRank();
		try{
			pagerank.readInput();
			pagerank.calPageRank();
		} catch (Exception e){
			e.printStackTrace();
		}
	}
}
