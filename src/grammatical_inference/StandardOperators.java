package grammatical_inference;

import org.moeaframework.core.operator.DefaultOperators;

public class StandardOperators extends DefaultOperators {

    public StandardOperators() {
        super();

        setMutationHint(GrammarRepresentation.class, "Graminf_1X_Crossover");
        setMutationHint(GrammarRepresentation.class, "Graminf_Mut");


        register ("Graminf_1X_Crossover", (properties, problem) -> new GramInf_1X());
        register ("Graminf_Mut", (properties, problem) -> new Graminf_Mut());
    }
}
