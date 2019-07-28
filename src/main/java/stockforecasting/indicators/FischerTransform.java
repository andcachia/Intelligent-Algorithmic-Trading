/*
* The Fisher Transform applies the Fisher tranform to a data signal.
*
* A BUY singal is issued whenever the the Fisher transform signal crosses 0 
* and closes when it hits the upperbound.
* A SELL signal is issued whenever it crosses 0 downwards and closes when 
* it hits the lowerbound.
*
* This function accepts dynamic data, i.e. one input value at a time.
*
 */
package stockforecasting.indicators;

import stockforecasting.tradingsystems.TradingSystem.SIGNAL;
import stockforecasting.utilities.Chart;
import stockforecasting.utilities.DataFormat;

public class FischerTransform {
        
    DataFormat df = new DataFormat();
    Chart chart = new Chart("Fischer Transform", 1);
    
    double buy_open = 0;
    double buy_close = 0;
    double buy_rollback = 0;
    boolean b_rollback = false;
    double sell_open = 0;
    double sell_close = 0;
    double sell_rollback = 0;
    boolean s_rollback = false;
    
    boolean buy_position = false;
    boolean sell_position = false;
    
    final double upperbound = 0.1;
    final double lowerbound = -0.1;

    double highest = 0;
    double lowest = 0;
    int fischer_size;

    SIGNAL returnSignal;

    public FischerTransform() {
        chart.SetInputs("Fischer","lowerbound","upperbound","Zero");
    }    
    
    //Accepts one value at a time and dynamically processes it
    public double[] convert(double[] input){
        double[] fischer = new double[input.length];
        for (int i=0; i<input.length;i++){
            if (input[i] > highest)
                highest = input[i];
            if (input[i] < lowest)
                lowest = input[i];

            returnSignal = SIGNAL.NONE;

            double point_temp = 0.33 * 2 * (input[i]-lowest) / (highest-lowest) - 0.5;

            fischer[i] = 0.5 * Math.log((1 + point_temp)/(1-point_temp));
        }
        return fischer;
    }

    //Accepts one value at a time and dynamically processes it
    public SIGNAL run(double input, double previous_input){
        if (input > highest)
            highest = input;
        if (input < lowest)
            lowest = input;
        
        returnSignal = SIGNAL.NONE;

        double point_temp_previous = 0.33 * 2 * (previous_input-lowest) / (highest-lowest) - 0.5;
        double fischer_temp_previous = 0.5 * Math.log((1 + point_temp_previous)/(1-point_temp_previous));
        
        double point_temp = 0.33 * 2 * (input-lowest) / (highest-lowest) - 0.5;
        double fischer_temp = 0.5 * Math.log((1 + point_temp)/(1-point_temp));
        
        //Checks where fisher value lies and gives a signal accordingly
        if (fischer_temp > 0 && fischer_temp_previous < 0){
            buy_position = true;
            sell_position = false;
        }
        
        if (fischer_temp < 0 && fischer_temp_previous > 0){
            buy_position = false;
            sell_position = true;
        }
        
        if (fischer_temp > upperbound && fischer_temp_previous < upperbound) 
            buy_position = false;
    
        if (fischer_temp < lowerbound && fischer_temp_previous > lowerbound) 
            sell_position = false;
        
        //Return respective signal
        if (buy_position)
            returnSignal = SIGNAL.BUY;
        if (sell_position)
            returnSignal = SIGNAL.SELL;
        
        chart.UpdateGraph(fischer_temp, lowerbound, upperbound, 0);
        
        return returnSignal;

//        if ((fischer.get(fischer_size-1) < upperbound.get(fischer_size-1)) && (fischer.get(fischer_size) > upperbound.get(fischer_size))){
//            buy_open(input);
//        }
//        if ((fischer.get(fischer_size-1) > upperbound.get(fischer_size-1)) && (fischer.get(fischer_size) < upperbound.get(fischer_size))){
//            buy_close(input);
//        }
//        if ((fischer.get(fischer_size-1) > lowerbound.get(fischer_size-1)) && (fischer.get(fischer_size) < lowerbound.get(fischer_size))){
//            sell_open(input);
//        }
//        if ((fischer.get(fischer_size-1) < lowerbound.get(fischer_size-1)) && (fischer.get(fischer_size) > lowerbound.get(fischer_size))){
//            sell_close(input);
//        }
        
        
    }
        
    public void checkRollback(){
        if (b_rollback)
            buy_close -= buy_rollback;
        if (s_rollback)
            sell_close -= sell_rollback;
    }
    
    public void printResults(){
        System.out.println("Buy open: "+buy_open+", buy close :"+buy_close+" - Profit = "+(buy_open - buy_close));
        System.out.println("Sell open: "+sell_open+", sell close :"+sell_close+" - Profit = "+(sell_open - sell_close));
             
        //DrawInputs(fischer, upperbound, lowerbound);
    }
    
    public void reset(){
        highest = 0;
        lowest = 0;
    }    
    
    public void buy_open(double value){
        buy_open += value;
        b_rollback = false;
    }
    public void buy_close(double value){
        buy_close += value;
        buy_rollback = value;
        b_rollback = true;
    }
    public void sell_open(double value){
        sell_open += value;
        s_rollback = false;
    }
    public void sell_close(double value){
        sell_close += value;
        sell_rollback = value;
        s_rollback = true;
    }

}
