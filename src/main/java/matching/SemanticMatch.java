package matching;

import com.google.gson.annotations.Expose;
import org.toradocu.extractor.Tag;
import util.SimpleMethodCodeElement;

import java.util.*;

/**
 * Created by arianna on 29/05/17.
 *
 * A SemanticMatch bounds a Tag with to its valid semantic matches, i.e. method code elements
 * that have a semantic distance below the predefined threshold.
 * We store further information in order to have a clearer and more complete output.
 *
 */

public class SemanticMatch implements Comparable<SemanticMatch>{

    /** The method the Tag belongs to. */
    String method;

    /** The tag for which we want to produce a condition translation. */
    Tag tag;

    /** The distance threshold used for this match. */
    float threshold;

    /** The result of comment parsing: stopwords removal etc. */
    String parsedComment;


    /**
     * Method code element that have a semantic distance from the comment which is below the
     * threshold,i.e. candidates for the correct translation.
     */
    LinkedHashMap<SimpleMethodCodeElement, Double> candidates;


    /** Fields that define the correcteness and partial correctness of this match. */
    @Expose
    public boolean topCandidateIsCorrect;
    @Expose
    public boolean candidateFound;


    public SemanticMatch(Tag tag, String method, String parsedComment, float threshold) {
        this.tag = tag;
        this.method = method;
        this.parsedComment = parsedComment;
        this.threshold = threshold;
    }

    /**
     * Is the top candidate the actual right one?
     */
    public void computeCorrectness() {
        String firstSignature = candidates.keySet().iterator().next().getCodeElementName();
        String methodName = firstSignature.substring(0, firstSignature.indexOf("("));
        this.topCandidateIsCorrect = tag.getCondition().get().contains(methodName);
    }

    /**
     * Is the actual right candidate among the list of possible candidates?
     */
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


    public String getMethod() {
        return method;
    }

    public void setCandidates(LinkedHashMap<SimpleMethodCodeElement, Double> orderedDistances) {
        int i=0;
        this.candidates = new LinkedHashMap<SimpleMethodCodeElement, Double>();
        for (Map.Entry<SimpleMethodCodeElement, Double> entry : orderedDistances.entrySet()) {
            this.candidates.put(entry.getKey(), entry.getValue());
            i++;
            if(i==5)
                return;
        }
    }

    public LinkedHashMap<SimpleMethodCodeElement, Double> getCandidates() {
        return candidates;
    }

    @Override
    public int compareTo(SemanticMatch semanticMatch) {
        return this.method.compareTo(semanticMatch.method);
    }
}
