package grammatical_inference;

import org.antlr.runtime.RecognitionException;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.ATNSerializer;
import org.antlr.v4.runtime.tree.*;
import org.antlr.v4.tool.Grammar;
import org.antlr.v4.tool.GrammarInterpreterRuleContext;
import org.antlr.v4.tool.GrammarParserInterpreter;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;
import org.moeaframework.core.configuration.Property;
import org.moeaframework.core.configuration.Validate;
import util.Pair;
import util.Triple;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class Graminf_Mut implements Variation {
    private double probability;

    public Graminf_Mut() {
        this(1.0);
    }

    private TreeMap<String, String> grammarmap;

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
        this.grammarmap = grammar;

        // parse positive samples and collect paths of near-misses

        HashMap<CommonToken, ArrayList<Triple<?, Pair<Integer, Integer>, Integer>>> nearMisses = new HashMap<>();

        String antlrGrammar = GramInf.treeMapToAntlrString(grammar);
        String path = "C:\\Users\\omer_\\Desktop\\algSamples\\subset\\subset";
        try {
            parseSamples(antlrGrammar, path, nearMisses);
        } catch (RecognitionException e) {
            throw new RuntimeException(e);
        }

        if (!nearMisses.isEmpty()) {
            List<TreeMap<String, String>> mutations = mutate(nearMisses, grammarmap);
            Solution[] mutated_solutions = new Solution[mutations.size()];

            if (mutated_solutions.length > 0) {

                for (int i = 0; i < mutations.size(); i++) {
                    Solution result = sol.copy();
                    mutated_solutions[i] = result;
                    GrammarRepresentation representation = (GrammarRepresentation) result.getVariable(0);
                    representation.setGrammar(mutations.get(i));
                    mutated_solutions[i].setVariable(0, representation);
                }


                return mutated_solutions;
            }
        }

        return new Solution[]{solutions[0].copy()};
    }

    private void parseSamples(String grammarText, String pathName,
                              HashMap<CommonToken, ArrayList<Triple<?, Pair<Integer, Integer>, Integer>>> nearMisses)
            throws RecognitionException {
        Grammar g = new Grammar(grammarText);

        try (Stream<Path> paths = Files.walk(Paths.get(pathName))) {
            paths
//                    .parallel()
                    .forEach(p -> parseFile(p, g, nearMisses));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parseFile(Path path, Grammar antlrGrammar, HashMap<CommonToken, ArrayList<Triple<?, Pair<Integer, Integer>, Integer>>> nearMisses) {
        BufferedReader reader = null;
        File program = path.toFile();

        if (program.isFile()) {  //walk also goes through dirs...
            try {
                String programPath = program.getAbsolutePath();
                reader = new BufferedReader(new FileReader(programPath));
                CharStream input = CharStreams.fromReader(reader);
                LexerInterpreter lexEngine = antlrGrammar.createLexerInterpreter(input);
                lexEngine.getErrorListeners();
                lexEngine.removeErrorListeners(); // prevent logging error listeners
                CustomErrorListener lexerListener = new CustomErrorListener(); //use our custom listener
                lexEngine.addErrorListener(lexerListener);

                CommonTokenStream tokens = new CommonTokenStream(lexEngine);
                GrammarParserInterpreter p = new GrammarParserInterpreter(antlrGrammar,
                        new ATNDeserializer().deserialize(ATNSerializer.getSerialized(antlrGrammar.getATN()).toArray()), tokens);
                // prevent large error logs
                p.removeErrorListeners();
                CustomErrorListener parserListener = new CustomErrorListener();
                p.addErrorListener(parserListener);
                ParseTree t = p.parse(antlrGrammar.rules.get("r0").index);

                if (parserListener.getNumberOfSyntaxErrors() == 1 && lexerListener.getNumberOfSyntaxErrors() == 0) {
                    ArrayList<Triple<?, Pair<Integer, Integer>, Integer>> tokenRules = new ArrayList<>(); // todo: also need this in mutate function

                    ParseTreeWalker walker = new ParseTreeWalker();
                    ParseTreeListener listener = new ParseTreeListener() {
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
                                if (child instanceof TerminalNode) { // else branch : token, parentruleindex
                                    Token token = ((TerminalNode) child).getSymbol();
//                                    tokenRuleMap.put(i + "_" + ruleIndex, new Pair<>(token, ruleIndex)); // key cannot be i, ruleIndex_i maybe better?

                                    int parentRuleIndex = -1; // default value, if we're dealing with the root rule
                                    if (parserRuleContext.getParent() != null) {
                                        parentRuleIndex = parserRuleContext.getParent().getRuleIndex();
                                    }

                                    tokenRules.add(new Triple<>(token,
                                            new Pair<>(ruleIndex, parentRuleIndex),
                                            i));
                                } else {
                                    String ruleName = "r" + ((GrammarInterpreterRuleContext) child).getRuleIndex();

                                    int parentRuleIndex = -1; // default value, if we're dealing with the root rule
                                    if (parserRuleContext.getParent() != null) {
                                        parentRuleIndex = parserRuleContext.getParent().getRuleIndex();
                                    }
                                    tokenRules.add(new Triple<>(ruleName,
                                            new Pair<>(ruleIndex, parentRuleIndex),
                                            i));
                                }
                            }
                        }

                        @Override
                        public void exitEveryRule(ParserRuleContext parserRuleContext) {

                        }
                    };


                    CommonToken llt = parserListener.getLastLegalToken();
                    walker.walk(listener, t);

                    nearMisses.put(llt, tokenRules);

//
//                    //todo move out of this function...
//
//                    // key = token or rulename, v1 = ruleIndex, v2 = childindex (lower means appears earlier)
//                    Triple<?, Integer, Integer> ruleIndexLlt = tokenRules
//                            .stream()
//                            .filter(tr -> tr.getKey() == llt)
//                            .findFirst()
//                            .orElse(null);
//
//                    // now: determine what to mutate
//                    // if v1 == 0: mutate llt in r0
//                    // if v1 != 0: mutate appropriate non-terminal of parent
//
//                    if (ruleIndexLlt != null) {
//                        if ((Integer) ruleIndexLlt.getValue1() == 0) {
//                            // look up terminal in r0 and mutate
//
//                            Stream<Triple<?, Integer, Integer>> occurrencesTerminalSymbol = tokenRules
//                                    .stream()
//                                    .filter(tr -> {
//                                        if (tr.getKey() instanceof Token) {
//                                            return ((Token) tr.getKey())
//                                                    .getText()
//                                                    .equals(((Token) ruleIndexLlt.getKey()).getText());
//                                        } else {
//                                            return false;
//                                        }
//                                    });
//                            List<Triple<?, Integer, Integer>> sortedOccurrences = occurrencesTerminalSymbol
//                                    .sorted(Comparator.comparing(Triple::getValue2)).toList();
//
//
//                            // find out which occurrence we're dealing with
//                            int indexOccurrence = sortedOccurrences.indexOf(ruleIndexLlt); // not sure how to use this. this number might be greater than occs in rule...
//
//                            //Get rule
//                            String ruleToMutate = grammarmap.get("r" + ruleIndexLlt.getValue1());
//
//                            int a = 5;
//
//
//                        } else {
//                            // look up non-terminal "r" + ruleIndexLlt.getValue1() in parent rule and mutate
//                        }
//
//                    }


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


    private List<TreeMap<String, String>> mutate(HashMap<CommonToken, ArrayList<Triple<?, Pair<Integer, Integer>, Integer>>> nearMisses, TreeMap<String, String> grammarMap) {
        List<TreeMap<String, String>> results = new ArrayList<>();
        Object[] keys = nearMisses.keySet().toArray();
        Object key = keys[new Random().nextInt(keys.length)]; // select a near miss

        CommonToken llt = (CommonToken) key; //last legal token of selected near miss
        ArrayList<Triple<?, Pair<Integer, Integer>, Integer>> tokenRules = nearMisses.get(llt);

        // key = token or rulename, v1 = ruleIndex, v2 = childIndex (lower value means appears earlier)
        Triple<?, Pair<Integer, Integer>, Integer> ruleIndexLlt = tokenRules
                .stream()
                .filter(tr -> tr.getKey() == llt)
                .findFirst()
                .orElse(null);

        // now: determine what to mutate
        // if v1 == 0: mutate llt terminal in r0
        // if v1 != 0: mutate appropriate non-terminal of parent

        if (ruleIndexLlt != null) {
            if (ruleIndexLlt.getValue1().getKey() == 0) {
                // look up terminal in r0 and mutate

                Stream<Triple<?, Pair<Integer, Integer>, Integer>> occurrencesTerminalSymbol = tokenRules
                        .stream()
                        .filter(tr -> {
                            if (tr.getKey() instanceof Token) {
                                return ((Token) tr.getKey())
                                        .getText()
                                        .equals(((Token) ruleIndexLlt.getKey()).getText());
                            } else {
                                return false;
                            }
                        });
                List<Triple<?, Pair<Integer, Integer>, Integer>> sortedOccurrences = occurrencesTerminalSymbol
                        .sorted(Comparator.comparing(Triple::getValue2)).toList();


                // find out which occurrence we're dealing with
                int indexOccurrence = sortedOccurrences.indexOf(ruleIndexLlt); // not sure how to use this. this number might be greater than occs in rule...

                //Get rule
                String ruleToMutate = grammarMap.get("r" + ruleIndexLlt.getValue1().getKey());

                int a = 5; //dbg


            } else {
                // look up non-terminal "r" + ruleIndexLlt.getValue1() in parent rule and mutate
                // parent rule = ruleIndexLlt.value1.value

                //Get rule
                String parentRuleName = "r" + ruleIndexLlt.getValue1().getValue();
                String ruleToMutate = grammarMap.get(parentRuleName);


                // check number of ruleNames in tokenRules

                List<Triple<?, Pair<Integer, Integer>, Integer>> occurrencesNonTerminalSymbol = tokenRules
                        .stream()
                        .filter(tr -> {
                            if (tr.getKey() instanceof String) {
                                return ((tr.getValue1().getKey())
                                        .equals((ruleIndexLlt.getValue1().getValue()))); //
                            } else {
                                return false;
                            }
                        })
                        .toList();
                int occurrenceCount = occurrencesNonTerminalSymbol.size();

                String[] ruleParts = ruleToMutate.split(" ");
                String ruleName = "r" + ruleIndexLlt.getValue1().getKey();

                int indexSubStringToMutate = findNthOccurrence(ruleParts, ruleName, occurrenceCount);


                //add +
                String[] rulePartsPlus = ruleParts.clone();
                rulePartsPlus[indexSubStringToMutate] = "(" + rulePartsPlus[indexSubStringToMutate] + ")+";
                TreeMap<String, String> grammarMapPlus = (TreeMap<String, String>) grammarMap.clone();
                grammarMapPlus.put(parentRuleName, String.join(" ", rulePartsPlus));
                results.add(grammarMapPlus);


                //add *
                String[] rulePartsStar = ruleParts.clone();
                rulePartsStar[indexSubStringToMutate] = "(" + rulePartsStar[indexSubStringToMutate] + ")*";
                TreeMap<String, String> grammarMapStar = (TreeMap<String, String>) grammarMap.clone();
                grammarMapStar.put(parentRuleName, String.join(" ", rulePartsStar));
                results.add(grammarMapStar);

                //add ?
                String[] rulePartsOpt = ruleParts.clone();
                rulePartsOpt[indexSubStringToMutate] = "(" + rulePartsOpt[indexSubStringToMutate] + ")?";
                TreeMap<String, String> grammarMapOpt = (TreeMap<String, String>) grammarMap.clone();
                grammarMapOpt.put(parentRuleName, String.join(" ", rulePartsOpt));
                results.add(grammarMapOpt);


                int a = 5;

            }

        }

        return results;
    }


    private int findNthOccurrence(String[] strArr, String substr, int n) {
        int occurrence = 0;
        int latestOccurrence = 0;
        for (int i = 0; i < strArr.length; i++) {
            if (strArr[i].equals(substr)) {
                occurrence++;
                if (occurrence == n) {
                    latestOccurrence = i;
                    return i;
                }
            }
        }
        return latestOccurrence;
    }


}
