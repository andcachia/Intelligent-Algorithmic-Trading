
package stockforecasting.utilities;

import stockforecasting.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


public class DataFormat {
       
    public double[] fileToArray(String csvFile, int l){
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";
        String[] parts;
        double[] prices = new double[l];
        
        try{
            br = new BufferedReader(new FileReader(csvFile));
            
            int count = 0;

             //Receive values from file and store them in a 2d array, with each row contianing input and movement
            while ((line = br.readLine()) != null && (count < prices.length)) {
                    // use comma as separator to split between input and movement
                    parts = line.split(cvsSplitBy);
                    prices[count] = Double.parseDouble(parts[1]);

                    count++;
            }
                
        } catch (Exception e){
                e.printStackTrace();
	} finally {
		if (br != null) {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}  
        return prices;
    }
    
    public static double[] stringToArrayDouble(String s){
        int count = 0;
        double[] d = new double[s.length()];
        while (count<s.length()){
            d[count] = Double.parseDouble(String.valueOf(s.charAt(count)));
            count++;
        }
        return d;
    }        
    
    public static double checkMovement(double current_price, double new_price){
        if (new_price > current_price) return 1;
        else if (new_price < current_price) return 0;
        else return 0.5;
    }
    
    public static double[][] merge(double[] ... a){
        double[][] temp = new double[a[0].length][a.length];
        int column = 0;
        for (double[] d:a){
            if (d.length != a[0].length){
                System.out.println("Arrays must be of same length");
                break;
            }
            else{
                for (int row=0;row<d.length;row++){
                    temp[row][column] = d[row];
                }
                column++;
            }
                
        }
        return temp;
    }
    
    public static double[][] concat(double[][] a, double[][] b){
        double[][] temp = new double[a.length][a[0].length+b[0].length];
        if (a.length == b.length){
            for (int i=0; i<a.length;i++){
                for (int j=0;j<a[0].length;j++){
                    temp[i][j] = a[i][j];
                }
                for (int k=0;k<b[0].length;k++){
                    temp[i][k+a[0].length] = b[i][k];
                }
            }
        }
        else System.out.println("Arrays must be of same size");
        return temp;
    }
    
    public static double[][] make2D(double[] a){
        double[][] temp = new double[a.length][1];
        for (int i=0; i<a.length;i++){
            temp[i][0] = a[i];
        }
        return temp;
    }
    
    public static double[][] timeSeries(double[] a, int n){
        double[][] temp = new double[a.length][n];
        for(int i=0; i<a.length; i++){
            for(int j=0; j<n; j++){
                if (i>=j) temp[i][j] = a[i-j];
                else temp[i][j] = a[i];
            }
        }
        return temp;
    }
    
    public static double[] scale(double[] series, double min, double max, int lower_limit, int upper_limit){
        double temp[] = new double[series.length];
        for (int i=0; i<series.length; i++){
            temp[i] = ((series[i] - min) / (max - min)) * (upper_limit-lower_limit) + lower_limit;
        }
        return temp;
    }
    
    public static double[] transform(double[] series, int n){
        double[] temp = new double[series.length];
        for (int i=n;i<series.length; i++){
            int ans=0;
            if (i<n) temp[i] = 0;
            else for (int j=0;j<n;j++){
                if (series[i-j] > series[i-j-1])
                    ans++;
                else
                    ans--;
            }
            temp[i] = ans;
        }
        return temp;
    }
    
    public static double[][] cropArray(double[][] a, int from, int to){
        double[][] temp = new double[to-from][a[0].length];
        for (int i=from;i<to;i++){
            System.arraycopy(a[i], 0, temp[i-from], 0, a[0].length);
        }
        return temp;
    }
    
    
   public double avg(double[] input){
       double avg = 0;
       for (int i=0; i<input.length;i++){
           avg += input[i];
       }
       return (avg / input.length);
   }
    
}
