package stockforecasting.ml;

import java.util.Random;

import org.jenetics.IntegerGene;
import org.jenetics.MeanAlterer;
import org.jenetics.Mutator;
import org.jenetics.Optimize;
import org.jenetics.Phenotype;
import org.jenetics.engine.Engine;
import org.jenetics.engine.EvolutionResult;
import org.jenetics.engine.EvolutionStatistics;
import org.jenetics.engine.codecs;
import org.jenetics.util.IntRange;

import stockforecasting.tradingsystems.GAHybridSystem;

public class GA_Weighting {

    public static final int numOfRandomTests = 10;
    public static String Filepath;
    public static int TotalArray;
    public static int[] TrainingSize = new int[numOfRandomTests];
    public static int[] TrainingStart = new int[numOfRandomTests];
    private static Random rand = new Random();

    private static double fitness(final int[] x){

        double total = 0;
        for (int i=0; i<numOfRandomTests; i++){
            GAHybridSystem hybridSystem = new GAHybridSystem(Filepath, TotalArray, TrainingStart[i], TrainingSize[i], true);
            double result = hybridSystem.tradingSimulation(x);
            total += (result * (1 + i/5));
        }

        return total;
    }

    public int[] runSimulation(String filepath, int totalArraySize, int trainingSize) {

        Filepath = filepath;
        TotalArray = totalArraySize;

        //First sample should include most recent data
        TrainingStart[0] = trainingSize-200;
        TrainingSize[0] = 199;

        // Create 9 other random samples to be used as testing periods
        for (int i=1; i<numOfRandomTests; i++){
            TrainingStart[i] = rand.nextInt(trainingSize-200);
            TrainingSize[i] = rand.nextInt(trainingSize - TrainingStart[i] - 51) + 50;

            // System.out.println("Training start: " + TrainingStart[i]);
            // System.out.println("Training size: " + TrainingSize[i]);
        }

        int ChromosoneLength = 8;

        final Engine<IntegerGene, Double> engine = Engine
                .builder(
                    GA_Weighting::fitness,
                    codecs.ofVector(IntRange.of(1,2), ChromosoneLength)
                )
                .populationSize(20)
                .optimize(Optimize.MAXIMUM)
                .alterers(
                    new Mutator<>(0.1),
                    new MeanAlterer<>(0.6)
                )
                .build();
                    
        final EvolutionStatistics<Double, ?> statistics = EvolutionStatistics.ofNumber();

        final Phenotype<IntegerGene, Double> best = engine.stream()
                .peek(statistics)
                .limit(25)
                .collect(EvolutionResult.toBestPhenotype());
            
        //System.out.println(statistics);
        //System.out.println(best);
        //System.out.println();

        int[] results = new int[ChromosoneLength];
        for (int i=0; i<results.length; i++){
            results[i] = best.getGenotype().getChromosome().getGene(i).intValue();
        }

        return results;
    }
}