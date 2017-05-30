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
    boolean topCandidateIsCorrect;
    boolean candidateFound;
    float threshold;


    public SemanticMatch(Tag tag, String method, String parsedComment, float threshold) {
        this.tag = tag;
        this.method = method;
        this.parsedComment = parsedComment;
        this.threshold = threshold;
    }

    public void computeCorrectness() {
        String firstSignature = candidates.keySet().iterator().next().getCodeElementName();
        String methodName = firstSignature.substring(0, firstSignature.indexOf("("));
        this.topCandidateIsCorrect = tag.getCondition().get().contains(methodName);
    }

    public void computePartialCorrectness(){
        String translation = tag.getCondition().get();
        for(SimpleMethodCodeElement key : candidates.keySet()) {
            String signature = key.getCodeElementName();
            String methodName = signature.substring(0, signature.indexOf("("));
            if(translation.contains(methodName)) {
                this.candidateFound = true;
                return;
            }
        }
        this.candidateFound = false;
    }
}
