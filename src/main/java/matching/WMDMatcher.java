package matching;

/**
 * Created by arianna on 10/07/17.
 */
public class WMDMatcher extends SemanticMatcher {
    public WMDMatcher(String className, boolean stopwordsRemoval, boolean posSelect, boolean tfid, float distanceThreshold) {
        super(className, stopwordsRemoval, posSelect, tfid, distanceThreshold);
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
}
