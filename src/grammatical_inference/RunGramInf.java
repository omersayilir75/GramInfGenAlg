package grammatical_inference;

import org.moeaframework.Executor;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.spi.OperatorFactory;


public class RunGramInf {

    public static void main(String[] args) throws Exception {

        OperatorFactory.getInstance().addProvider(new StandardOperators());

        System.out.println("GA:");



        System.out.println("GramInf_Mut:");
        NondominatedPopulation result0 = new Executor()
                .withAlgorithm("GA")
                .withProperty("populationSize", 398)
                .withProperty("operator","GramInf_Mut")
                .withProblemClass(GramInf_Weighted.class)
                .withMaxEvaluations(4400)
                .distributeOnAllCores()
                .run();
        result0.display();

        System.out.println("GramInf_1X_Crossover:");
        NondominatedPopulation result1 = new Executor()
                .withAlgorithm("GA")
                .withProperty("populationSize", 398)
                .withProperty("operator","GramInf_1X_Crossover")
                .withProblemClass(GramInf_Weighted.class)
                .withMaxEvaluations(4400)
                .distributeOnAllCores()
                .run();
        result1.display();

        System.out.println("GramInf_Mut_Repair:");
        NondominatedPopulation result2 = new Executor()
                .withAlgorithm("GA")
                .withProperty("populationSize", 398)
                .withProperty("operator","GramInf_Mut_Repair")
                .withProblemClass(GramInf_Weighted.class)
                .withMaxEvaluations(4400)
                .distributeOnAllCores()
                .run();
        result2.display();

        System.out.println("GramInf_Mut_Repair+GramInf_Mut:");
        NondominatedPopulation result3 = new Executor()
                .withAlgorithm("GA")
                .withProperty("populationSize", 398)
                .withProperty("operator","GramInf_Mut_Repair+GramInf_Mut")
                .withProblemClass(GramInf_Weighted.class)
                .withMaxEvaluations(4400)
                .distributeOnAllCores()
                .run();
        result3.display();

        System.out.println("GramInf_1X_Crossover+GramInf_Mut:");
        NondominatedPopulation result4 = new Executor()
                .withAlgorithm("GA")
                .withProperty("populationSize", 398)
                .withProperty("operator","GramInf_1X_Crossover+GramInf_Mut")
                .withProblemClass(GramInf_Weighted.class)
                .withMaxEvaluations(4400)
                .distributeOnAllCores()
                .run();
        result4.display();


        System.out.println("GramInf_1X_Crossover+GramInf_Mut_Repair:");
        NondominatedPopulation result5 = new Executor()
                .withAlgorithm("GA")
                .withProperty("populationSize", 398)
                .withProperty("operator","GramInf_1X_Crossover+GramInf_Mut_Repair")
                .withProblemClass(GramInf_Weighted.class)
                .withMaxEvaluations(4400)
                .distributeOnAllCores()
                .run();
        result5.display();

        System.out.println("GramInf_1X_Crossover+GramInf_Mut_Repair+GramInf_Mut:");
        NondominatedPopulation result6 = new Executor()
                .withAlgorithm("GA")
                .withProperty("populationSize", 398)
                .withProperty("operator","GramInf_1X_Crossover+GramInf_Mut_Repair+GramInf_Mut")
                .withProblemClass(GramInf_Weighted.class)
                .withMaxEvaluations(4400)
                .distributeOnAllCores()
                .run();
        result6.display();


        System.out.println("NSGAII:");


        System.out.println("GramInf_Mut:");
        NondominatedPopulation result7 = new Executor()
                .withAlgorithm("NSGAII")
                .withProperty("populationSize", 398)
                .withProperty("operator","GramInf_Mut")
                .withProblemClass(GramInf.class)
                .withMaxEvaluations(4400)
                .distributeOnAllCores()
                .run();
        result7.display();

        System.out.println("GramInf_1X_Crossover:");
        NondominatedPopulation result8 = new Executor()
                .withAlgorithm("NSGAII")
                .withProperty("populationSize", 398)
                .withProperty("operator","GramInf_1X_Crossover")
                .withProblemClass(GramInf.class)
                .withMaxEvaluations(4400)
                .distributeOnAllCores()
                .run();
        result8.display();

        System.out.println("GramInf_Mut_Repair:");
        NondominatedPopulation result9 = new Executor()
                .withAlgorithm("NSGAII")
                .withProperty("populationSize", 398)
                .withProperty("operator","GramInf_Mut_Repair")
                .withProblemClass(GramInf.class)
                .withMaxEvaluations(4400)
                .distributeOnAllCores()
                .run();
        result9.display();

        System.out.println("GramInf_Mut_Repair+GramInf_Mut:");
        NondominatedPopulation result10 = new Executor()
                .withAlgorithm("NSGAII")
                .withProperty("populationSize", 398) 
                .withProperty("operator","GramInf_Mut_Repair+GramInf_Mut")
                .withProblemClass(GramInf.class)
                .withMaxEvaluations(4400)
                .distributeOnAllCores()
                .run();
        result10.display();

        System.out.println("GramInf_1X_Crossover+GramInf_Mut:");
        NondominatedPopulation result11 = new Executor()
                .withAlgorithm("NSGAII")
                .withProperty("populationSize", 398)
                .withProperty("operator","GramInf_1X_Crossover+GramInf_Mut")
                .withProblemClass(GramInf.class)
                .withMaxEvaluations(4400)
                .distributeOnAllCores()
                .run();
        result11.display();


        System.out.println("GramInf_1X_Crossover+GramInf_Mut_Repair:");
        NondominatedPopulation result12 = new Executor()
                .withAlgorithm("NSGAII")
                .withProperty("populationSize", 398)
                .withProperty("operator","GramInf_1X_Crossover+GramInf_Mut_Repair")
                .withProblemClass(GramInf.class)
                .withMaxEvaluations(4400)
                .distributeOnAllCores()
                .run();
        result12.display();

        System.out.println("GramInf_1X_Crossover+GramInf_Mut_Repair+GramInf_Mut:");
        NondominatedPopulation result13 = new Executor()
                .withAlgorithm("NSGAII")
                .withProperty("populationSize", 398)
                .withProperty("operator","GramInf_1X_Crossover+GramInf_Mut_Repair+GramInf_Mut")
                .withProblemClass(GramInf.class)
                .withMaxEvaluations(4400)
                .distributeOnAllCores()
                .run();
        result13.display();


    }
}
