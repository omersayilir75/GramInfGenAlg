package grammatical_inference;

import net.seninp.gi.logic.GrammarRules;
import net.seninp.gi.repair.NewRepair;
import net.seninp.gi.repair.RePairGrammar;
import net.seninp.gi.sequitur.SAXRule;
import net.seninp.gi.sequitur.SequiturFactory;
import org.antlr.runtime.RecognitionException;
import org.antlr.v4.analysis.LeftRecursiveRuleTransformer;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.ATNSerializer;
import org.antlr.v4.runtime.tree.*;
import org.antlr.v4.tool.Grammar;
import org.antlr.v4.tool.GrammarInterpreterRuleContext;
import org.antlr.v4.tool.GrammarParserInterpreter;
import org.antlr.v4.tool.Rule;
import org.antlr.v4.tool.ast.GrammarRootAST;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;
import util.Pair;
import util.ReadProperties;
import util.Triple;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static util.GrammarUtils.*;

public class GramInf_Mut_Repair implements Variation {
    @Override
    public String getName() {
        return "GramInf_Mut_Repair";
    }

    @Override
    public int getArity() {
        return 1;
    }


    @Override
    public Solution[] evolve(Solution[] solutions) {
        Solution sol = solutions[0].copy();

        // get the grammar
        GrammarRepresentation repr = (GrammarRepresentation) sol.getVariable(0);
        TreeMap<String, String> grammar = repr.getGrammar();

        // parse positive samples and collect paths of near-misses

        // llt, tokenrules, unrecognised substring grammar
        // with tokenrules being a triple of either terminal or non-terminal string name, <current rule, parent rule> and index of current terminal/nonterminal in the file being parsed.
        HashMap<CommonToken, Pair<ArrayList<Triple<?, Pair<Integer, Integer>, Integer>>, TreeMap<String, String>>> nearMisses = new HashMap<>();

        String antlrGrammar = GramInf.treeMapToAntlrString(grammar);
        String path = ReadProperties.getInstance().getValue("POS_SAMPLES_PATH");
        try {
            parseSamples(antlrGrammar, path, nearMisses);
        } catch (RecognitionException e) {
            throw new RuntimeException(e);
        }

        if (!nearMisses.isEmpty()) {
            List<TreeMap<String, String>> mutations = mutate(nearMisses, grammar);
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
                              HashMap<CommonToken, Pair<ArrayList<Triple<?, Pair<Integer, Integer>, Integer>>, TreeMap<String, String>>> nearMisses)
            throws RecognitionException {
        Grammar g = new Grammar(grammarText);


        GrammarRootAST rootAST = g.ast;
        String[] ruleNames = g.getRuleNames();
        Collection<Rule> rules = Arrays.stream(ruleNames).map(rn -> g.getRule(rn))
                .collect(Collectors.toList());

        // Create the transformer
        LeftRecursiveRuleTransformer transformer = new LeftRecursiveRuleTransformer(rootAST, rules, g);

        // Translate the left-recursive rules
        transformer.translateLeftRecursiveRules();


        try (Stream<Path> paths = Files.walk(Paths.get(pathName))) {
            paths
//                    .parallel()
                    .forEach(p -> {
                        try {
                            parseFile(p, g, nearMisses);
                        } catch (Exception ignored) {

                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parseFile(Path path, Grammar antlrGrammar,
                           HashMap<CommonToken, Pair<ArrayList<Triple<?, Pair<Integer, Integer>, Integer>>, TreeMap<String, String>>> nearMisses) throws Exception {
        BufferedReader reader = null;
        File program = path.toFile();

        if (program.isFile()) {  //walk also goes through dirs...
            try {
                String programPath = program.getAbsolutePath();
                String input = new String(Files.readAllBytes(Paths.get(programPath)), StandardCharsets.UTF_8);
                CharStream charStream = CharStreams.fromString(input);
                LexerInterpreter lexEngine = antlrGrammar.createLexerInterpreter(charStream);
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

                if (lexerListener.getNumberOfSyntaxErrors() > 0) { // we have some errors
                    String subStr = input.substring(lexerListener.getFirstError(), lexerListener.getLastError() + 1);

                    SAXRule r = SequiturFactory.runSequitur(subStr); // left recursive rules without alternative become a problem
                    GrammarRules rules = r.toGrammarRulesData();

                    TreeMap<String, String> subStrGrammar = new TreeMap<>();
                    rules.forEach(rule -> addToMap(rule, subStrGrammar));


                    ArrayList<Triple<?, Pair<Integer, Integer>, Integer>> tokenRules = new ArrayList<>();

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
                                if (child instanceof TerminalNode) {
                                    Token token = ((TerminalNode) child).getSymbol();

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


                    nearMisses.put(llt, new Pair(tokenRules, subStrGrammar));

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

    private List<TreeMap<String, String>> mutate(HashMap<CommonToken, Pair<ArrayList<Triple<?, Pair<Integer, Integer>, Integer>>, TreeMap<String, String>>> nearMisses, TreeMap<String, String> grammarMap) {
        List<TreeMap<String, String>> results = new ArrayList<>();
        Object[] keys = nearMisses.keySet().toArray();
        Object key = keys[new Random().nextInt(keys.length)]; // select a near miss

        CommonToken llt = (CommonToken) key; //last legal token of selected near miss
        ArrayList<Triple<?, Pair<Integer, Integer>, Integer>> tokenRules = nearMisses.get(llt).getKey();

        // key = token or rulename, v1 = ruleIndex, v2 = childIndex (lower value means appears earlier)
        Triple<?, Pair<Integer, Integer>, Integer> ruleIndexLlt = tokenRules
                .stream()
                .filter(tr -> tr.getKey() == llt)
                .findFirst()
                .orElse(null);

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
                int indexOccurrence = sortedOccurrences.indexOf(ruleIndexLlt);

                //Get rule
                String ruleToMutate = grammarMap.get("r" + ruleIndexLlt.getValue1().getKey());

                String[] ruleParts = ruleToMutate.split(" ");

                int indexNthOccurrenceInRule = findNthOccurrence(ruleParts,
                        ((Token) ruleIndexLlt.getKey()).getText(),
                        indexOccurrence);

                if (indexNthOccurrenceInRule != -1) {
                    String ruleName = "r" + ruleIndexLlt.getValue1().getKey();
                    // get substring grammar and change rule names:
                    nearMisses.get(llt);
                    // inject start rule subStringGrammar as optional...
                    List<String> rulePartsList = new ArrayList<>(Arrays.asList(ruleParts));
                    rulePartsList.add(indexNthOccurrenceInRule + 1, "(r" + grammarMap.keySet().size() + ")?");
                    grammarMap.put("r" + ruleIndexLlt.getValue1().getKey(), String.join(" ", rulePartsList));
                    // add rest of rules
                    copyAllRules(nearMisses.get(llt).getValue(), grammarMap);
                    results.add(grammarMap);

                }
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


                if (indexSubStringToMutate != -1) {

                    // get substring grammar and change rule names:
                    nearMisses.get(llt);
                    // inject start rule subStringGrammar as optional...
                    List<String> rulePartsList = new ArrayList<>(Arrays.asList(ruleParts));
                    rulePartsList.add(indexSubStringToMutate + 1, "(r" + grammarMap.keySet().size() + ")?");
                    grammarMap.put(parentRuleName, String.join(" ", rulePartsList));
                    // add rest of rules
                    copyAllRules(nearMisses.get(llt).getValue(), grammarMap);
                    results.add(grammarMap);

                }

            }

        }

        return results;
    }





}
