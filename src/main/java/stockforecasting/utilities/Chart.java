/****
 * 
 * This chart library uses the JFreeChart library. It allows for a variable amount of input signals,
 * which are updated dynamically during run time. This generates a graph which updates dynamically 
 * with each new value input.
 * 
 */
package stockforecasting.utilities;

import stockforecasting.*;
import org.jfree.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.Serializable;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;


public class Chart implements Serializable{
    
    private XYSeriesCollection dataset = new XYSeriesCollection();
    private XYSeries[] series;
    private double PosX = 0;
    private String title ="";
    private int order; //determines where frame will be placed on screen
    
    public Chart(String title, int order){
        this.title = title;
        this.order = order;
    }
    
    //Adds new value onto chart for each series
    public void UpdateGraph(double ... input){
        int i=0;
        PosX++;
        for (double d : input){
            series[i].add(PosX,d);
            i++;
        }            
    }
    
    //Gets the number of series and sets the chart
     public void SetInputs(String ... s){
        //Screen dimensions
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        int width = gd.getDisplayMode().getWidth();
        int height = gd.getDisplayMode().getHeight();
         
        //Frame settings
        JFrame myframe2 = new JFrame();
        myframe2.setTitle("Neural Net");
        myframe2.setLayout(new BorderLayout());
        myframe2.setSize(width/2, height/2);
        myframe2.setVisible(true);
        myframe2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        switch(order){
            case 1: 
                myframe2.setLocation(0, 0);
                break;
            case 2: 
                myframe2.setLocation(width/2, 0);
                break;
            case 3: 
                myframe2.setLocation(0, height/2);
                break;
            case 4:
                myframe2.setLocation(width/2, height/2);
                break;
        }
        
        //Chart settings and variables
        JFreeChart chart = createChart(dataset, title, "Position","Value");
        series = new XYSeries[s.length];
        if (series != null){
            XYPlot plot = (XYPlot) chart.getPlot();
            plot.getRenderer().setSeriesPaint(0, Color.RED);
            if (series.length>=1)plot.getRenderer().setSeriesPaint(1, Color.GREEN);
            if (series.length>=2)plot.getRenderer().setSeriesPaint(2, Color.BLUE);
            if (series.length>=3)plot.getRenderer().setSeriesPaint(3, Color.BLACK);
        }
        for (int i=0; i<s.length; i++){
            series[i] = new XYSeries(s[i]);
            dataset.addSeries(series[i]);
        }   
        
        JPanel content = new JPanel(new BorderLayout());
        ChartPanel chartPanel = new ChartPanel(chart);
        content.add(chartPanel);
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        myframe2.setContentPane(content);

        //makes frame paint immediately
        myframe2.validate();
    }
    
    private JFreeChart createChart(final XYDataset dataset, String title, String xLabel, String yLabel) {
        
        //create the chart...
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
        
        // return chart;
        return null;
    }
}
