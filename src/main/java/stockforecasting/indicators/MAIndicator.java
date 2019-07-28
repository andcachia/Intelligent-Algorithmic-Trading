package stockforecasting.indicators;

import stockforecasting.tradingsystems.TradingSystem.SIGNAL;
import stockforecasting.utilities.Chart;
import stockforecasting.utilities.DataFormat;

public class MAIndicator {
     
    DataFormat df = new DataFormat();
    MovingAverages ma = new MovingAverages();
    Chart chart_buy = new Chart("MA Buy", 3);
    Chart chart_sell = new Chart("MA Sell", 4);

    double buy_cash = 10000;
    double sell_cash = 10000;
    double test_cash;
    double highest_profit = 0;

    double buy_rollback = 0;
    double sell_rollback = 0;
    double test_rollback = 0;

    int window_size = 100;

    int[] SMA_window_buy = {5,15,25,40,50,75,100};
    int[] EMA_window_buy = {3,5,9,11,15,20,25};

    int[] TMA_window_sell = {10,20,35,45,55,90};
    int[] BMA_window_sell = {4,7,10,12,18,22};

    int SMA_window_buy_current = 55, EMA_window_buy_current = 5, TMA_window_sell_current = 20, BMA_window_sell_current = 7;

    int l = 1000;
    boolean buy_position = false;
    boolean sell_position = false; 
    boolean test_position = false; 

    double[] SMA_buy;
    double[] EMA_buy;
    double[] TMA_sell;
    double[] BMA_sell;
    
    SIGNAL buySignal;
    SIGNAL sellSignal;
    SIGNAL[] returnSignal = new SIGNAL[2];

    
    public MAIndicator(){
        chart_buy.SetInputs("Input","SMA","EMA");
        chart_sell.SetInputs("Input","TMA","BMA");
    }
        
    public SIGNAL[] run(double[] input){

        buySignal = SIGNAL.NONE;
        sellSignal = SIGNAL.NONE;
        int count = input.length-1;
        
        //***** Buy 
  
        //Generate SMA and EMA signal value
        SMA_buy = ma.SMA(input, SMA_window_buy_current);
        EMA_buy = ma.EMA(input, EMA_window_buy_current);
        
        //If EMA value is greater, generate BUY signal
        if (EMA_buy[count]>=SMA_buy[count])
            buySignal = SIGNAL.BUY;


        //****** Sell
        
        //Generate TMA and BMA signal value
        TMA_sell = ma.TMA(input, TMA_window_sell_current);
        BMA_sell = ma.BMA(input, BMA_window_sell_current);
        
        //If TMA value is greater, generate SELL signal
        if (TMA_sell[count]>=BMA_sell[count])
            sellSignal = SIGNAL.SELL;
         
        //Add values to graph
        chart_buy.UpdateGraph(input[count], SMA_buy[count], EMA_buy[count]);
        chart_sell.UpdateGraph(input[count], TMA_sell[count], BMA_sell[count]);
        
        //Return both buy and sell signals
        returnSignal[0] = buySignal;
        returnSignal[1] = sellSignal;
        return returnSignal;
    }
    
    public void RegimeSwitch(double[] input){
        //************ Buy regime switch
        highest_profit = 0;

        for (int x=0;x<SMA_window_buy.length;x++){
            for (int y=0; y<EMA_window_buy.length;y++){

                test_cash = 10000;
                test_position = false;
                SMA_buy = ma.SMA(input, SMA_window_buy[x]);
                EMA_buy = ma.EMA(input, EMA_window_buy[y]);

                if (EMA_window_buy[y] < SMA_window_buy[x]){
                    for (int i=SMA_window_buy[x]; i<SMA_buy.length; i++){
                        if (EMA_buy[i-1]<SMA_buy[i-1] && EMA_buy[i]>=SMA_buy[i]){
                            test_position = false;
                            test_cash = test_cash - input[i];
                            test_rollback=test_cash;
                        }
                        if (EMA_buy[i-1]>SMA_buy[i-1] && EMA_buy[i]<=SMA_buy[i]){
                            test_position = true;
                            test_cash = test_cash + input[i];
                        }
                    }
                    if(test_position)
                        test_cash = test_rollback;
                    if (test_cash > highest_profit){
                            highest_profit = test_cash;
                            SMA_window_buy_current = SMA_window_buy[x];
                            EMA_window_buy_current = EMA_window_buy[y];
                    }
                    //System.out.println(EMA_window_buy[y] + " : " + SMA_window_buy[x] + " - " + test_cash);
                }
            }
        }

        //**************** Sell regime switch
        highest_profit = 0;

        for (int x=0;x<TMA_window_sell.length;x++){
            for (int y=0; y<BMA_window_sell.length;y++){

                test_cash = 10000;
                test_position = false;
                TMA_sell = ma.TMA(input, TMA_window_sell[x]);
                BMA_sell = ma.BMA(input, BMA_window_sell[y]);

                if (BMA_window_sell[y] < TMA_window_sell[x]){
                    for (int i=BMA_window_sell[x]; i<TMA_sell.length; i++){
                        if (BMA_sell[i-1]>TMA_sell[i-1] && BMA_sell[i]<=TMA_sell[i]){
                            test_position = false;
                            test_cash = test_cash + input[i];
                            test_rollback=test_cash;
                        }
                        if (BMA_sell[i-1]<TMA_sell[i-1] && BMA_sell[i]>=TMA_sell[i]){
                            test_position = true;
                            test_cash = test_cash - input[i];
                        }
                    }
                    if(test_position)
                        test_cash = test_rollback;
                    if (test_cash > highest_profit){
                            highest_profit = test_cash;
                            TMA_window_sell_current = TMA_window_sell[x];
                            BMA_window_sell_current = BMA_window_sell[y];
                    }
                    //System.out.println(TMA_window_sell[y] + " : " + BMA_window_sell[x] + " - " + test_cash);
                }
            }
        }
    }

    public void checkRollback(){
        if(buy_position)
            buy_cash = buy_rollback;
        if(sell_position)
            sell_cash = sell_rollback;
    }
    
    public void reset(){
        SMA_window_buy_current = 55; 
        EMA_window_buy_current = 5; 
        TMA_window_sell_current = 20; 
        BMA_window_sell_current = 7;
    }
}
