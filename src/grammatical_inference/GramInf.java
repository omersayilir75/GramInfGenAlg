package grammatical_inference;

import net.seninp.gi.logic.GrammarRuleRecord;
import net.seninp.gi.logic.GrammarRules;
import net.seninp.gi.sequitur.SAXRule;
import net.seninp.gi.sequitur.SequiturFactory;
import org.antlr.runtime.RecognitionException;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.LexerInterpreter;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.ATNSerializer;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.tool.Grammar;
import org.antlr.v4.tool.GrammarParserInterpreter;
import org.moeaframework.core.Solution;
import org.moeaframework.problem.AbstractProblem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class GramInf extends AbstractProblem {
    private int sampleToInit = 0;

    public GramInf() {
        super(1, 4);
    }

    private final String skipWhitespace = "Whitespace\n" +
            "\t\t:   [ \\t]+\n" +
            "\t\t-> skip\n" +
            "\t\t;";

    @Override
    public Solution newSolution() {
        Solution solution = new Solution(1, 4);
        try {
            solution.setVariable(0, new GrammarRepresentation(
                    grammarForFileInFolder("C:\\Users\\omer_\\Desktop\\gensamples\\positive\\desk\\terminalOnly\\subset")
            ));
            sampleToInit++;
        } catch (Exception ignored) {

        }
        return solution;
    }

    @Override
    public void evaluate(Solution solution) {

        GrammarRepresentation grammarRep = (GrammarRepresentation) solution.getVariable(0);
        TreeMap<String, String> grammarMap = grammarRep.getGrammar();
        String grammar = treeMapToAntlrString(grammarMap);

        //parse positive samples
        AtomicInteger totalTruePositiveSamples = new AtomicInteger();
        AtomicInteger passedTruePositiveSamples = new AtomicInteger();

        try {
            parseSamples(grammar, "C:\\Users\\omer_\\Desktop\\gensamples\\positive\\desk\\terminalOnly\\subset", passedTruePositiveSamples, totalTruePositiveSamples);
        } catch (RecognitionException e) {
            throw new RuntimeException(e);
        }

        // Maximise ratio passed true positives
        double ratioTruePositivesPassed = ((double) passedTruePositiveSamples.get() / (double) totalTruePositiveSamples.get());
        solution.setObjective(0, -ratioTruePositivesPassed);

        //parse negative samples
        AtomicInteger totalTrueNegativeSamples = new AtomicInteger();
        AtomicInteger passedTrueNegativeSamples = new AtomicInteger();

        try {
            parseSamples(grammar, "C:\\Users\\omer_\\Desktop\\gensamples\\negative\\desk\\wordmutation\\terminalOnly\\output\\subfolder1", passedTrueNegativeSamples, totalTrueNegativeSamples);
        } catch (RecognitionException e) {
            throw new RuntimeException(e);
        }

        // Minimise ratio passed true positives
        double ratioTrueNegativesPassed = ((double) passedTrueNegativeSamples.get() / (double) totalTrueNegativeSamples.get());
        solution.setObjective(0, ratioTrueNegativesPassed);

        // Minimise average length productions
        AtomicInteger totalLength = new AtomicInteger();
        grammarMap.forEach((r, c) -> {
            String[] words = r.split(" ");
            totalLength.addAndGet((words.length));
        });
        double averageLength = ( (double) totalLength.get() / grammarMap.size());
        solution.setObjective(2, averageLength);

        // Minimise average number of productions
        solution.setObjective(3, grammarMap.size());
    }

    private TreeMap<String, String> grammarForFileInFolder(String path) throws Exception {
        File dir = new File(path);
        File[] files = dir.listFiles();

        assert files != null;
        File pickedFile = files[sampleToInit];
        String fileContents = new String(Files.readAllBytes(Paths.get(pickedFile.getAbsolutePath())));

        SAXRule r = SequiturFactory.runSequitur(fileContents);
        GrammarRules rules = r.toGrammarRulesData();

        TreeMap<String, String> grammar = new TreeMap<>();
        rules.forEach(rule -> addToMap(rule, grammar));
        return grammar;
    }

    private void addToMap(GrammarRuleRecord rule, TreeMap<String, String> map) {
        String rulestr = rule.getRuleString();
        String[] parts = rulestr.split("\\s+");
        StringBuilder quotedRule = new StringBuilder();
        for (String part : parts) {
            if (!part.matches("r\\d+")) {
                quotedRule.append("'").append(part).append("' ");
            } else {
                quotedRule.append(part).append(' ');
            }
        }
        map.put(rule.getRuleName(), quotedRule.toString().trim());
    }


    private String treeMapToAntlrString(TreeMap<String, String> map) {
        StringBuilder rulesAsString = new StringBuilder("grammar desk;\n");

        map.forEach((name, content) -> rulesAsString.append(name).append(": ").append(content).append(";\n"));
        rulesAsString.append(skipWhitespace);
        return rulesAsString.toString();
    }

    private void parseSamples(String grammarText, String pathName, AtomicInteger passedSamples, AtomicInteger totalSamples) throws RecognitionException {
        Grammar g = new Grammar(grammarText);

        try (Stream<Path> paths = Files.walk(Paths.get(pathName))) {
            paths
                    .parallel()
                    .forEach(p -> parseFile(p, g, passedSamples, totalSamples));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parseFile(Path path, Grammar g, AtomicInteger passedSamples, AtomicInteger totalSamples) {
        BufferedReader reader = null;
        File program = path.toFile();

        if (program.isFile()) {  //walk also goes through dirs...
            try {
                String programPath = program.getAbsolutePath();
                reader = new BufferedReader(new FileReader(programPath));
                CharStream input = CharStreams.fromReader(reader);
                LexerInterpreter lexEngine = g.createLexerInterpreter(input);
                lexEngine.removeErrorListeners(); // prevent logging error listeners
                CommonTokenStream tokens = new CommonTokenStream(lexEngine);
                GrammarParserInterpreter parser = new GrammarParserInterpreter(g,
                        new ATNDeserializer().deserialize(ATNSerializer.getSerialized(g.getATN()).toArray()), tokens);
                // prevent large error logs
                parser.removeErrorListeners();
                CustomErrorListener listener = new CustomErrorListener();
                parser.addErrorListener(listener);
                ParseTree t = parser.parse(g.rules.get("r0").index);

                if (parser.getNumberOfSyntaxErrors() == 0) {
                    passedSamples.incrementAndGet();
                }
                totalSamples.incrementAndGet();

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
