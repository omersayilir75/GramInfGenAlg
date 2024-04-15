package grammatical_inference;

import org.moeaframework.core.operator.DefaultOperators;

public class StandardOperators extends DefaultOperators {

    public StandardOperators() {
        super();

        setMutationHint(GrammarRepresentation.class, "Graminf_1X_Crossover");


        register ("Graminf_1X_Crossover", (properties, problem) -> new GramInf_1X());
    }
}
