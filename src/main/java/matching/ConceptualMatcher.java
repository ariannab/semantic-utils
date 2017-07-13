package matching;

import de.jungblut.distance.CosineDistance;
import de.jungblut.glove.GloveRandomAccessReader;
import de.jungblut.math.DoubleVector;
import edu.stanford.nlp.ling.CoreLabel;
import org.toradocu.extractor.DocumentedMethod;
import org.toradocu.extractor.Tag;
import org.toradocu.translator.StanfordParser;
import util.AlignmentScore;
import util.OutputUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by arianna on 10/07/17.
 *
 * Component that implements the conceptual similarity, derived from the alignments matrix.
 */
public class ConceptualMatcher extends SemanticMatcher {


    ConceptualMatcher(String className, boolean stopwordsRemoval, boolean posSelect, boolean tfid, float distanceThreshold) {
        super(className, stopwordsRemoval, posSelect, tfid, distanceThreshold);
    }

    private void conceptualSimMatch(GloveRandomAccessReader db, Tag tag, DocumentedMethod method, Set<SimpleMethodCodeElement> codeElements) throws IOException {
        Set<String> commentWordSet = super.parseComment(tag, method);
        String parsedComment = String.join(" ", commentWordSet).replaceAll("\\s+", " ").trim();
        Map<String, Double> freq = new HashMap<String, Double>();

//      Map<MethodCodeElement, Double> distances = new HashMap<MethodCodeElement, Double>();
        Map<SimpleMethodCodeElement, Double> distances = new HashMap<SimpleMethodCodeElement, Double>();

        //     Set<CodeElement<?>> codeElements = Matcher.codeElementsMatch(method, subject, predicate);
//      Set<CodeElement<?>> codeElements = JavaElementsCollector.collect(method);

        // For each code element, I want to take the vectors of its identifiers (like words componing the method name)
        // and compute the semantic similarity with the predicate (or the whole comment, we'll see)

        if (codeElements != null && !codeElements.isEmpty()) {
            if (tfid) freq = TFIDUtils.computeTFIDF(freq, codeElements);
//            for (CodeElement<?> codeElement : codeElements) {
            for(SimpleMethodCodeElement codeElement : codeElements){
//                if (codeElement instanceof MethodCodeElement) {
                String codeElementName = codeElement.getCodeElementIds().iterator().next();
                String[] camelId = codeElementName.split("(?<!^)(?=[A-Z])");
                String joinedId = String.join(" ", camelId).replaceAll("\\s+", " ").toLowerCase().trim();

                int index = 0;
                for (CoreLabel lemma : StanfordParser.lemmatize(joinedId)) {
                    if (lemma != null) camelId[index] = lemma.lemma();
                    index++;
                }
                Set<String> codeElementWordSet = removeStopWords(camelId);

                distances.put(codeElement,computeAlignmentMatrix(db, commentWordSet, codeElementWordSet));
            }
            retainMatches(parsedComment, method.getName(), tag, distances);
        }
    }


    private double computeAlignmentMatrix(GloveRandomAccessReader db,
                                                 Set<String> comment,
                                                 Set<String> codeElement) throws IOException {
        double currentBest = 0;
        String currentY = "";
        List<String> X = new ArrayList<>();
        X.addAll(comment);
        Set<String>  Y = new HashSet<>();
        Y.addAll(codeElement);
        List<String> alreadyTaken = new ArrayList<>();
        Map<String, AlignmentScore> alignments = new HashMap<>();
        for(int i=0; i < X.size(); i++){
            String x = X.get(i);
            for(String y: Y){
                double sim = this.computeSim(db, x, y);
                if(sim > currentBest && !alreadyTaken.contains(y)){
                    currentBest = sim;
                    currentY = y;
                }else if(sim > currentBest && alreadyTaken.contains(y)){
                    double oldValue = alignments.get(y).score;
                    // y was matched to another x with score oldValue
                    if(sim > oldValue){
                        //but this x is better!
                        String oldX = alignments.get(y).x;
                        X.add(oldX);
                        currentBest = sim;
                        currentY = y;
                    }
                }
            }
            if(currentBest!=0 && !currentY.equals("")) {
                alignments.put(currentY, new AlignmentScore(x, currentBest));
                alreadyTaken.add(currentY);
                currentBest = 0;
                currentY = "";
            }
            X.remove(x);
            i--;
        }
        double sum = 0;
        for(AlignmentScore alignmentScore : alignments.values()){
            sum+=alignmentScore.score;
        }
        double similarity = 2*sum/(comment.size()+codeElement.size());

        return similarity;
    }

    void runConceptualSim(GloveRandomAccessReader db, File goalFile, Set<SimpleMethodCodeElement> codeElements) throws IOException {
        Set<DocumentedMethod> methods = this.readMethodsFromJson(goalFile);

        for(DocumentedMethod m : methods){
            HashSet<SimpleMethodCodeElement> referredCodeElements = codeElements
                    .stream()
                    .filter(forMethod -> forMethod.getForMethod().equals(m.getSignature()))
                    .collect(Collectors.toCollection(HashSet::new));

            if(m.returnTag() != null){
                String condition = m.returnTag().getCondition().get();
                if(!condition.equals("")) {
                    try {
                        conceptualSimMatch(db, m.returnTag(), m, referredCodeElements);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            if(!m.throwsTags().isEmpty()){
                for(Tag throwTag : m.throwsTags()){
                    String condition = throwTag.getCondition().get();
                    if(!condition.equals("")) {
                        try {
                            conceptualSimMatch(db, throwTag, m, referredCodeElements);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        OutputUtil.exportTojson(false, true);
    }

    @Override
    protected double computeSim(GloveRandomAccessReader db, String commentT, String codeElemT) throws IOException {
        DoubleVector ctVector = db.get(commentT);
        DoubleVector cetVector = db.get(codeElemT);
        CosineDistance cos = new CosineDistance();
        if(ctVector!=null && cetVector!=null) {
            double cosineDistance = -(cos.measureDistance(ctVector, cetVector)-1);
            return (1 + cosineDistance) / 2;
        }

        return 0;
    }
}
