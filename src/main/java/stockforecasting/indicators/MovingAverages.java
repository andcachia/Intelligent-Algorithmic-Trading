/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package stockforecasting.indicators;

import stockforecasting.*;

public class MovingAverages {
                
    public double[] SMA(double[] input, int window_size) {
        double[] SMA = new double[input.length];
        double ans;
        System.arraycopy(input, 0, SMA, 0, window_size);
        for (int i=window_size;i<input.length;i++){
            ans = 0;
            for(int j=i-window_size+1; j<=i; j++){
                ans += input[j];
            }
            SMA[i] = ans / window_size;
        }
        return SMA;
    }

    public double[] EMA(double[] input, int window_size) {
        double[] EMA = new double[input.length];
        double ans;
        int denom = sumUpTo(window_size);
        System.arraycopy(input, 0, EMA, 0, window_size);
        for (int i=window_size;i<input.length;i++){
            ans = 0;
            for(int j=0; j<window_size; j++){
                ans += (window_size - j) * input[i-j];
            }
            EMA[i] = ans / denom;
        }
        return EMA;
    } 
    
    public double[] TMA(double[] input, int window_size){
        double[] TMA = new double[input.length];
        int max = (int)Math.ceil((window_size+1)/2);
        double[] temp = SMA(input, max);
        TMA = SMA(temp,max);
        return TMA;
    }
    
    public double[] BMA(double[] input, int window_size){
        double[] BMA = new double[input.length];
        int[] coeff = new int[window_size];
        int level = window_size - 1;
        int num = 1;
        int denom = 0;
        double ans;
        System.arraycopy(input, 0, BMA, 0, window_size);
        for (int k=0;k<window_size;k++){
            coeff[k] = num;
            denom += num;
            num = num * (level - k)/(k + 1);           
        }        
        for (int i=window_size; i<input.length;i++){
            ans = 0;
            for (int k=0;k<window_size;k++){
                ans += coeff[k] * input[i-k];
                //System.out.print(coeff[k] + "*" + input[i-k] + " + ");
            }
            BMA[i] = ans / denom;
            //System.out.println("Total: " + BMA[i]);
        }        
        return BMA;        
    }
    
    
    
    
    //************************************
    private int sumUpTo(int n){
        if (n>0)
            return n + sumUpTo(n-1);
        else
            return 0;
    }
    
}
