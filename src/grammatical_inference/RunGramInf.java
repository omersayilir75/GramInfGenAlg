package grammatical_inference;

import org.moeaframework.Executor;
import org.moeaframework.core.NondominatedPopulation;


public class RunGramInf {

    public static void main(String[] args) throws Exception {
        NondominatedPopulation result = new Executor()
                .withAlgorithm("NSGAII")
                .withProblemClass(GramInf.class)
                .withMaxEvaluations(1000)
                .run();
    }
}
