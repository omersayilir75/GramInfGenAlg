package test;

import grammatical_inference.GramInf_1X;
import org.moeaframework.core.Solution;
import grammatical_inference.GrammarRepresentation;
import org.junit.jupiter.api.Test;

import java.util.TreeMap;

class GramInf_1XTest {
    // mainly for debugging
    @Test
    void testEvolve() {
        GramInf_1X crossover = new GramInf_1X();
        
        TreeMap<String, String> treemap1 = new TreeMap<>();
        treemap1.put("r0", "r1 (r2)* (r3)+ (r4)*");
        treemap1.put("r1", "c p m p h u p w");
        treemap1.put("r2", "c m i p h u p w");
        treemap1.put("r3", "c m i p h u m i p w");
        treemap1.put("r4", "c m i j j 3 p h u p w");

        GrammarRepresentation grammar1 = new GrammarRepresentation(treemap1);

        Solution sol1 = newSolution(grammar1);

        TreeMap<String, String> treemap2 = new TreeMap<>();
        treemap2.put("r0", "r1 r2");
        treemap2.put("r1", "p r y 4 y 6 w m i m l o");
        treemap2.put("r2", "p r y l m  4 y 6 w");
//        treemap2.put("r3", "p r y d j 4 y 6 w");
//        treemap2.put("r4", "p r m p y 4 y 6 w");


        GrammarRepresentation grammar2 = new GrammarRepresentation(treemap2);

        Solution sol2 = newSolution(grammar2);


        Solution[] parents = {sol1, sol2};

        Solution[] offspring = crossover.evolve(parents);
    }

    private Solution newSolution(GrammarRepresentation grammar) {
        Solution solution = new Solution(1, 4);
        solution.setVariable(0, grammar);
        return solution;
    }
}