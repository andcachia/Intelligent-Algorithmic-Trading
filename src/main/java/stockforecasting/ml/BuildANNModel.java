/*
 * Trains the ANN and stores the resulting model by serializing the object to a file.
 */
package stockforecasting.ml;

import stockforecasting.indicators.FischerTransform;
import stockforecasting.indicators.MovingAverages;
import stockforecasting.ml.ANN;
import stockforecasting.utilities.DataFormat;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class BuildANNModel {
    
    int total_array;
    int target_array;
    int test_array_size;
    int array_start = 25;
    int window_size = 5;
        
    public BuildANNModel(String fileName, String dataFilePath, int _total_array, int _target_array){

        total_array = _total_array;
        target_array = _target_array;
        test_array_size = total_array - target_array;
        
        DataFormat df = new DataFormat();
        MovingAverages ma = new MovingAverages();
        FischerTransform ft = new FischerTransform();
               
        double[] movement = new double[target_array];
        double[][] input;
        double[][] target;

        //Prepare for receiving file input
        String csvFile = dataFilePath;

        double[] total_prices = df.fileToArray(csvFile, total_array);
        double[] training_prices = new double[target_array];
        double[] test_prices = new double[test_array_size];
        
        System.arraycopy(total_prices, 0, training_prices, 0, target_array);
        System.arraycopy(total_prices, target_array, test_prices, 0, test_array_size);
        
        double[] fisher = ft.convert(training_prices);
        double[] SP_avg = ma.SMA(fisher, window_size);

        for (int i=1;i<SP_avg.length;i++){
            movement[i-1] = df.checkMovement(SP_avg[i-1], SP_avg[i]);  
        }
        movement[SP_avg.length-1] = 1;

        input = df.timeSeries(SP_avg,20);
        target = df.make2D(movement);

        input = df.cropArray(input,array_start,input.length);
        target = df.cropArray(target,array_start,target.length);

        ANN net = new ANN();
        net.setHiddenNeurons(40);
        net.setErr(0.3);
        net.setLrc(0.6);
        net.setMomentum(0.1);
        net.setConvergenceLimit(Math.round(target_array * 0.2));
        net.modifyValues(false, 0.005);
        net.details(false);
        //net.printInputs(input, target);
        net.train(input, target);    
        
        
        //***** Testing Simulation
        
        //Prepare data
        double test_result = 0;
        double[] test_fisher = ft.convert(test_prices);
        double[] test_SMA = ma.SMA(test_fisher, window_size);
        double[][] test_values = df.timeSeries(test_SMA, 20);
        double[] test_movement = new double[test_array_size];
        for (int i=1;i<test_SMA.length;i++){
            test_movement[i-1] = df.checkMovement(test_SMA[i-1], test_SMA[i]);  
        }
        movement[test_SMA.length-1] = 1;
        target = df.make2D(test_movement);
        
        //Run Simulation
        for (int i=window_size; i<test_values.length; i++){
            test_result += net.test(test_values[i], target[i]);
        }
        
        //Print results
        double accuracy = ((test_array_size-test_result) / test_array_size) * 100;
        System.out.println("ANN Accuracy: "+accuracy+"%");        
        System.out.println();
        
        //**** Serialize ANN
       //if (accuracy > 76){
           try
           {
              FileOutputStream fileOut = new FileOutputStream("data/ANN_serialized_" + fileName +".ser");
              ObjectOutputStream out = new ObjectOutputStream(fileOut);
              out.writeObject(net);
              out.close();
              fileOut.close();
              //System.out.printf("Serialized data is saved in ANN_serialized_temp.ser");
           }catch(IOException i)
           {
               i.printStackTrace();
           }
       //}
            
    }   
    
}
