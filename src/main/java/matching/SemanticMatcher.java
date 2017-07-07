package matching;

import com.google.gson.reflect.TypeToken;
import de.jungblut.distance.CosineDistance;
import de.jungblut.glove.GloveRandomAccessReader;
import de.jungblut.math.DoubleVector;
import edu.stanford.nlp.ling.CoreLabel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.toradocu.extractor.DocumentedMethod;
import org.toradocu.extractor.Tag;
import org.toradocu.translator.StanfordParser;
import org.toradocu.util.GsonInstance;
import util.AlignmentScore;
import util.OutputUtil;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by arianna on 29/05/17.
 *
 * Main class. Contains all the methods to compute the {@code SemantichMatch}es for a given class.
 *
 */
public class SemanticMatcher {

    static boolean stopwordsRemoval;
    static boolean posSelect;
    static boolean tfid;
    static float distanceThreshold;
    static List<String> stopwords;
    public static String fileName;

    /** Stores all the {@code SemanticMatch}es collected during a test. */
    public static Set<SemanticMatch> semanticMatches;

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
                                "true", "false", "the", "a", "if", "for", "be", "is", "are", "was", "were", "this", "do",
                                "does", "did", "not", "of"));

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
     * Takes a goal file of a certain class in order to extract all its {@code DocumentedMethod}s and
     * the list of Java code elements that can be used in the translation.
     *
     * @param db
     * @param goalFile the class goal file
     * @param codeElements the list of Java code elements for the translation
     */
    public static void runVectorMatch(GloveRandomAccessReader db, File goalFile, Set<SimpleMethodCodeElement> codeElements) throws IOException {
        Set<DocumentedMethod> methods = readMethodsFromJson(goalFile);

        for(DocumentedMethod m : methods){
            HashSet<SimpleMethodCodeElement> referredCodeElements = codeElements
                    .stream()
                    .filter(forMethod -> forMethod.getForMethod().equals(m.getSignature()))
                    .collect(Collectors.toCollection(HashSet::new));

            if(m.returnTag() != null){
                String condition = m.returnTag().getCondition().get();
                if(!condition.equals("")) {
                    try {
                        vectorsMatch(db, m.returnTag(), m, referredCodeElements);
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
                            vectorsMatch(db, throwTag, m, referredCodeElements);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        OutputUtil.exportTojson(false, false);
    }


    public static void runConceptualSim(GloveRandomAccessReader db, File goalFile, Set<SimpleMethodCodeElement> codeElements) throws IOException {
        Set<DocumentedMethod> methods = readMethodsFromJson(goalFile);

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


//    public void runWmdMatch(File goalFile, Set<SimpleMethodCodeElement> codeElements) throws IOException {
//        Set<DocumentedMethod> methods = readMethodsFromJson(goalFile);
//
//        ClassLoader classLoader = getClass().getClassLoader();
//        File file = new File(classLoader.getResource("GoogleNews-vectors-negative300.bin.gz").getFile());
//        WordVectors vectors = WordVectorSerializer.loadGoogleModel(file, true);
//        WordMovers wm = WordMovers.Builder().wordVectors(vectors).build();
//
//        for(DocumentedMethod m : methods){
//            HashSet<SimpleMethodCodeElement> referredCodeElements = codeElements
//                    .stream()
//                    .filter(forMethod -> forMethod.getForMethod().equals(m.getSignature()))
//                    .collect(Collectors.toCollection(HashSet::new));
//
//            if(m.returnTag() != null){
//                String condition = m.returnTag().getCondition().get();
//                if(!condition.equals("")) {
//                    try {
//                        wmdMatch(wm, m.returnTag(), m, referredCodeElements);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//            if(!m.throwsTags().isEmpty()){
//                for(Tag throwTag : m.throwsTags()){
//                    String condition = throwTag.getCondition().get();
//                    if(!condition.equals("")) {
//                        try {
//                            wmdMatch(wm, throwTag, m, referredCodeElements);
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            }
//        }
//        exportTojson(true);
//    }



    /**
     * Computes semantic matching through GloVe vectors.
     *
     * @param tag the tag for which we want to produce a condition translation
     * @param method the method the tag belongs to
     * @param codeElements the code elements that are possible candidates to use in the translation
     * @throws IOException if the GloVe database couldn't be read
     */
    static void vectorsMatch(GloveRandomAccessReader db, Tag tag, DocumentedMethod method, Set<SimpleMethodCodeElement> codeElements) throws IOException {
        Set<String> commentWordSet = parseComment(tag, method);
        String parsedComment = String.join(" ", commentWordSet).replaceAll("\\s+", " ").trim();
        Map<String, Double> freq = new HashMap<String, Double>();
        CosineDistance cos = new CosineDistance();

        DoubleVector commentVector = getCommentVector(commentWordSet, db);

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
                DoubleVector methodVector = getCodeElementVector(db, freq, codeElement);

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

//    void wmdMatch(WordMovers wm, Tag tag, DocumentedMethod method, Set<SimpleMethodCodeElement> codeElements) throws IOException {
//        Map<SimpleMethodCodeElement, Double> distances = new HashMap<SimpleMethodCodeElement, Double>();
//
//        String parsedComment = tag.getComment();
//        if (codeElements != null && !codeElements.isEmpty()) {
//            for(SimpleMethodCodeElement codeElement : codeElements){
//                Set<String> ids = codeElement.getCodeElementIds();
//                for (String id : ids) {
//                    String[] camelId = id.split("(?<!^)(?=[A-Z])");
//                    String joinedId = String.join(" ", camelId).replaceAll("\\s+", " ").trim();
//                    double dist = wm.distance(parsedComment, joinedId);
//                    distances.put(codeElement, dist);
//                }
//            }
//        }
//        retainMatches(parsedComment, method.getName(), tag, distances);
//    }

    /**
     * Build the vector representing a code element, made by its IDs camel case-splitted
     *
     * @param db gloVe database
     * @param freq TFID map
     * @param codeElement the code element
     * @return a {@code DoubleVector} representing the code element vector
     * @throws IOException if the database couldn't be read
     */
    private static DoubleVector getCodeElementVector(GloveRandomAccessReader db, Map<String, Double> freq, SimpleMethodCodeElement codeElement) throws IOException {
        int index;
        Set<String> ids = codeElement.getCodeElementIds();
        DoubleVector codeElementVector = null;
        for (String id : ids) {
            String[] camelId = id.split("(?<!^)(?=[A-Z])");
            String joinedId = String.join(" ", camelId).replaceAll("\\s+", " ").toLowerCase().trim();
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
                        if (codeElementVector == null) codeElementVector = v;
                        else codeElementVector = codeElementVector.add(v);
                    }
                }
            }
        }
        return codeElementVector;
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


    static void conceptualSimMatch(GloveRandomAccessReader db, Tag tag, DocumentedMethod method, Set<SimpleMethodCodeElement> codeElements) throws IOException {
        Set<String> commentWordSet = parseComment(tag, method);
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
//                Set<String> codeElementWordSet = new HashSet<String>(Arrays.asList(camelId));
//                double totalSimilarity = computeTotSimilarity(db, commentWordSet, codeElementWordSet);
                distances.put(codeElement,computeAlignmentMatrix(db, commentWordSet, codeElementWordSet));
                //Normalize the computed similarities
//                double conceptualSimilarity = totalSimilarity*2/(commentWordSet.size()+codeElementWordSet.size());
//                distances.put(codeElement, conceptualSimilarity);
            }
            retainMatches(parsedComment, method.getName(), tag, distances);
        }
    }

    private static double computeTotSimilarity(GloveRandomAccessReader db,
                                               Set<String> commentTerms,
                                               Set<String> codeElementTerms) throws IOException {
       /*
       * Per ogni termine nel commento devo trovare il best match con un termine del code element (E DA LÌ LI ELIMINO?).
       * Una volta trovato, lo tengo da parte. Ripeto finché non ho esaurito tutti i termini del commento (E DEL CODE ELEMENT?).
       * I valori tenuti da parte vanno sommati: il risultato è Txy.
       */
       double bestMatch = 0;
       double sum = 0;
       for(String commentT : commentTerms){
           for(String codeElemT : codeElementTerms){
               double sim = computeSim(db, commentT, codeElemT);
               if(sim > bestMatch) bestMatch = sim;
           }
           sum += bestMatch;
           bestMatch = 0;
       }
       return sum;
    }


    private static double computeAlignmentMatrix(GloveRandomAccessReader db,
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
                double sim = computeSim(db, x, y);
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
            if(currentBest!=0 && currentY!="") {
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

    private static double computeSim(GloveRandomAccessReader db, String commentT, String codeElemT) throws IOException {
        DoubleVector ctVector = db.get(commentT);
        DoubleVector cetVector = db.get(codeElemT);
        CosineDistance cos = new CosineDistance();
        if(ctVector!=null && cetVector!=null) {
            double cosineDistance = -(cos.measureDistance(ctVector, cetVector)-1);
            return (1 + cosineDistance) / 2;
        }

        return 0;
    }


    /**
     * Parse the original tag comment according to the configuration parameters.
     *
     * @param tag the {@code Tag} the comment belongs to
     * @param method the {@code DocumentedMethod} containing the tag
     * @return the parsed comment in form of array of strings (words)
     */
    @NotNull
    private static Set<String> parseComment(Tag tag, DocumentedMethod method) {
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
        Set<String> wordList = removeStopWords(wordComment);

        return wordList;
    }

    @NotNull
    private static Set<String> removeStopWords(String[] words) {
        if (stopwordsRemoval) {
            for (int i = 0; i != words.length; i++) {
                if (stopwordsRemoval && stopwords.contains(words[i].toLowerCase()))
                    words[i] = "";
            }
        }
        Set<String> wordList = new HashSet<>(Arrays.asList(words));
        wordList.removeAll(Arrays.asList(""));
        return wordList;
    }


    /**
     * Compute and instantiate the {@code SemantiMatch} for a tag.
     *
     * @param parsedComment the parse tag comment
     * @param methodName name of the method the tag belongs to
     * @param tag the {@code Tag}
     * @param distances the computed distance, for every possible code element candidate, from the parsed comment
     */
    private static void retainMatches(String parsedComment, String methodName, Tag tag, Map<SimpleMethodCodeElement, Double> distances){
        SemanticMatch aMatch = new SemanticMatch(tag, methodName, parsedComment, distanceThreshold);

        // Select as candidates only code elements that have a semantic distance below the chosen threshold.
        distances.values().removeIf(new Predicate<Double>() {
            @Override
            public boolean test(Double aDouble) {
                return aDouble < distanceThreshold;
            }
        });

        aMatch.candidates = distances.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));

        if(!aMatch.candidates.isEmpty()) {
            aMatch.computeCorrectness();
            aMatch.computePartialCorrectness();
            semanticMatches.add(aMatch);
        }
    }

}
