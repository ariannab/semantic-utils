package matching;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonStreamParser;
import de.jungblut.glove.GloveRandomAccessReader;
import de.jungblut.glove.impl.GloveBinaryRandomAccessReader;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.junit.Test;
import util.SimpleMethodCodeElement;
import util.StatsUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by arianna on 29/05/17.
 */
public class SemanticMatcherTest {

    public void testVectorMatch(GloveRandomAccessReader db, String className, String goalOutputFile, String codeElementsFile)
            throws FileNotFoundException {
        Set<SimpleMethodCodeElement> collectedMethods = new HashSet<>();
        // Ideal threshold for vector sum is 0.24
        SemanticMatcher semanticMatcher = new SemanticMatcher(
                className, true, true, false, (float)-1);

        // Load all the DocumentedMethods composing a class using its goal file
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(goalOutputFile).getFile());

        Gson gson = new GsonBuilder().create();

        // This is the job that normally the JavaElementsCollector would do in Toradocu.
        // For simplicity of test those code elements are stored in a Json.
        JsonStreamParser parser = new JsonStreamParser(new FileReader(new File(classLoader.getResource(codeElementsFile).getFile())));
        while(parser.hasNext())
        {
            collectedMethods.add(gson.fromJson(parser.next(), SimpleMethodCodeElement.class));
        }

        try {
            semanticMatcher.runVectorMatch(db, file, collectedMethods);
            StatsUtil.computeStats(semanticMatcher);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void testConcSimMatch(GloveRandomAccessReader db, String className, String goalOutputFile, String codeElementsFile)
            throws FileNotFoundException {
        Set<SimpleMethodCodeElement> collectedMethods = new HashSet<>();
        // Ideal threshold for conceptual similarity is 0.75
        ConceptualMatcher semanticMatcher = new ConceptualMatcher(
                className, true, true, false, (float)-1);

        // Load all the DocumentedMethods composing a class using its goal file
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(goalOutputFile).getFile());

        Gson gson = new GsonBuilder().create();

        // This is the job that normally the JavaElementsCollector would do in Toradocu.
        // For simplicity of test those code elements are stored in a Json.
        JsonStreamParser parser = new JsonStreamParser(new FileReader(new File(classLoader.getResource(codeElementsFile).getFile())));
        while(parser.hasNext())
        {
            collectedMethods.add(gson.fromJson(parser.next(), SimpleMethodCodeElement.class));
        }

        try {
            semanticMatcher.runConceptualSim(db, file, collectedMethods);
            StatsUtil.computeStats(semanticMatcher);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void testWmdMatch(String className, String goalOutputFile, String codeElementsFile, WordVectors vectors) throws FileNotFoundException {
        Set<SimpleMethodCodeElement> collectedMethods = new HashSet<>();
        // Ideal threshold for WMD is 5.5
        WMDMatcher semanticMatcher = new WMDMatcher(
                className, true, true, false, (float)-1);

        // Load all the DocumentedMethods composing a class using its goal file
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(goalOutputFile).getFile());

        Gson gson = new GsonBuilder().create();

        // This is the job that normally the JavaElementsCollector would do in Toradocu.
        // For simplicity of test those code elements are stored in a Json.
        JsonStreamParser parser = new JsonStreamParser(new FileReader(new File(classLoader.getResource(codeElementsFile).getFile())));
        while(parser.hasNext())
        {
            collectedMethods.add(gson.fromJson(parser.next(), SimpleMethodCodeElement.class));
        }
        semanticMatcher.runWmdMatch(file, collectedMethods, vectors);
        StatsUtil.computeStats(semanticMatcher);
    }


    //TODO generalize the test cases.
    @Test
    public void testAll(){
        File file = new File("semanticStats.csv");
        try {
            Files.deleteIfExists(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // vectors databases are loaded only once
        GloveRandomAccessReader gloveBinaryDb = setUpGloveBinaryDB();
        WordVectors gloveVectors = setUpGloveTxtVectors();

        String goalOutput= "goals/freecol-0.11.6/net.sf.freecol.common.model.Unit_goal.json";
        String codeElements = "code-elements/net.sf.freecol.common.model.Unit_codeElements.json";
        String className = codeElements.substring(codeElements.indexOf("/")+1, codeElements.indexOf("_"));

        try {
            testVectorMatch(gloveBinaryDb, className, goalOutput, codeElements);
            testConcSimMatch(gloveBinaryDb, className, goalOutput, codeElements);
            testWmdMatch(className, goalOutput, codeElements, gloveVectors);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        goalOutput= "goals/jgrapht/org.jgrapht.Graph_goal.json";
        codeElements = "code-elements/org.jgrapht.Graph_codeElements.json";
        className = codeElements.substring(codeElements.indexOf("/")+1, codeElements.indexOf("_"));

        try {
            testVectorMatch(gloveBinaryDb, className, goalOutput, codeElements);
            testConcSimMatch(gloveBinaryDb, className, goalOutput, codeElements);
            testWmdMatch(className, goalOutput, codeElements, gloveVectors);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


    }

    private WordVectors setUpGloveTxtVectors() {
        File gloveTxt = new File("/home/arianna/Scaricati/glove-master/target/glove.6B.300d.txt");
        WordVectors gloveVectors = null;
        try {
            gloveVectors = WordVectorSerializer.loadTxtVectors(gloveTxt);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return gloveVectors;
    }

    private GloveRandomAccessReader setUpGloveBinaryDB() {
        GloveRandomAccessReader gloveBinaryDb = null;
        try {
            gloveBinaryDb =
                    new GloveBinaryRandomAccessReader(
                            Paths.get("/home/arianna/Scaricati/glove-master/target/glove-binary"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return gloveBinaryDb;
    }
}
