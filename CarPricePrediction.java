import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/** Car price prediction problem */
public class CarPricePrediction {
    public static final int N_INPUTS = 21;
    private static final int HIDDEN_LAYER_SIZE = 2;
    private static final int N_WEIGHTS = N_INPUTS * HIDDEN_LAYER_SIZE + HIDDEN_LAYER_SIZE * 1;
    private static final int N_BIASES = HIDDEN_LAYER_SIZE + 1;
    public static final int N_PARAMETERS = N_WEIGHTS + N_BIASES;

    /**
     * Construct a car price prediction problem instance.
     * @param dataset Specifies which dataset to use. Valid values are "train",
     *                "validation" or "test".
     */
    public CarPricePrediction(String dataset) throws IOException {
        if(Objects.equals(dataset, "train")) load_dataset("data/train.csv");
        else if(Objects.equals(dataset, "validation")) load_dataset("data/validation.csv");
        else if(Objects.equals(dataset, "test")) load_dataset("data/test.csv");
        else throw new IllegalArgumentException("Only permitted arguments for " +
                    "CarPricePrediction::CarPricePrediction are train, " +
                    "validation and test.");
    }
    /**
     * Rectangular bounds on the search space.
     * @return Vector b such that b[i][0] is the minimum permissible value of the
     * ith solution component and b[i][1] is the maximum.
     */
    public static double[][] bounds() {
        double[][] bnds = new double[N_PARAMETERS][2];
        double[] dim_bnd = {-10.0,10.0};
        for(int i = 0;i<N_PARAMETERS;++i)
            bnds[i] = dim_bnd;
        return bnds;
    }

    /**
     * Check whether the ANN parameters (biases/weights) lie within the
     * problem's feasible region.
     * There should be the correct number of biases and weights for the
     * network structure.
     * Each bias/weight should lie within the range specified by the bounds.
     */
    public boolean is_valid(double[] parameters) {
        if(parameters.length != N_PARAMETERS) return false;
        //All weights/biases lie within the bounds.
        double[][] b = bounds();
        for(int i = 0; i<N_PARAMETERS; i++)
            if(parameters[i] < b[i][0] || parameters[i] > b[i][1] )
                return false;
        return true;
    }

    /**
     * Evaluate a set of ANN parameters on the dataset used by the class
     * instance (train/validation/test).
     * @param parameters An array of size N_PARAMETERS containing the weights
     *                   and biases to be used by the ANN to predict car
     *                   prices.
     * @return The MSE of the predictions of the ANN on the selected dataset.
     */
    public double evaluate(double[] parameters) {
        double mse = 0.0;
        for(int i = 0; i < X.size(); i++){
            double y_pred = predict(X.get(i),parameters);
            mse += Math.pow(y.get(i)-y_pred,2.0);
        }
        mse /= X.size();
        return mse;
    }

    private List<double[]> X;
    private List<Double> y;

    private void load_dataset(String file) throws IOException {
        X = new ArrayList<>();
        y = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        while ((line = br.readLine()) != null) {
            String[] line_data = line.split(",");
            if(line_data.length != N_INPUTS + 1){
                throw new RuntimeException("in CarpPricePrediction::load_dataset, " +
                        "a line in the dataset contained the wrong number of" +
                        "entries.");
            }
            double[] x = new double[N_INPUTS];
            for(int i = 0; i < N_INPUTS; i++){ x[i] = Double.parseDouble(line_data[i]); }
            X.add(x);
            y.add(Double.parseDouble(line_data[N_INPUTS]));
        }
    }

    private static double predict(double[] input, double[] parameters){
        int weight_pos = 0;
        int bias_pos = N_WEIGHTS;

        double [] hidden_layer_vals = new double[HIDDEN_LAYER_SIZE];
        for (int i = 0; i < HIDDEN_LAYER_SIZE; i++){
            double weighted_sum = parameters[bias_pos];
            bias_pos++;
            for(int j = 0; j < N_INPUTS; j++){
                weighted_sum += input[j]*parameters[weight_pos];
                weight_pos++;
            }
            hidden_layer_vals[i] = relu(weighted_sum);
        }

        double output = parameters[bias_pos];
        for (int i = 0; i < HIDDEN_LAYER_SIZE; i++) {
            output += hidden_layer_vals[i] * parameters[weight_pos];
            weight_pos++;
        }

        return output;
    }

    private static double relu(double v){
        if(v < 0) return 0;
        else return v;
    }
}
