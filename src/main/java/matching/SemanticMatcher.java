package matching;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import de.jungblut.distance.CosineDistance;
import de.jungblut.glove.GloveRandomAccessReader;
import de.jungblut.glove.impl.GloveBinaryRandomAccessReader;
import de.jungblut.math.DoubleVector;
import edu.stanford.nlp.ling.CoreLabel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.toradocu.extractor.DocumentedMethod;
import org.toradocu.extractor.Tag;
import org.toradocu.translator.StanfordParser;
import org.toradocu.util.GsonInstance;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by arianna on 29/05/17.
 */
public class SemanticMatcher {

    static boolean stopwordsRemoval;
    static boolean posSelect;
    static boolean tfid;
    static float distanceThreshold;
    static List<String> stopwords;
    static String fileName;

    /** Stores all the {@code SemanticMatch}es collected during a test. */
    static Set<SemanticMatch> semanticMatches;

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
        semanticMatches = new HashSet<SemanticMatch>();

        //TODO very naive list. Not the best to use.
        this.stopwords =
                new ArrayList<>(
                        Arrays.asList(
                                "true", "false", "the", "a", "if", "be", "is", "are", "was", "were", "this", "do",
                                "does", "did"));

        if (stopwordsRemoval) this.fileName = "semantic_" + className;
        else this.fileName = "semantic_noSW_" + className;

        File file = new File(this.fileName);
        try {
            Files.deleteIfExists(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static Set<DocumentedMethod> readMethodsFromJson(File goalFile){
        try (BufferedReader reader =
                     Files.newBufferedReader(goalFile.toPath())){

            Set<DocumentedMethod> methods = new HashSet<DocumentedMethod>();
            methods.addAll(
                    GsonInstance.gson()
                            .fromJson(reader, new TypeToken<Set<DocumentedMethod>>() {}.getType()));
            return methods;
        } catch (IOException e) {
            System.exit(1);
        }
        return null;
    }

    /**
     * Take a goal file of a certain class in order to extract all its {@code DocumentedMethod}s and
     * the list of Java code elements that can be used in the translation.
     *  @param goalFile the class goal file
     * @param codeElements the list of Java code elements for the translation
     */
    public static void run(File goalFile, Set<SimpleMethodCodeElement> codeElements) throws IOException {
        Set<DocumentedMethod> methods = readMethodsFromJson(goalFile);
        for(DocumentedMethod m : methods){
            if(m.returnTag() != null){
                String condition = m.returnTag().getCondition().get();
                if(!condition.equals("")) {
                    try {
                        semanticMatch(m.returnTag(), m,
                                codeElements
                                        .stream()
                                        .filter(forMethod -> forMethod.getForMethod().equals(m.getSignature()))
                                        .collect(Collectors.toCollection(HashSet::new)));
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
                            semanticMatch(throwTag, m,
                                    codeElements
                                            .stream()
                                            .filter(forMethod -> forMethod.getForMethod().equals(m.getSignature()))
                                            .collect(Collectors.toCollection(HashSet::new)));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        exportInjson();
    }

    static void semanticMatch(Tag tag, DocumentedMethod method, Set<SimpleMethodCodeElement> codeElements) throws IOException {
        Set<String> commentWordSet = new HashSet<String>(Arrays.asList(parseComment(tag, method)));
        String parsedComment = String.join(" ", commentWordSet).replaceAll("\\s+", " ").trim();

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

        DoubleVector commentVector = getCommentVector(commentWordSet, db);

//        Map<MethodCodeElement, Double> distances = new HashMap<MethodCodeElement, Double>();

        Map<SimpleMethodCodeElement, Double> distances = new HashMap<SimpleMethodCodeElement, Double>();
        //    Set<CodeElement<?>> codeElements = Matcher.codeElementsMatch(method, subject, predicate);
        // for each code element, I want to take the vectors of its identifiers (like words componing the method name)
        // and compute the semantic similarity with the predicate (or the whole comment, we'll see)

//        Set<CodeElement<?>> codeElements = JavaElementsCollector.collect(method);

        int index = 0;
        if (codeElements != null && !codeElements.isEmpty()) {
            if (tfid) freq = TFIDUtils.computeTFIDF(freq, codeElements);
//            for (CodeElement<?> codeElement : codeElements) {
            for(SimpleMethodCodeElement codeElement : codeElements){
//                if (codeElement instanceof MethodCodeElement) {
                    Set<String> ids = codeElement.getCodeElementIds();
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
                        distances.put(codeElement, dist);
                    }
//                }
            }
            retainMatches(parsedComment, method.getName(), tag, distances);
//          printOnFile(tag, method, comment, distances);
        }
    }

    @Nullable
    private static DoubleVector getCommentVector(Set<String> wordComment, GloveRandomAccessReader db) throws IOException {
        DoubleVector commentVector = null;
        Iterator<String> wordIterator = wordComment.iterator();
        while(wordIterator.hasNext()){
            String word = wordIterator.next();
            if(word!=null){
                DoubleVector v = db.get(word.toLowerCase());
                if (v != null) {
                    if (commentVector == null) commentVector = v;
                    else commentVector = commentVector.add(v);
                }
            }
        }
        return commentVector;
    }

    @NotNull
    private static String[] parseComment(Tag tag, DocumentedMethod method) {
        String comment = "";
        if (posSelect) comment = POSUtils.findSubjectPredicate(tag.getComment(), method);
        else comment = tag.getComment();
        comment = comment.replaceAll("[^A-Za-z0-9! ]", "");

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

        return wordComment;
    }

    private static void exportInjson() throws IOException {
        File file = new File(fileName+"_results.json");
        try {
            Files.deleteIfExists(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        if(!semanticMatches.isEmpty()) {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName+"_results.json", true));
            for (SemanticMatch sm : semanticMatches) {
                    JsonParser jp = new JsonParser();
                    JsonElement je = jp.parse(gson.toJson(sm));
                    String prettyJsonString = gson.toJson(je);

                    writer.write(prettyJsonString+"\n");
                }
            writer.close();
        }
    }


    private static void retainMatches(String parsedComment, String name, Tag tag, Map<SimpleMethodCodeElement, Double> distances) throws IOException {
        SemanticMatch aMatch = new SemanticMatch(tag, name, parsedComment);
        distances.values().removeIf(new Predicate<Double>() {
            @Override
            public boolean test(Double aDouble) {
                return aDouble > distanceThreshold;
            }
        });

        aMatch.candidates = distances.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(/*Collections.reverseOrder()*/))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));

        if(!aMatch.candidates.isEmpty()) {
            aMatch.computeCorrectness();
            semanticMatches.add(aMatch);
        }
    }

//    private static void printOnFile(Tag tag, DocumentedMethod method, String comment, Map<MethodCodeElement, Double> distances) throws IOException {
//        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true));
//        writer.write("\nMethod: " + method.getSignature());
//
//        boolean found = false;
//        Double min = Double.valueOf(distanceThreshold);
//        Map.Entry<MethodCodeElement, Double> minEntry = null;
//
//        for (Map.Entry<MethodCodeElement, Double> entry : distances.entrySet()) {
//            if (entry.getValue() < distanceThreshold) {
//                if (entry.getValue() < min) {
//                    min = entry.getValue();
//                    minEntry = entry;
//                }
//
//                found = true;
//                writer.write(
//                        "\n"
//                                + tag.getKind()
//                                + " "
//                                + tag.getComment()
//                                + " ("
//                                + comment
//                                + ")"
//                                + " -> "
//                                + entry.getKey().getJavaExpression()
//                                + " dist="
//                                + entry.getValue());
//            }
//        }
//        if (found) writer.write("\n");
//        else writer.write("\n! WARNING: no best match found !\n");
//        if (minEntry != null) {
//            writer.write(
//                    "\nBest method match: "
//                            + minEntry.getKey().getJavaExpression()
//                            + " , distance="
//                            + minEntry.getValue()
//                            + "\n\n");
//        }
//
//        writer.close();
//    }
}
