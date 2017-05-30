package matching;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

/**
 * Created by arianna on 29/05/17.
 */
public class TFIDUtils {

    public static Map<String, Double> computeTFIDF(
            Map<String, Double> freq, Set<SimpleMethodCodeElement> codeElements) {
//        for (CodeElement<?> codeElement : codeElements) {
        for(SimpleMethodCodeElement codeElement : codeElements){
//            if (codeElement instanceof MethodCodeElement) {
                freq = storeFrequencies(codeElement.getCodeElementIds(), freq);
            }
//        }

        freq = adjustFrequencies(freq);
        return freq;
    }

    private static Map<String, Double> adjustFrequencies(Map<String, Double> freq) {
        for (Map.Entry<String, Double> entry : freq.entrySet()) {
            double absolute = entry.getValue();
            double relative = absolute / freq.size();
            freq.put(entry.getKey(), relative);
        }
        return freq;
    }

    private static Map<String, Double> storeFrequencies(
            Set<String> identifiers, Map<String, Double> freq) {
        for (String id : identifiers) {
            String[] camelId = id.split("(?<!^)(?=[A-Z])");
            for (int i = 0; i != camelId.length; i++) {
                if (freq.containsKey(camelId[i].toLowerCase())) {
                    double f = freq.remove(camelId[i].toLowerCase());
                    freq.put(camelId[i].toLowerCase(), ++f);
                } else freq.put(camelId[i].toLowerCase(), 1.0);
            }
        }
        return freq;
    }
}
