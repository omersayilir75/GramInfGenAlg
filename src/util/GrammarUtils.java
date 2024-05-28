package util;

import net.seninp.gi.logic.GrammarRuleRecord;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

public class GrammarUtils {

    public static void addToMap(GrammarRuleRecord rule, TreeMap<String, String> map) {
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

    public static int findNthOccurrence(String[] strArr, String substr, int n) {
        int occurrence = 0;
        int latestOccurrenceIndex = -1;

        for (int i = 0; i < strArr.length; i++) {
            if (strArr[i].contains(substr)) {
                occurrence++;
                latestOccurrenceIndex = i;
                if (occurrence == n) {
                    return i;
                }
            }
        }
        return latestOccurrenceIndex; // if we cant match an occurrence, use the latest.
    }

    public static void copyAllRules (TreeMap<String,String> source,TreeMap<String, String> destination){
        AtomicInteger newRuleIndex1 = new AtomicInteger(destination.keySet().size());

        source.entrySet().stream().forEach(e -> {
            String oldRuleName = e.getKey();
            String newRuleName = "r" + newRuleIndex1.getAndIncrement();

            for (String key : source.keySet()) {
                String originalValue = source.get(key);
                String modifiedValue = originalValue.replaceAll(oldRuleName, newRuleName);
                source.put(key, modifiedValue); // Update the value in the source map
            }
        });

        AtomicInteger newRuleIndex2 = new AtomicInteger(destination.keySet().size());

        source.entrySet().stream().forEach(e -> {
            String oldRuleName = e.getKey();
            String newRuleName = "r" + newRuleIndex2.getAndIncrement();


            destination.put(newRuleName, source.get(oldRuleName));
        });
        return;
    }


}


