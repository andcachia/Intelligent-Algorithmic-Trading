//**********************************************************************
//Neural Network Toolbox
//Created by Andrew Cachia
//
//To use, simply enter any 2D array as input, and another as the target, 
//where each column contains multiple samples of a particular criteria, and each 
//row contains the inputs to the network associated with the particular target.
//
//Input and target length (number of rows) must be equal.
//
//Default parameters may be changed using public setters.
//
//**********************************************************************

package stockforecasting.ml;

import stockforecasting.*;
import stockforecasting.utilities.Chart;

import java.awt.BorderLayout;
import java.io.Serializable;
import static java.lang.Math.round;
import java.util.Arrays;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class ANN implements Serializable{

    private static final long serialVersionUID = 6673423048343986258L;
    
    private Random rand = new Random();

    private int counter = 0;    
    private double[] hidden;
    private double[][] OUT;
    private double[] error;
    private double[] delta;
    private double[][] WH;
    private double[][] WO;
    //Weights to hold delta values used for momentum in back propogation
    private double[][] WH_delta;
    private double[][] WO_delta;
    
    private double newDelta = 0;
    private int badEpoch = 1000;
    private boolean badFact = false;
    
    //default values, can be changed with public setters
    private double lrc = 0.2;
    private double err = 0.2;
    private double momentum = 0.9;
    private int hidden_neurons = 10;
    private double testing_ratio = 0.2;
    private boolean details = false;
    private boolean modify_values = false;
    private double modify_rate = 0.01;
    private double convergence_limit = 0;
    
    private Chart myChart = new Chart("ANN", 1);
       
    private JFreeChart chart;
    private JFreeChart chart2;
    private XYSeries series1;
    private XYSeries series2;
    private XYSeries series3;
    private XYSeriesCollection dataset;
    private XYSeriesCollection dataset2;
    private double PosX = 0;
    private double PosY = 0;
    
    JFrame test_frame = new JFrame("Testing Results");
    JTextArea textArea = new JTextArea();    
        
    
    //***************************** Training **********************************           
    public double[][] train(double[][] input, double[][] target){
        
        //myChart.SetInputs(3);
        
        //Test Frame settings
        test_frame.setSize(800, 300);
        test_frame.setVisible(true);
        test_frame.add(textArea);
        
        if (input.length != target.length)
            System.out.println("Input and target must be of same length");
        else{
            
            setChart();       
            DrawInputs(input);
            
//            for (int i=0; i<input.length; i++){
//                for (int j=0; j<input[0].length; j++){
//                    input[i][j] = sigmoid(input[i][j]);
//                }
//            }
            
            hidden = new double[hidden_neurons];
            OUT = new double[target.length][target[0].length];
            error = new double[target[0].length];

            //randomize weights
            WH = randomizeWeights(input[0].length,hidden_neurons,-1,1);
            WO = randomizeWeights(hidden_neurons,target[0].length,-1,1);
            
            //These are initially set to 0
            WH_delta = randomizeWeights(input[0].length,hidden_neurons,0,0);
            WO_delta = randomizeWeights(hidden_neurons,target[0].length,0,0);

            shuffleArray(input, target);
            
            //split number between testing and training based on ratio
            int n_training = (int) round(input.length * (1 - testing_ratio));

            
            //***********Begin training neural network
            while (badEpoch > convergence_limit && counter < 5000){
                if (details) System.out.println("\n******  Epoch Number: " + counter + " *********");
                badEpoch = 0;
                counter++;
                if (modify_values){
                    if (err < 0.3) err += modify_rate;
                    if (momentum > 0.1) momentum -= modify_rate;
                }
                for (int count=0;count<n_training;count++){
                    badFact = false;
                    delta = new double[target[0].length];
                    
                    //program carries out first forward pass
                    hidden = forwardPass(input[count], WH, false);
                    OUT[count] = forwardPass(hidden, WO, true);
                                        
                    //computes error value and delta for each output neuron
                    if (details) System.out.println("Vector: " + count);
                    for (int i=0;i<target[0].length;i++){
                        error[i] = (target[count][i] - OUT[count][i]);
                        delta[i] = OUT[count][i]*(1-OUT[count][i])*error[i];
                        if (Math.abs(error[i]) > err) badFact = true;
                        if (details){
                            System.out.print("\tOUT: " + OUT[count][i] + "\t ");
                            System.out.print("\tTarget: " + target[count][i] + "\t ");                    
                            System.out.print("\tError: " + error[i] + "\t ");                    
                            System.out.println("\tDelta: " + delta[i]);
                        }
                    }

                    if (badFact){
                        badEpoch++;
                        //passes delta values and performs first backward pass
                            backwardPass(WO, WO_delta, hidden, delta);
                        //computes new delta values for hidden layer, using old delta values
                            delta = newDelta(delta, WO, hidden);
                        //performs second backward pass using new delta values
                            backwardPass(WH, WH_delta, input[count], delta);
                    }
                }
                if (details){
                    System.out.println("Layer 1 weights: ");
                    printWeights(WH);
                    System.out.println("Layer 2 weights: ");
                    printWeights(WO);
                }
                updateGraph();
            } 
            
            System.out.println("Number of epochs: "+counter);

            //Run test with remaining testing values
            //run(Arrays.copyOfRange(input,n_training,input.length), Arrays.copyOfRange(target,n_training,target.length));
            
            return OUT;
        }
        return null;
    }

    
    //****************************** Testing *********************************
    public int test(double[] input, double[] target){
        
        double[] hid;
        double[] out;
        double total_err = 0;
        int wrong = 0;
        int j = 0;        

        //System.out.print("Input: " + printArray(input));

        //almost same process as before, however only forward pass is done (no backward pass)
        hid = forwardPass(input,WH, false);
        out = forwardPass(hid, WO, true);

        //System.out.print("\tExpected: " + printArray(target));

        //System.out.print("\tActual:  ");
            for(double d : out){
                //System.out.print(d + "   ");
                total_err += target[j] - out[j];
                if (Math.abs(total_err) > 0.4) wrong++;
                j++;
            }
        //System.out.print("\n");
        
        return wrong;
    }

    
    //************************* Run **********************
    public double[] run(double[] input){
        double[] hid;
        double[] out;     

        hid = forwardPass(input,WH,false);
        out = forwardPass(hid,WO,true);

        return out;
    }
    
    
    //******************************** Functions ****************************
    
    // Implementing Fisher-Yates shuffle
    private void shuffleArray(double[][] ar1, double[][] ar2)
    {
      Random rnd = new Random();
      for (int i = ar1.length - 1; i > 0; i--)
      {
        int index = rnd.nextInt(i + 1);
        // Simple swap
        double[] a0 = ar1[index];
        double[] a1 = ar2[index];
        ar1[index] = ar1[i];
        ar2[index] = ar2[i];
        ar1[i] = a0;
        ar2[i] = a1;
      }
    }
    
    private double[][] randomizeWeights(int i, int j, double min, double max){
        double[][] c = new double[i][j];
        for (int cnt1 = 0; cnt1<i; cnt1++){
            for (int cnt2 = 0; cnt2<j; cnt2++){
                c[cnt1][cnt2] = min + (rand.nextDouble()*(max - min));
            }
        }
        return c;
    }

    
    private double[] forwardPass(double[] in_vector, double[][] WH, boolean hidden){
        double[] out_vector = new double[WH[0].length];
        for(int i=0;i<out_vector.length;i++){
            double x=0;
            for(int j=0;j<in_vector.length;j++){
                x += (in_vector[j] * WH[j][i]);
            }
            out_vector[i] = x;
        }
        
        for(int n=0;n<out_vector.length;n++){
            if (hidden)
                out_vector[n] = sigmoid(out_vector[n]);
            else
                out_vector[n] = sigmoid(out_vector[n]);
        }
        return out_vector;
    }
    
    private void backwardPass(double[][] w, double[][] w_delta, double[] layer_in, double[] delta){
        double temp;
        for (int i=0; i<layer_in.length; i++){
            for(int j=0;j<w[0].length;j++){
                temp = (lrc * layer_in[i] * delta[j]) + (momentum * w_delta[i][j]);
                w[i][j] = w[i][j] + temp;
                w_delta[i][j] = temp;
            }
        }
    }
    
    //Calculate hidden layer delta values during back-propogation
    private double[] newDelta(double[] delta, double[][] WO, double[] hidden){
        int dlength = hidden.length;
        double[] d = new double[dlength];
        for (int i=0;i<dlength;i++){            
            for (int j=0;j<delta.length;j++){
                d[i] += delta[j] * WO[i][j];
            }
            d[i] = d[i] * (hidden[i] * (1 - hidden[i]));
        }
        return d;
    }
    
    //Squashing function
    private double sigmoid(double d){
        return (1 / (1 + Math.exp(-d)));
    }
    
    private double tanh(double d){
        return (Math.exp(d) - Math.exp(-d))/(Math.exp(d) + Math.exp(-d));
    }
    
    
    //***************************** Display functions ************************
    public void printWeights(double[][] w){
        for (int i=0;i<w.length;i++){
            for(int j=0;j<w[0].length;j++){
                System.out.print(w[i][j] + " ");
            }
            System.out.println("");
        }
    }
    
    public void printInputs(double[][] input, double[][] target){
        for (int i=0; i<input.length;i++){
            for (int j=0; j<input[0].length; j++)
                System.out.print(input[i][j] + " - ");
            System.out.println(" | ");
            for (int j=0; j<target[0].length; j++)
                System.out.print(target[i][j] + " - ");
            System.out.println("");
        }
    }
    
    private String printArray(double[] d){
        String s = "";
        for (int i=0;i<d.length;i++){
            s += d[i] + ",";
        }
        return s;
    }
    
    
    
    //**************************** Chart Functions ***************************
    private void setChart(){
        //Frame settings
        JFrame myframe = new JFrame();
        myframe.setTitle("Neural Net");
        myframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        myframe.setLayout(new BorderLayout());
        myframe.setSize(1200, 650);
        myframe.setVisible(true);

        //Chart settings and variables
        series1 = new XYSeries("First");
        dataset = new XYSeriesCollection();
        dataset.addSeries(series1);
        chart = createChart(dataset, "Bad Facts Over Time","Number of Epochs", "Bad Facts");
        JPanel content = new JPanel(new BorderLayout());
        ChartPanel chartPanel = new ChartPanel(chart);
        content.add(chartPanel);
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        myframe.setContentPane(content);

        //makes frame paint immediately
        myframe.validate();
    }
    
    private void DrawInputs(double[][] input){
        
        Chart myChart2 = new Chart("ANN Inputs", 2);
        myChart2.SetInputs("S&P","NSDQ","Recession");

        for (int i=0;i<input.length;i++){
            myChart2.UpdateGraph(input[i][0]);
        }
               
    }
    
    private JFreeChart createChart(final XYDataset dataset, String title, String xLabel, String yLabel) {
        
        // create the chart...
        final JFreeChart chart = ChartFactory.createXYLineChart(
            title,                      // chart title
            xLabel,                     // x axis label
            yLabel,                     // y axis label
            dataset,                    // data
            PlotOrientation.VERTICAL,
            true,                     // include legend
            true,                     // tooltips
            false                     // urls
        );
        
        return chart;
    }
    
    private void updateGraph(){      
            PosX++;
            PosY = badEpoch;
            series1.add(PosX, PosY);
    }
    
    //**************************** Public Setters ***************************

    public void setLrc(double lrc) {
        this.lrc = lrc;
    }

    public void setErr(double err) {
        this.err = err;
    }

    public void setMomentum(double momentum) {
        this.momentum = momentum;
    }

    public void setHiddenNeurons(int hidden_neurons) {
        this.hidden_neurons = hidden_neurons;
    }

    public void setTestingRatio(double testing_ratio) {
        this.testing_ratio = testing_ratio;
    }
    
    public void details(boolean details) {
        this.details = details;
    }

    public void modifyValues(boolean modify_values, double modify_rate) {
        this.modify_values = modify_values;
        this.modify_rate = modify_rate;
    }

    public void setConvergenceLimit(double convergence_limit) {
        this.convergence_limit = convergence_limit;
    }
    
}
