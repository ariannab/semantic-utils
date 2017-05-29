import com.google.gson.reflect.TypeToken;
import de.jungblut.distance.CosineDistance;
import de.jungblut.glove.GloveRandomAccessReader;
import de.jungblut.glove.impl.GloveBinaryRandomAccessReader;
import de.jungblut.math.DoubleVector;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import org.toradocu.extractor.DocumentedMethod;
import org.toradocu.extractor.Tag;
import org.toradocu.translator.CodeElement;
import org.toradocu.translator.JavaElementsCollector;
import org.toradocu.translator.MethodCodeElement;
import org.toradocu.translator.StanfordParser;
import org.toradocu.util.GsonInstance;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by arianna on 29/05/17.
 */
public class SemanticMatcher {

    private static List<DocumentedMethod> methods;
    boolean stopwordsRemoval;
    boolean posSelect;
    boolean tfid;
    float distanceThreshold;
    List<String> stopwords;
    String fileName;


    public SemanticMatcher(
            String className,
            boolean stopwordsRemoval,
            boolean posSelect,
            boolean tfid,
            float distanceThreshold) {

        this.tfid = tfid;
        this.stopwordsRemoval = stopwordsRemoval;
        this.posSelect = posSelect;
        this.distanceThreshold = distanceThreshold;

        //TODO very naive list. Not the best to use.
        this.stopwords =
                new ArrayList<>(
                        Arrays.asList(
                                "true", "false", "the", "a", "if", "be", "is", "are", "was", "were", "this", "do",
                                "does", "did"));

        if (stopwordsRemoval) this.fileName = "semanticOutputs/semanticMatch_" + className + ".txt";
        else this.fileName = "semanticOutputs/semanticMatch_noSW_" + className + ".txt";

        File file = new File(this.fileName);
        try {
            Files.deleteIfExists(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void readMethodsFromJson(){
        String freecolPath= "resources/freecol-0.11.6/net.sf.freecol.common.model.Unit_goal.json";
        try (BufferedReader reader =
                     Files.newBufferedReader(new File(freecolPath).toPath())){
            methods = new ArrayList<>();
            methods.addAll(
                    GsonInstance.gson()
                            .fromJson(reader, new TypeToken<List<DocumentedMethod>>() {}.getType()));
        } catch (IOException e) {
            System.exit(1);
        }
    }

    public static void main(String[] args){
        readMethodsFromJson();
        for(DocumentedMethod m : methods){
            if(m.returnTag() != null){
                String condition = m.returnTag().getCondition().toString().replace("Optional[", "").replace("]", "");
                if(!condition.equals("")) {
                    System.out.println(m.getSignature());
                    System.out.println("\""+m.returnTag().getComment()+"\"");
                    System.out.println(condition);
                    System.out.println("\n");
                }
            }

        }
    }


    public void semanticMatch(Tag tag, DocumentedMethod method) throws IOException {
        String comment = "";
        if (posSelect) comment = findSubjectPredicate(tag.getComment(), method);
        else comment = tag.getComment();
        comment = comment.replaceAll("[^A-Za-z0-9]", "");

        String[] wordComment = comment.split(" ");
        int index = 0;
        List<CoreLabel> lemmas = StanfordParser.lemmatize(comment);
        if (wordComment.length != lemmas.size()) System.out.println("?");
        for (CoreLabel lemma : lemmas) {
            if (lemma != null) wordComment[index] = lemma.lemma();
            index++;
        }
        if (stopwordsRemoval) {
            for (int i = 0; i != wordComment.length; i++) {
                if (stopwordsRemoval && stopwords.contains(wordComment[i].toLowerCase()))
                    wordComment[i] = "";
            }
        }

        comment = String.join(" ", wordComment).replaceAll("\\s+", " ").trim();

        GloveRandomAccessReader db = null;
        try {
            db =
                    new GloveBinaryRandomAccessReader(
                            Paths.get("/home/arianna/Scaricati/glove-master/target/glove-binary"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Map<String, Double> freq = new HashMap<String, Double>();
        CosineDistance cos = new CosineDistance();

        DoubleVector commentVector = null;
        for (int i = 0; i != wordComment.length; i++) {
            DoubleVector v = db.get(wordComment[i].toLowerCase());
            if (v != null) {
                if (commentVector == null) commentVector = v;
                else commentVector = commentVector.add(v);
            }
        }

        Map<MethodCodeElement, Double> distances = new HashMap<MethodCodeElement, Double>();
        //    Set<CodeElement<?>> codeElements = Matcher.codeElementsMatch(method, subject, predicate);
        // for each code element, I want to take the vectors of its identifiers (like words componing the method name)
        // and compute the semantic similarity with the predicate (or the whole comment, we'll see)

        Set<CodeElement<?>> codeElements = JavaElementsCollector.collect(method);

        if (codeElements != null && !codeElements.isEmpty()) {
            if (tfid) freq = computeTFIDF(freq, codeElements);
            for (CodeElement<?> codeElement : codeElements) {
                if (codeElement instanceof MethodCodeElement) {
                    Set<String> ids = codeElement.getIdentifiers();
                    DoubleVector methodVector = null;
                    for (String id : ids) {
                        String[] camelId = id.split("(?<!^)(?=[A-Z])");
                        String joinedId = String.join(" ", camelId).replaceAll("\\s+", " ").trim();
                        index = 0;
                        for (CoreLabel lemma : StanfordParser.lemmatize(joinedId)) {
                            if (lemma != null) camelId[index] = lemma.lemma();

                            index++;
                        }
                        for (int i = 0; i != camelId.length; i++) {
                            if (!tfid || (tfid && freq.get(camelId[i].toLowerCase()) < 0.5)) {
                                DoubleVector v = db.get(camelId[i].toLowerCase());
                                if (stopwordsRemoval && stopwords.contains(camelId[i].toLowerCase())) continue;
                                if (v != null) {
                                    if (methodVector == null) methodVector = v;
                                    else methodVector = methodVector.add(v);
                                }
                            }
                        }
                    }

                    if (methodVector != null && commentVector != null) {
                        double dist = cos.measureDistance(methodVector, commentVector);
                        distances.put((MethodCodeElement) codeElement, dist);
                    }
                }
            }
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true));
            writer.write("\nMethod: " + method.getSignature());

            boolean found = false;
            Double min = Double.valueOf(distanceThreshold);
            Map.Entry<MethodCodeElement, Double> minEntry = null;

            for (Map.Entry<MethodCodeElement, Double> entry : distances.entrySet()) {
                if (entry.getValue() < distanceThreshold) {
                    if (entry.getValue() < min) {
                        min = entry.getValue();
                        minEntry = entry;
                    }

                    found = true;
                    writer.write(
                            "\n"
                                    + tag.getKind()
                                    + " "
                                    + tag.getComment()
                                    + " ("
                                    + comment
                                    + ")"
                                    + " -> "
                                    + entry.getKey().getJavaExpression()
                                    + " dist="
                                    + entry.getValue());
                }
            }
            if (found) writer.write("\n");
            else writer.write("\n! WARNING: no best match found !\n");
            if (minEntry != null) {
                writer.write(
                        "\nBest method match: "
                                + minEntry.getKey().getJavaExpression()
                                + " , distance="
                                + minEntry.getValue()
                                + "\n\n");
            }

            writer.close();
        }
    }

    private static String findSubjectPredicate(String comment, DocumentedMethod method) {
        String result = "";
        for (SemanticGraph sg : StanfordParser.getSemanticGraphs(comment, method)) {
            //Search nouns and their adjectives.

            List<IndexedWord> nouns = sg.getAllNodesByPartOfSpeechPattern("NN(.*)");
            List<IndexedWord> adj = sg.getAllNodesByPartOfSpeechPattern("JJ(.*)");
            for (IndexedWord n : nouns) result += n.word() + " ";
            for (IndexedWord a : adj) result += a.word() + " ";

            //Search for a verb.
            List<IndexedWord> verbs = sg.getAllNodesByPartOfSpeechPattern("VB(.*)");
            for (IndexedWord v : verbs) result += v.word() + " ";
        }
        return result.replaceAll("\\s+", " ").trim();
    }

    private static Map<String, Double> computeTFIDF(
            Map<String, Double> freq, Set<CodeElement<?>> codeElements) {
        for (CodeElement<?> codeElement : codeElements) {
            if (codeElement instanceof MethodCodeElement) {
                freq = storeFrequencies(codeElement.getIdentifiers(), freq);
            }
        }

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
