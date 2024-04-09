package grammatical_inference;

import org.moeaframework.core.Variable;

import java.util.TreeMap;

public class GrammarRepresentation implements Variable {

    public TreeMap<String, String> getGrammar() {
        return grammar;
    }

    public void setGrammar(TreeMap<String, String> grammar) {
        this.grammar = grammar;
    }

    private TreeMap<String,String> grammar;

    public GrammarRepresentation(TreeMap<String, String> grammar) {
        this.grammar = grammar;
    }


    @Override
    public Variable copy() {
        return new GrammarRepresentation(grammar);
    }

    @Override
    public void randomize() {
    }

    @Override
    public String encode() {
        return null;
    }

    @Override
    public void decode(String s) {
    }
}
