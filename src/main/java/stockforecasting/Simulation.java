package stockforecasting;

import stockforecasting.ml.BuildANNModel;
import stockforecasting.ml.GA_Weighting;
import stockforecasting.tradingsystems.BuyAndHold;
import stockforecasting.tradingsystems.GAHybridSystem;
import stockforecasting.tradingsystems.HybridSystem;
import stockforecasting.tradingsystems.TradingSystem;

public class Simulation {

    public Simulation(){

        RunSimulation("btc_modified", 2074, 1709);
        RunSimulation("ltc_modified", 2074, 1709);
        RunSimulation("FTSE_modified", 3535, 3282);
        RunSimulation("GM_modified", 2043, 1791);
        RunSimulation("GOOG_modified", 3525, 3273);
        RunSimulation("GSPC_modified", 3524, 3273);
        RunSimulation("KO_modified", 3524, 3273);
        RunSimulation("eth_modified", 1244, 879);
        RunSimulation("NFLX_modified", 3524, 3273);
    }

    public void RunSimulation(String fileName, int total_array_size, int training_size){

        System.out.println(">>> " + fileName);

        String dataFilePath = "data/" + fileName + ".csv";

        // Run buy and hold strategy
        new BuyAndHold(fileName, total_array_size, training_size, total_array_size);

        // Train ANN model for each asset
        new BuildANNModel(fileName, dataFilePath, total_array_size, training_size);

        // Run algorithmic trading strategy
        new TradingSystem(dataFilePath, total_array_size, training_size);

        // Run hybrid trading strategy
        new HybridSystem(fileName,total_array_size, training_size);

        // Traing Genetic Algorithms and get best performing chromosome
        GA_Weighting ga = new GA_Weighting();
        int[] indicatorWeights = ga.runSimulation(fileName,total_array_size, training_size);

        // Run hybrid trading strategy with GA
        GAHybridSystem hybridSystem = new GAHybridSystem(fileName,total_array_size, 0, training_size, false);
        hybridSystem.tradingSimulation(indicatorWeights);
    }

}