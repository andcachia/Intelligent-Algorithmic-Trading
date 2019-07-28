
package stockforecasting.indicators;

import stockforecasting.tradingsystems.TradingSystem.SIGNAL;
import stockforecasting.utilities.Chart;
import stockforecasting.utilities.DataFormat;


public class RSI {
    
    DataFormat df = new DataFormat();
    Chart chart = new Chart("RSI", 2);
    
    double[] up;
    double[] down;
    double RS;
    double RSI;
    
    public SIGNAL returnSignal;
    
    public RSI() {
        chart.SetInputs("RSI","Lowerbound","Upperbound","Mid");
    }    
    
    //Convert to RSI signal
    public double[] convert(double[] input){
        double[] rsi = new double[input.length];
        double RS_value;
        double[] up_values = new double[10];
        double[] down_values = new double[10];
        for (int i=0; i<input.length; i++){
            if (i<=10) rsi[i] = input[i];
            else{
                //check up and down values for past 10 inputs
                for (int j=9; j>0; j--){
                    if(input[i-j]>input[i-j-1])
                        up_values[10-j] = input[i-j];
                    else if (input[i-j]<input[i-j-1])
                        down_values[10-j] = input[i-j];
                }
                RS_value = df.avg(up_values) / df.avg(down_values);
                rsi[i] = 100 - 100/(1+RS_value);
            }                
        }
        return rsi;
    }
    
    //Process data dynamically one at a time
    public SIGNAL run(double[] input){
        
        returnSignal = SIGNAL.NONE;
        //initialize arrays
        up = new double[input.length-1];
        down = new double[input.length-1];
        
        //Begin processing data
        //Starts from 1 since comapring to previous value
        for (int i=1; i<input.length; i++){
            if(input[i]>input[i-1])
                up[i-1] = input[i];
            else if (input[i]<input[i-1])
                down[i-1] = input[i];
        }
        
        RS= df.avg(up)/df.avg(down);
        RSI = 100 - 100/(1+RS);
        
        if (RSI > 70)
            returnSignal = SIGNAL.SELL;
        if (RSI < 30)
            returnSignal = SIGNAL.BUY;
        
        chart.UpdateGraph(RSI, 30.0, 70.0, 50.0);
        
        return returnSignal;
    }
}
