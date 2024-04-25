package grammatical_inference;

import org.moeaframework.Executor;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.spi.OperatorFactory;


public class RunGramInf {

    public static void main(String[] args) throws Exception {

        OperatorFactory.getInstance().addProvider(new StandardOperators());

        System.out.println("crossover+mutation:");
        NondominatedPopulation result = new Executor()
                .withAlgorithm("NSGAII")
                .withProperty("populationSize", 398) // seems like 398 = 400 files
                .withProperty("operator","Graminf_1X_Crossover+Graminf_Mut")
//                .withProperty("operator","")
                .withProblemClass(GramInf.class)
                .withMaxEvaluations(1000)
//                .distributeOnAllCores()
                .run();
        result.display();

        System.out.println("just mutation:");
        NondominatedPopulation result2 = new Executor()
                .withAlgorithm("NSGAII")
                .withProperty("populationSize", 398) // seems like 398 = 400 files
                .withProperty("operator","Graminf_Mut")
//                .withProperty("operator","")
                .withProblemClass(GramInf.class)
                .withMaxEvaluations(1000)
//                .distributeOnAllCores()
                .run();
        result2.display();

        System.out.println("just crossover:");
        NondominatedPopulation result3 = new Executor()
                .withAlgorithm("NSGAII")
                .withProperty("populationSize", 398) // seems like 398 = 400 files
                .withProperty("operator","Graminf_1X_Crossover")
//                .withProperty("operator","")
                .withProblemClass(GramInf.class)
                .withMaxEvaluations(1000)
//                .distributeOnAllCores()
                .run();
        result3.display();


    }
}
