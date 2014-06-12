package gbt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class Test {
	public static void main(String[] args) throws Exception{
		Gbrt curGbrt = new Gbrt();
		int num = 110;
		double[][] x = new double [num][7];
		double[] y = new double[num];
		String filename  = "train.txt";
		try{
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filename)), "UTF-8"));
			String cur;
			int count = 0;
			while ((cur = reader.readLine()) != null){
				if (cur.substring(0, 1).equals("#")){
					continue;
				}
				System.out.println(cur);
				String[] parts = cur.split(" ");
				for (int i = 0; i < 7; i++){
					x[count][i] = new Double(parts[i]);
				}
				//System.out.println(parts[7]);
				y[count] = new Double(parts[7]);
				
				count += 1;
				//System.out.println("ok");
			}
			reader.close();
		}catch (Exception e) {
			e.printStackTrace();
			System.out.println("Input Error...");
		}
		
		/*System.out.println("\nAbout x[][]");
		for (int i = 0; i < 6; i++){
			for (int j = 0; j < 7; j++){
				System.out.print(x[i][j] + " ");
			}
			System.out.print(": " + y[i]);
			System.out.println("");
		}*/
		
		curGbrt.trainParameters(x, y);
		//double[] t = {0.0, 0.0, 1.5861807, 2.2207792, 0.0, 0, 7.445471510436619E-7};
		//double[] t = {0.0, 0.0, 0.0, 0.0, 0.009047843, 1314577316031.0, 1.396809807374666E-6};	//1 
		//double[] t = {1.2024678, 0.0, 0.9157492, 1.6662724, 0.25826344, 0, 3.1601350656274008E-6};	//5
		double[] t = {1.4098897, 0.0, 1.312334, 2.805952, 0.42993873, 1274750514786.0, 5.263973434921354E-6}; //3
		double now = curGbrt.test(t);
		System.out.println(now);
		
		
		/*double[][] x = new double [100][3];
		for (int i = 0; i < 100; i++){
			x[i][0] = i;
			x[i][1] = i * 100;
			x[i][2] = i * 10000000;
		}
		double[] y = new double [100];
		for (int i = 0; i < 100; i++){
			y[i] = i;
		}
		testGbrt.trainParameters(x, y);
		
		Gbrt.writeToFile(testGbrt, "out.txt");
		Gbrt curGbrt = Gbrt.readFromFile("out.txt");
		
		double[] t = {10, 10, 10};
		double now = curGbrt.test(t);
		System.out.println(now);*/
	}
}
