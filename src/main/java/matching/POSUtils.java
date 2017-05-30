package matching;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import org.toradocu.extractor.DocumentedMethod;
import org.toradocu.translator.StanfordParser;

import java.util.List;

/**
 * Created by arianna on 29/05/17.
 */
public class POSUtils {

    public static String findSubjectPredicate(String comment, DocumentedMethod method) {
        String result = "";
        if(comment.contains("aboard"))
            System.out.print("DEBUUUUUUUUUUUUUUG");
        for (SemanticGraph sg : StanfordParser.getSemanticGraphs(comment, method)) {
            //Search nouns and their adjectives.

            List<IndexedWord> nouns = sg.getAllNodesByPartOfSpeechPattern("NN(.*)");
            List<IndexedWord> adjs = sg.getAllNodesByPartOfSpeechPattern("JJ(.*)");
            List<IndexedWord> advs = sg.getAllNodesByPartOfSpeechPattern("RB(.*)");
            for (IndexedWord noun : nouns) result += noun.word() + " ";
            for (IndexedWord adj : adjs) result += adj.word() + " ";
            for (IndexedWord adv : advs) result += adv.word() + " ";

            //Search for a verb.
            List<IndexedWord> verbs = sg.getAllNodesByPartOfSpeechPattern("VB(.*)");
            for (IndexedWord v : verbs) result += v.word() + " ";
        }
        return result.replaceAll("\\s+", " ").trim();
    }
}
