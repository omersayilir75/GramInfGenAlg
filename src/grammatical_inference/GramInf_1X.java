package grammatical_inference;


import org.moeaframework.core.PRNG;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variable;
import org.moeaframework.core.operator.OnePointCrossover;

import java.util.*;

public class GramInf_1X extends OnePointCrossover {

    // Implementation ideas
    // match rule with the same start symbol
    // if multiple match the closest
    // pick point (where they differ could be interesting)
    // case we have non-terminals:
    // option 1: implement it in such a way that these NTs are copied over replacing nothing
    // option 2: replace most similar NT


//    @Override
//    public int getArity() {
//        return 3;
//    }


    @Override
    public Solution[] evolve(Solution[] parents) {
        Solution result1 = parents[0].copy();
        Solution result2 = parents[1].copy();
        // get the grammars
        GrammarRepresentation repr1 = (GrammarRepresentation) result1.getVariable(0);
        GrammarRepresentation repr2 = (GrammarRepresentation) result2.getVariable(0);
        TreeMap<String, String> grammar1 = repr1.getGrammar();
        TreeMap<String, String> grammar2 = repr2.getGrammar();

        //experiment reducing randomness
        Comparator<String> lengthComparator = new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return Integer.compare(s1.length(), s2.length());
            }
        };

        // pick the rules to crossover
        Random rand = new Random();
        List<String> grammar1Keys = new ArrayList<>(grammar1.keySet());
//        String randomRuleKeyGrammar1 = grammar1Keys.get(rand.nextInt(grammar1Keys.size()));
//        String ruleParent1 = grammar1.get(randomRuleKeyGrammar1);

        String ruleParent1 = Collections.max(grammar1.values(), lengthComparator);
        String randomRuleKeyGrammar1 = null;
        for (Map.Entry<String, String> entry : grammar1.entrySet()) {
            if (entry.getValue().equals(ruleParent1)) {
                randomRuleKeyGrammar1 = entry.getKey();
                break;
            }
        }



        List<String> grammar2Keys = new ArrayList<>(grammar2.keySet());
//        String randomRuleKeyGrammar2 = grammar2Keys.get(rand.nextInt(grammar2Keys.size()));
//        String ruleParent2 = grammar2.get(randomRuleKeyGrammar2);

        String ruleParent2 = Collections.max(grammar2.values(), lengthComparator);
        String randomRuleKeyGrammar2 = null;
        for (Map.Entry<String, String> entry : grammar2.entrySet()) {
            if (entry.getValue().equals(ruleParent2)) {
                randomRuleKeyGrammar2 = entry.getKey();
                break;
            }
        }


        if (ruleParent1.split(" ").length < ruleParent2.split(" ").length) {
            crossoverRules(ruleParent1, randomRuleKeyGrammar1, grammar1, ruleParent2, randomRuleKeyGrammar2, grammar2);
        } else {
            crossoverRules(ruleParent2, randomRuleKeyGrammar2, grammar2, ruleParent1, randomRuleKeyGrammar1, grammar1);
        }


        return new Solution[]{result1, result2};
    }

    private void crossoverRules(String shorterRule,
                                String shorterRuleKey,
                                TreeMap<String, String> shorterRuleGrammar,
                                String longerRule,
                                String longerRuleKey,
                                TreeMap<String, String> longerRuleGrammar) {

        ArrayList<String> shorterRuleParts = new ArrayList<>(Arrays.asList(shorterRule.split(" ")));
        ArrayList<String> longerRuleParts = new ArrayList<>(Arrays.asList(longerRule.split(" ")));

        int shorterRuleLength = shorterRuleParts.toArray().length;
        int longerRuleLength = longerRuleParts.toArray().length;

        // define a cut point
        Random rand = new Random();
        int cutPoint = rand.nextInt(shorterRuleParts.toArray().length);


        // make copies to modify
        ArrayList<String> modifiedShorterRuleParts = new ArrayList<>(shorterRuleParts);
        ArrayList<String> modifiedLongerRuleParts = new ArrayList<>(longerRuleParts);


        // cross the rules over
        copyCrossedOverRule(shorterRuleGrammar, longerRuleGrammar, modifiedShorterRuleParts, longerRuleParts, cutPoint);

        modifiedLongerRuleParts.subList(shorterRuleLength, longerRuleLength).clear(); // truncate longer rule

        copyCrossedOverRule(longerRuleGrammar, shorterRuleGrammar, modifiedLongerRuleParts, shorterRuleParts, cutPoint);

        shorterRuleGrammar.put(shorterRuleKey, String.join(" ", modifiedShorterRuleParts));
        longerRuleGrammar.put(longerRuleKey, String.join(" ", modifiedLongerRuleParts));
    }

    private void copyCrossedOverRule(TreeMap<String, String> targetRuleGrammar,
                                     TreeMap<String, String> sourceRuleGrammar,
                                     ArrayList<String> targetRuleParts,
                                     ArrayList<String> sourceRuleParts, int cutPoint) {
        for (int i = cutPoint; i < sourceRuleParts.toArray().length; i++) {
            String part = sourceRuleParts.get(i);
            if (part.matches("r\\d+")) {
                String rule = sourceRuleGrammar.get(part);
                part = copyWithSubrules(rule, targetRuleGrammar, sourceRuleGrammar);
            } else if (isRuleWithOperators(part)) {
                // keep track of which operator was present.
//                String operator = null;
//                char[] operators = {'*', '+', '?'};
//                for (char op : operators) {
//                    if (part.contains(Character.toString(op))) {
//                        operator = Character.toString(op);
//                        break;
//                    }
//                }

                String partWithoutOperators = part.
                        replace("(", "").
                        replace(")", "").
                        replace("+", "").
                        replace("*", "").
                        replace("?","");
                // strip parenthesis and operator first
                String rule = sourceRuleGrammar.get(partWithoutOperators);
                // after invoking copyWithSubrules, put the parenthesis and operator back
                String newPartName = copyWithSubrules(rule, targetRuleGrammar, sourceRuleGrammar);
                // after invoking copyWithSubrules, put the parentheses and operator back
                part = part.replace(partWithoutOperators, newPartName);


            }
            try {
                targetRuleParts.set(i, part);
            } catch (IndexOutOfBoundsException e) {
                targetRuleParts.add(part);
            }
        }
    }

    private String copyWithSubrules(String rule, TreeMap<String, String> targetGrammar, TreeMap<String, String> sourceGrammar) {
        ArrayList<String> ruleParts = new ArrayList<>(Arrays.asList(rule.split(" ")));

        for (int i = 0; i < ruleParts.size(); i++) {
            String part = ruleParts.get(i);
            if (part.matches("r\\d+")) {
                String subRule = sourceGrammar.get(part); // get the subrule
                part = copyWithSubrules(subRule, targetGrammar, sourceGrammar); // part becomes new non-terminal name
                ruleParts.set(i, part);
            } else if (isRuleWithOperators(part)) {
                String partWithoutOperators = part.
                        replace("(", "").
                        replace(")", "").
                        replace("+", "").
                        replace("*", "").
                        replace("?","");


                // get the subrule
                String subRule = sourceGrammar.get(partWithoutOperators);

                String newPartName = copyWithSubrules(subRule, targetGrammar, sourceGrammar);
                // after invoking copyWithSubrules, put the parentheses and operator back
                part = part.replace(partWithoutOperators, newPartName);
                ruleParts.set(i, part);
            }
        }

        String modifiedRuleName = "r" + targetGrammar.keySet().size(); // rules are 0 indexed so this is the number of the next rule
        String modifiedRule = String.join(" ", ruleParts); // rule with updated non-terminal names
        targetGrammar.put(modifiedRuleName, modifiedRule); // rename and copy rule provided
        return modifiedRuleName;
    }

    public boolean isRuleWithOperators(String str) {
        if (str.matches("\\(r\\d+\\)\\*|\\(r\\d+\\)\\+|\\(r\\d+\\)\\?")) {
            return true;
        } else {
            int openIndex = str.indexOf('(');
            int closeIndex = str.lastIndexOf(')');
            if (openIndex == -1 || closeIndex == -1 || closeIndex < openIndex) {
                return false;
            } else {
                return isRuleWithOperators(str.substring(openIndex + 1, closeIndex));
            }
        }
    }



}
