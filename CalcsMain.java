import java.io.*;
import java.util.*;
import java.util.concurrent.*;
public class CalcsMain {
    static final int numberOfParents = 50;
    static final int numberOfGenerations = 30;
    static final float offspringMutationChance = 0.7f;
    static final int numberOfTries = 20; // how many attempts should be made during random optimisation to find a new
    // minima
    static double[][] population;
    static double[] bestPop;
    static double bestCost;
    static float[] mutationWeighting = {1, 1, 1};
    static float[] localSearchWeighting = {1, 1, 1};
    static Random ran;
    /*
     * Creating an evolutionary algorithm system
     */
    public static long main() throws IOException {
        double[] bestCosts = new double[numberOfGenerations];
        long startTime = System.nanoTime();
        for (float i : mutationWeighting) {
            i = 1 / mutationWeighting.length;
        }
        for (float i : localSearchWeighting) {
            i = 1 / localSearchWeighting.length;
        }
        var trainingProblem = new CarPricePrediction("train");
        var bounds = CarPricePrediction.bounds();
        // create set of randomly generated parents
        population = InitialisePopulation(bounds);
        EvaluatePopulation(population, trainingProblem);
        // loop through generations
        for (int i = 0; i < numberOfGenerations; i++) {
            EvolvePopulation(trainingProblem);
            bestCosts[i] = bestCost;
            //System.out.printf("Generation %d has recorded " +
            //"a best training error of: %f%n", i+1, bestCost);
        }
        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000000;
        //System.out.printf("%nBaseline algorithm finished " +
        //"in %d milliseconds%n%n", duration);
        // Check the MSE of the best parameters on the validation problem.
        var validation_problem = new CarPricePrediction("test");
        var validation_error = validation_problem.evaluate(bestPop);
        //System.out.printf("Validation error of best solution " +
        //"found while training: %f%n", validation_error);
        return duration;
    }
    /**
     * Creates offspring from each parent, mutates each offspring and
     * replaces old population with new offspring
     *
     * @param trainingProblem The prediction class
     */
    static void EvolvePopulation(CarPricePrediction trainingProblem) {
        double[][][] parents = SelectParents(population, trainingProblem);
        List<double[]> populationList = new ArrayList<>();
        for (int i = 0; i < numberOfParents / 2; i++) {
            // create offspring
            double[][] offspringPair = OnePointCrossover(parents[i]);
            for (double[] child : offspringPair) {
                Random random = new Random();
                float mutationRoll = random.nextFloat();
                // mutate offspring
                // possibly select from a weighted list a mutation
                if (mutationRoll < offspringMutationChance) {
                    SwapMutation(child);
                }
                // select from a weighted list a local search algorithm:
                LocalSearch(child, trainingProblem);
                // pattern search
                // random optimisation
                populationList.add(child);
            }
        }
        double[][] pop = populationList.toArray(population);
        population = populationList.toArray(population);
        EvaluatePopulation(population, trainingProblem);
    }
    static void LocalSearch(double[] child, CarPricePrediction training) {
        Random random = new Random();
        float searchRoll = random.nextFloat();
        if (searchRoll < localSearchWeighting[0]) {
            PatternSearch(child, training);
        } else if (searchRoll < localSearchWeighting[0] + localSearchWeighting[1]) {
            RandomOptimisation(child, training);
        }
    }
    static void PatternSearch(double[] child, CarPricePrediction training) {
        double startingEval = training.evaluate(child);
        double[] temp = new double[child.length];
        System.arraycopy(child, 0, temp, 0, child.length);
        for (int i = 0; i < child.length; i++) {
            Probe(i, 1, child, temp, training);
            Probe(i, 0.5, child, temp, training);
            Probe(i, 0.25, child, temp, training);
            Probe(i, 0.125, child, temp, training);
        }
        double endingEval = training.evaluate(child);
        if (endingEval < startingEval && localSearchWeighting[0] < 0.85) {
            localSearchWeighting[0] += 0.05;
            localSearchWeighting[1] -= 0.05;
        }
    }
    static void Probe(int i, double probeDist, double[] child, double[] temp, CarPricePrediction
            training) {
        double tempPos = child[i] + probeDist;
        double tempNeg = child[i] - probeDist;
        temp[i] = tempPos;
        if (training.evaluate(temp) < training.evaluate(child)) {
            child[i] = tempPos;
        }
        temp[i] = tempNeg;
        if (training.evaluate(temp) < training.evaluate(child)) {
            child[i] = tempNeg;
        }
    }
    static double[] RandomOptimisation(double[] child, CarPricePrediction training) {
        double startingEval = training.evaluate(child);
        for (int i = 0; i < child.length; i++) {
            for (int x = 0; x < 10; x++) {
                double newVal = -10 + ran.nextDouble() * (10 - -10);
                child[i] = newVal;
                double eval = training.evaluate(child);
                if (eval < startingEval) {
                    break;
                }
            }
            double eval = training.evaluate(child);
            if (eval < startingEval) {
                break;
            }
        }
        double endingEval = training.evaluate(child);
        if (endingEval < startingEval && localSearchWeighting[1] < 0.85) {
            localSearchWeighting[1] += 0.05;
            localSearchWeighting[0] -= 0.05;
        }
        return new double[1];
    }
    /**
     * Evaluates a given population to find the best MSE from all available
     * candidates and saves the best
     *
     * @param _population the population
     * @param trainingProblem the training problem with evaluation method
     */
    static void EvaluatePopulation(double[][] _population, CarPricePrediction trainingProblem) {
        double _bestCost = 1000000000;
        double[] _bestPop = null;
        for (double[] doubles : _population) {
            double cost = trainingProblem.evaluate(doubles);
            if (cost < _bestCost) {
                _bestCost = cost;
                _bestPop = doubles;
            }
        }
        bestCost = _bestCost;
        bestPop = _bestPop;
    }
    /**
     * Generates a random initial population of n size within the given bounds
     *
     * @param bounds The bounds of the problem
     * @return The generated population array
     */
    // update initial parent selection method
    static double[][] InitialisePopulation(double[][] bounds) {
        double[][] pop = new double[numberOfParents][bounds.length];
        for (int i = 0; i < numberOfParents; i++) {
            var r = new Random();
            ran = r;
            pop[i] = RandomParameters(bounds, r);
        }
        return pop;
    }
    /**
     * Generates an array of random parameters within the boundary
     *
     * @param bounds The bounds of the problem
     * @param r The random instance
     * @return The array of randomly generated parameters making up one member of the population
     */
    public static double[] RandomParameters(double[][] bounds, Random r) {
        var parameters = new double[bounds.length];
        for (int j = 0; j < bounds.length; j++) {
            parameters[j] = bounds[j][0] + r.nextDouble() * (bounds[j][1] - bounds[j][0]);
        }
        return parameters;
    }
    /**
     * Fills and returns an array half the size of the input population
     * with each value containing an array of two parents, each of
     * which consists of an array of values used for evaluation
     *
     * @param _population The population set
     * @param trainingProblem The prediction class
     * @return The array of parent match arrays
     */
    static double[][][] SelectParents(double[][] _population, CarPricePrediction trainingProblem) {
        double[][][] parentMatches = new double[_population.length / 2][2][];
        int tournamentSelection = _population.length / 4;
        tournamentSelection = Math.round(tournamentSelection / 2) * 2;
        for (int i = 0; i < parentMatches.length; i++) {
            parentMatches[i][0] = TournamentSelection(_population, tournamentSelection,
                    trainingProblem);
            parentMatches[i][1] = TournamentSelection(_population, tournamentSelection,
                    trainingProblem);
        }
        return parentMatches;
    }
    /**
     * Performs tournament selection on the given population and returns the tournament winner
     *
     * @param _population An array of the current population which is legible for tournament
    selection
     * @param tournamentSelection The given number of tournament slots
     * @param trainingProblem The predicate mechanism
     * @return The tournament winner
     */
    static double[] TournamentSelection(double[][] _population, int tournamentSelection,
                                        CarPricePrediction trainingProblem) {
        double bestValue = 1000000000;
        double[] bestPop = null;
        for (int i = 0; i < tournamentSelection; i++) {
            int randomInt = ThreadLocalRandom.current().nextInt(0, _population.length);
            double cost = trainingProblem.evaluate(_population[randomInt]);
            if (cost < bestValue) {
                bestPop = _population[randomInt];
                bestValue = cost;
            }
        }
        return bestPop;
    }
    /**
     * Randomly chooses a cutting point in the array and splits both parents
     * at that point, before combining the halves of each parent with
     * the other as offspring.
     *
     * @param _population The population set
     * @return The two offspring in an array
     */
    static double[][] OnePointCrossover(double[][] _population) {
        double[][] offspringArray = new double[2][_population[0].length];
        int cutPoint = ThreadLocalRandom.current().nextInt(1, _population[0].length - 1);
        for (int i = 0; i < cutPoint; i++) {
            offspringArray[0][i] = _population[1][i];
            offspringArray[1][i] = _population[0][i];
        }
        for (int i = cutPoint; i < _population[0].length; i++) {
            offspringArray[0][i] = _population[0][i];
            offspringArray[1][i] = _population[1][i];
        }
        return offspringArray;
    }
    /**
     * Performs the swap mutation on an input candidate
     *
     * @param input The array of parameters to be mutated
     */
    static void SwapMutation(double[] input) {
        int val1 = ThreadLocalRandom.current().nextInt(0, input.length);
        int val2 = ThreadLocalRandom.current().nextInt(0, input.length);
        var tempVar = input[val1];
        input[val1] = input[val2];
        input[val2] = tempVar;
    }
    /**
     * Shuffles an array randomly
     *
     * @param array The array to be shuffled
     */
    static void shuffleArray(double[] array) {
        int index;
        double temp;
        Random random = new Random();
        for (int i = array.length - 1; i > 0; i--) {
            index = random.nextInt(i + 1);
            temp = array[index];
            array[index] = array[i];
            array[i] = temp;
        }
    }
}

