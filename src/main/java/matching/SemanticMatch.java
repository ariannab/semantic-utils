package matching;

import org.toradocu.extractor.DocumentedMethod;
import org.toradocu.extractor.Tag;
import org.toradocu.translator.MethodCodeElement;

import java.util.LinkedHashMap;

/**
 * Created by arianna on 29/05/17.
 */
public class SemanticMatch {
    Tag tag;
    LinkedHashMap<Method, Double> candidates;

    public SemanticMatch(Tag tag) {
        this.tag = tag;
    }


}
