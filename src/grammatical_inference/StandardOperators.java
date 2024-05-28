package grammatical_inference;

import org.moeaframework.core.operator.DefaultOperators;

public class StandardOperators extends DefaultOperators {

    public StandardOperators() {
        super();

        setMutationHint(GrammarRepresentation.class, "GramInf_1X_Crossover");
        setMutationHint(GrammarRepresentation.class, "GramInf_Mut");
        setMutationHint(GrammarRepresentation.class, "GramInf_Mut_Repair");


        register ("GramInf_1X_Crossover", (properties, problem) -> new GramInf_1X());
        register ("GramInf_Mut", (properties, problem) -> new GramInf_Mut());
        register ("GramInf_Mut_Repair", (properties, problem) -> new GramInf_Mut_Repair());
    }
}
