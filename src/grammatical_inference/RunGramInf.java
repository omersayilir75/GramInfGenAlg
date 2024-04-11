package grammatical_inference;

import org.moeaframework.Executor;
import org.moeaframework.core.NondominatedPopulation;


public class RunGramInf {

    public static void main(String[] args) throws Exception {


        NondominatedPopulation result = new Executor()
                .withAlgorithm("NSGAII")
                .withProperty("populationSize", 399)
                .withProperty("operator","1x") //change out with my own crossover.
                .withProblemClass(GramInf.class)
                .withMaxEvaluations(1000)
//                .distributeOnAllCores()
                .run();
        result.display();
    }
}
