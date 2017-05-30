package matching;

import org.toradocu.extractor.Tag;

import java.util.LinkedHashMap;

/**
 * Created by arianna on 29/05/17.
 *
 * A SemanticMatch bounds a Tag with to its valid semantic matches, i.e. method code elements that have a semantic distance below the predefined threshold.
 */

public class SemanticMatch {
    String method;
    Tag tag;
    String parsedComment;
    LinkedHashMap<SimpleMethodCodeElement, Double> candidates;
    boolean correct;


    public SemanticMatch(Tag tag, String method, String parsedComment) {
        this.tag = tag;
        this.method = method;
        this.parsedComment = parsedComment;
    }

    public void computeCorrectness() {
        String firstSignature = candidates.keySet().iterator().next().getCodeElementName();
        String methodName = firstSignature.substring(0, firstSignature.indexOf("("));
        this.correct = tag.getCondition().toString().contains(methodName);
    }
}
