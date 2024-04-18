package grammatical_inference;

import org.antlr.runtime.RecognitionException;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.ATNSerializer;
import org.antlr.v4.runtime.tree.*;
import org.antlr.v4.tool.Grammar;
import org.antlr.v4.tool.GrammarParserInterpreter;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;
import org.moeaframework.core.configuration.Property;
import org.moeaframework.core.configuration.Validate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.stream.Stream;

public class Graminf_Mut implements Variation {
    private double probability;

    public Graminf_Mut() {
        this(1.0);
    }

    public double getProbability() {
        return this.probability;
    }

    public Graminf_Mut(double probability) {
        this.setProbability(probability);
    }

    @Property("rate")
    public void setProbability(double probability) {
        Validate.probability("probability", probability);
        this.probability = probability;
    }


    @Override
    public String getName() {
        return "Graminf_Mut";
    }

    @Override
    public int getArity() {
        return 1;
    }

    @Override
    public Solution[] evolve(Solution[] solutions) {
        // todo impl here
        // parse pos samples DONE
        // collect samples that are near misses (1 syntax error) DONE
        // get position of failure (need to mod the custom error here probably) DONE
        // mutate around failing point in grammar
        // try and return a widened and narrowed version of the grammar if they succeed.

        Solution sol = solutions[0].copy();
        // get the grammar
        GrammarRepresentation repr = (GrammarRepresentation) sol.getVariable(0);
        TreeMap<String, String> grammar = repr.getGrammar();

        // parse positive samples and collect paths of near-misses

        HashMap<String, HashMap<CommonToken, HashMap<Integer, Integer>>> nearMisses = new HashMap<>();

        String antlrGrammar = GramInf.treeMapToAntlrString(grammar);
        String path = "C:\\Users\\omer_\\Desktop\\algSamples\\subset\\subset";
        try {
            parseSamples(antlrGrammar, path, nearMisses);
        } catch (RecognitionException e) {
            throw new RuntimeException(e);
        }


        return new Solution[0];


    }

    private void parseSamples(String grammarText, String pathName,
                              HashMap<String, HashMap<CommonToken, HashMap<Integer, Integer>>> nearMisses)
            throws RecognitionException {
        Grammar g = new Grammar(grammarText);

        try (Stream<Path> paths = Files.walk(Paths.get(pathName))) {
            paths
                    .parallel()
                    .forEach(p -> parseFile(p, g, nearMisses));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parseFile(Path path, Grammar g, HashMap<String, HashMap<CommonToken, HashMap<Integer, Integer>>> nearMisses) {
        BufferedReader reader = null;
        File program = path.toFile();

        if (program.isFile()) {  //walk also goes through dirs...
            try {
                String programPath = program.getAbsolutePath();
                reader = new BufferedReader(new FileReader(programPath));
                CharStream input = CharStreams.fromReader(reader);
                LexerInterpreter lexEngine = g.createLexerInterpreter(input);
                lexEngine.getErrorListeners();
                lexEngine.removeErrorListeners(); // prevent logging error listeners
                CustomErrorListener lexerListener = new CustomErrorListener(); //use our custom listener
                lexEngine.addErrorListener(lexerListener);

                CommonTokenStream tokens = new CommonTokenStream(lexEngine);
                GrammarParserInterpreter p = new GrammarParserInterpreter(g,
                        new ATNDeserializer().deserialize(ATNSerializer.getSerialized(g.getATN()).toArray()), tokens);
                // prevent large error logs
                p.removeErrorListeners();
                CustomErrorListener parserListener = new CustomErrorListener();
                p.addErrorListener(parserListener);
                ParseTree t = p.parse(g.rules.get("r0").index);

                if (parserListener.getNumberOfSyntaxErrors() == 1 && lexerListener.getNumberOfSyntaxErrors() == 0) {
                    HashMap<Token, Integer> tokenRuleMap = new HashMap<>(); // (hopefully) contains <token index, corresponding rule index>...

                    ParseTreeWalker walker = new ParseTreeWalker();
                    ParseTreeListener listener = new ParseTreeListener() {
                        private ParserRuleContext _ctx = null;

                        @Override
                        public void visitTerminal(TerminalNode terminalNode) {

                        }

                        @Override
                        public void visitErrorNode(ErrorNode errorNode) {

                        }

                        @Override
                        public void enterEveryRule(ParserRuleContext parserRuleContext) {
                            int ruleIndex = parserRuleContext.getRuleIndex();
                            for (int i = 0; i < parserRuleContext.getChildCount(); i++) {
                                ParseTree child = parserRuleContext.getChild(i);
                                if (child instanceof TerminalNode) {
                                    Token token = ((TerminalNode) child).getSymbol();
                                    tokenRuleMap.put(token, ruleIndex);
                                }
                            }
                        }
//                        Todo: possible improvement lower i means earlier in the rule. this way picking which part of the rule to mutate is a bit easier
//                        @Override
//                        public void enterEveryRule(ParserRuleContext parserRuleContext) {
//                            int ruleIndex = parserRuleContext.getRuleIndex();
//                            for (int i = 0; i < parserRuleContext.getChildCount(); i++) {
//                                ParseTree child = parserRuleContext.getChild(i);
//                                if (child instanceof TerminalNode) {
//                                    Token token = ((TerminalNode) child).getSymbol();
//                                    test.put(token, new Pair<>(ruleIndex, i));
//                                }
//                            }
//                        }


                        @Override
                        public void exitEveryRule(ParserRuleContext parserRuleContext) {

                        }
                    };


                    CommonToken llt = parserListener.getLastLegalToken();
                    walker.walk(listener, t);

                    int ruleIndexLlt = tokenRuleMap.get(llt);


                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

            }
        }
    }
}
