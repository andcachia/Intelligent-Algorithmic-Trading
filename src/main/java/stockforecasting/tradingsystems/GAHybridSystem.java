/*************
 * 
 * This system buils upon the Hybrid System by adding weightings to the different indicators. 
 * These weightings are determined by the Genetic Algorithm.
 * 
 * 
 ******/
package stockforecasting.tradingsystems;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import stockforecasting.indicators.FischerTransform;
import stockforecasting.indicators.MAIndicator;
import stockforecasting.indicators.MovingAverages;
import stockforecasting.indicators.RSI;
import stockforecasting.ml.ANN;
import stockforecasting.ml.GA_Weighting;
import stockforecasting.tradingsystems.TradingSystem.SIGNAL;
import stockforecasting.utilities.DataFormat;


public class GAHybridSystem {
    
    DataFormat df = new DataFormat();
    MovingAverages ma = new MovingAverages();
    ANN net = null;
    
    String dataFilePath;
    int arrayLength;
    int trainingSize;
    int test_size;

    int SMA_window_size = 20;
    
    double open_buy, close_buy, open_sell, close_sell, buy_profit, sell_profit, buy_ROI, sell_ROI;
    boolean buy_open, sell_open;
    
    int ma_window_size = 100;
    double[] ma_window = new double[ma_window_size];
    int rsi_window_size = 5;
    double[] rsi_window = new double[rsi_window_size];
    int ann_window_size = 20;
    double[] ann_window = new double[ann_window_size];
    
    RSI r = new RSI();
    MAIndicator mi = new MAIndicator();
    FischerTransform ft = new FischerTransform();
    FischerTransform ft_ann = new FischerTransform();
    
    SIGNAL fisherSignal;
    SIGNAL[] maSignal;
    SIGNAL rsiSignal;
    double annSignal;

    double[] input;
    double[] SMA;
    
    List<String> csvFile = new ArrayList<String>();

    GA_Weighting ga = new GA_Weighting();
    boolean training;
    
    public GAHybridSystem(String fileName, int totalArraySize, int trainingStart, int trainingSize, boolean training){

        this.training = training;

        this.dataFilePath = fileName;
        arrayLength = totalArraySize;
        this.trainingSize = trainingSize;
        test_size = arrayLength - trainingSize;

        //DeSerialize ANN
        try
        {
           FileInputStream fileIn = new FileInputStream("data/ANN_serialized_" + fileName +".ser");
           ObjectInputStream in = new ObjectInputStream(fileIn);
           net = (ANN) in.readObject();
           in.close();
           fileIn.close();
        }catch(IOException i)
        {
           i.printStackTrace();
           return;
        }catch(ClassNotFoundException c)
        {
           System.out.println("ANN class not found");
           c.printStackTrace();
           return;
        }
        
        //Import Data
        csvFile.add("data/"+ fileName +".csv");

        //double result = 0;

        double[] input_pre = df.fileToArray(csvFile.get(0), arrayLength);

        if (training){
            input = new double[trainingSize];
            System.arraycopy(input_pre, trainingStart, input, 0, trainingSize);
            SMA = ma.SMA(input, SMA_window_size);
        }
        else{
            input = new double[test_size];
            System.arraycopy(input_pre, trainingSize, input, 0, test_size);
            SMA = ma.SMA(input, SMA_window_size);
        }
    }

    public double tradingSimulation(int[] indicatorWeights){
    
        open_buy = 0;
        close_buy = 0;
        open_sell = 0;
        close_sell = 0;
        buy_open = false;
        sell_open = false;
        
        double indicators_buy_value;
        double indicators_sell_value;

        if (!training) 
            System.out.println("SMA Length: " + SMA.length);
        
        for (int i=ma_window_size; i<SMA.length;i++){
            
            indicators_buy_value = 0;
            indicators_sell_value = 0;
            
            //********* Indicators
            
            //Moving Averages Indicator
            System.arraycopy(SMA, (i-ma_window_size), ma_window, 0, ma_window_size);
            maSignal = mi.run(ma_window);
            
            //RSI Indicator
            System.arraycopy(SMA, (i-rsi_window_size), rsi_window, 0, rsi_window_size);
            rsiSignal = r.run(rsi_window);
            
            //Fischer Transform Indicator
            fisherSignal = ft.run(SMA[i],SMA[i-1]);
            
            //Place past 20 values in ann_window
            System.arraycopy(input, (i-ann_window_size), ann_window, 0, ann_window_size);
            ann_window = ft_ann.convert(ann_window);
            ann_window = ma.SMA(ann_window, 5);
            double[] annSignalTemp = net.run(ann_window);
            annSignal = (annSignalTemp[0] < 0.3) ? 0 : annSignalTemp[0] > 0.7 ? 1 : annSignalTemp[0];
            
            if (i % 15 == 0)
                mi.RegimeSwitch(ma_window);
            
            if (maSignal[0] == SIGNAL.BUY) indicators_buy_value += indicatorWeights[0];
            if (rsiSignal == SIGNAL.BUY) indicators_buy_value += indicatorWeights[1];
            if (fisherSignal  == SIGNAL.BUY) indicators_buy_value += indicatorWeights[2];
            if (annSignal  == 1.0) indicators_buy_value += indicatorWeights[3];
            
            if (maSignal[1] == SIGNAL.SELL) indicators_sell_value += indicatorWeights[4];
            if (rsiSignal == SIGNAL.SELL) indicators_sell_value += indicatorWeights[5];
            if (fisherSignal  == SIGNAL.SELL) indicators_sell_value += indicatorWeights[6];
            if (annSignal  == 0.0) indicators_sell_value += indicatorWeights[7];
            
        //    System.out.println("*** Day number "+i);
        //    System.out.println("Fisher: "+fisherSignal);
        //    System.out.println("MA Buy: "+maSignal[0]);
        //    System.out.println("MA Sell: "+maSignal[1]);
        //    System.out.println("RSI: "+rsiSignal);
            
            //If 2 out of 4 signal BUY, then open buy position
            if (indicators_buy_value >= 2)
            {
                    if (!buy_open){
                    open_buy += input[i];
                    buy_open = true;
                    //System.out.println("Open buy: "+input[i]);
                    }
            }
            else if (buy_open){
                close_buy += input[i];
                buy_open = false;
                buy_profit = close_buy - open_buy;
                buy_ROI = Math.log(close_buy / open_buy );
                //System.out.println("Close buy: "+input[i]);
            }
            
            //If 2 out of 4 signal SELL, then open sell position
            if (indicators_sell_value >= 2)
            {
                if (!sell_open){
                open_sell += input[i];
                sell_open = true;
                //System.out.println("Open sell: "+input[i]);
                }
            }
            else if (sell_open){
                close_sell += input[i];
                sell_open = false;
                sell_profit = open_sell - close_sell;
                sell_ROI = Math.log(open_sell / close_sell);
                //System.out.println("Close sell: "+input[i]);
            }

            if (!training && (i % 90 == 0)){
                System.out.println("Number of days past: "+i);
                System.out.println("Retraining...");
                int newTrainingSize = (trainingSize + i) < arrayLength ? trainingSize + i : arrayLength;
                indicatorWeights = ga.runSimulation(dataFilePath, arrayLength, newTrainingSize);
                System.out.println("Resuming...");
            }
        }

        if (!training){
            System.out.println("Hybrid Trading System Results");
            System.out.println("Buy ROI: " + Math.round(buy_ROI*10000.0)/10000.0);
            System.out.println("Sell ROI: " + Math.round(sell_ROI*10000.0)/10000.0);
            System.out.println("Total ROI: " + Math.round((buy_ROI + sell_ROI)*10000.0)/10000.0);
            System.out.println("Buy Profit: " + Math.round(buy_profit*10000.0)/10000.0);
            System.out.println("Sell Profit: " + Math.round(sell_profit*10000.0)/10000.0);
            System.out.println("Total Profit: " + Math.round((buy_profit+sell_profit)*10000.0)/10000.0);
            //System.out.println(MessageFormat.format("{0},{1},{2},{3},{4},{5}",buy_ROI, sell_ROI, buy_ROI + sell_ROI, buy_profit, sell_profit, buy_profit+sell_profit));
            System.out.println();
        }

        return buy_profit+sell_profit;
    }
}
