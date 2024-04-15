package grammatical_inference;

import org.moeaframework.Executor;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.spi.OperatorFactory;


public class RunGramInf {

    public static void main(String[] args) throws Exception {

        OperatorFactory.getInstance().addProvider(new StandardOperators());

        NondominatedPopulation result = new Executor()
                .withAlgorithm("NSGAII")
                .withProperty("populationSize", 398) // seems like 398 = 400 files
                .withProperty("operator","Graminf_1X_Crossover")
                .withProblemClass(GramInf.class)
                .withMaxEvaluations(1000)
                .distributeOnAllCores()
                .run();
        result.display();
    }
}
