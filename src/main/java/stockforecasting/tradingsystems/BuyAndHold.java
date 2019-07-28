/*************
 * 
 * Determines the returns gained from a buy and hold strategy
 * 
 * 
 ******/
package stockforecasting.tradingsystems;

import java.util.ArrayList;
import java.util.List;

import stockforecasting.ml.GA_Weighting;
import stockforecasting.utilities.DataFormat;


public class BuyAndHold {
    
    String dataFilePath;
    int arrayLength;
    int trainingSize;
    int test_size;
    DataFormat df = new DataFormat();
    
    double open_buy, close_buy, open_sell, close_sell, buy_profit, sell_profit, buy_ROI, sell_ROI;
    boolean buy_open, sell_open;

    double[] input;
    double[] SMA;
    
    List<String> csvFile = new ArrayList<String>();

    GA_Weighting ga = new GA_Weighting();
    boolean testing;
    
    public BuyAndHold(String fileName, int totalArraySize, int startIndex, int endIndex){

        this.dataFilePath = fileName;
        arrayLength = totalArraySize;
        
        //Import Data
        csvFile.add("data/"+ fileName +".csv");

        double[] input = df.fileToArray(csvFile.get(0), arrayLength);

        double profit = input[endIndex-1] - input[startIndex];
        double ROI = profit / input[startIndex];

        System.out.println("Buy and Hold:");
        System.out.println("Profit/Loss: " + Math.round(profit*10000.0)/10000.0);
        System.out.println("ROI: " + Math.round(ROI*10000.0)/10000.0);

    }
}
