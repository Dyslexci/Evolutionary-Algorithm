import java.io.*;
import java.util.Random;
public class Main {
    static final int numberRuns = 30;
    public static void main(String[] args) throws IOException {
        double[] costs = new double[numberRuns];
        double[][] bestCosts = new double[numberRuns][];
        long[] times = new long[numberRuns];
        for(int i = 0; i < numberRuns; i++) {
            var testSet = new BaselineMain();
            //costs[i] = testSet.main();
            times[i] = testSet.main();
            System.out.println("Iteration " + (i + 1) + ": " + costs[i]);
        }
        double totalCost = 0;
        for(double cost : costs) {
            totalCost += cost;
        }
        System.out.println();
        System.out.println();
        System.out.println("Average baseline: " + totalCost / numberRuns);
        //ConvertToCSV(costs);
        //CentralLimit(costs, totalCost / numberRuns);
        //ConvertAverageToCSV(bestCosts);
        ConvertLongToCSV(times);
    }
    static void CentralLimit(double[] data, double mean) {
        double[] differences = new double[data.length];
        for(int i = 0; i < data.length; i++) {
            differences[i] = data[i] - mean;
        }
        for(int i = 0; i < data.length; i++) {
            differences[i] = differences[i] * differences[i];
        }
        double sumOfDifferences = 0;
        for(double cost : differences) {
            sumOfDifferences += cost;
        }
        double variance = sumOfDifferences / numberRuns;
        System.out.println("Standard deviation: " + Math.sqrt(variance));
        System.out.println("Variance: " + variance);
        double top = (mean - variance);
        double bottom = (Math.sqrt(variance) / Math.sqrt(numberRuns));
        double z = top / bottom;
        System.out.println("Z: " + z);
    }
    static void ConvertToCSV(double[] data) throws IOException {
        String[] dataAsStrings = new String[data.length];
        for(int i = 0; i < data.length; i++) {
            dataAsStrings[i] = Double.toString(data[i]);
        }
        FileWriter csvWriter = new FileWriter("BaselineCSV.csv");
        csvWriter.append(String.join(",", dataAsStrings));
        csvWriter.flush();
        csvWriter.close();
    }
    static void ConvertLongToCSV(long[] data) throws IOException {
        String[] dataAsStrings = new String[data.length];
        for(int i = 0; i < data.length; i++) {
            dataAsStrings[i] = Long.toString(data[i]);
        }
        FileWriter csvWriter = new FileWriter("BaselineTimesCSV.csv");
        csvWriter.append(String.join(",", dataAsStrings));
        csvWriter.flush();
        csvWriter.close();
    }
    static void ConvertAverageToCSV(double[][] data) throws IOException {
        double[] dataAvg = new double[data[0].length];
        for(int i = 0; i < data[0].length; i++) {
            double tempSum = 0;
            for(int x = 0; x < data.length; x++) {
                tempSum += data[x][i];
            }
            dataAvg[i] = tempSum / data.length;
        }
        String[] dataAsStrings = new String[dataAvg.length];
        for(int i = 0; i < dataAvg.length; i++) {
            dataAsStrings[i] = Double.toString(dataAvg[i]);
        }
        FileWriter csvWriter = new FileWriter("BaselineCSVavg.csv");
        csvWriter.append(String.join(",", dataAsStrings));
        csvWriter.flush();
        csvWriter.close();
    }
}
