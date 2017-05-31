package matching;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonStreamParser;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by arianna on 29/05/17.
 */
public class SemanticMatcherTest {

    public void testStopwordsRemoval(String className, String goalOutputFile, String codeElementsFile) throws FileNotFoundException {
        Set<SimpleMethodCodeElement> collectedMethods = new HashSet<SimpleMethodCodeElement>();
        SemanticMatcher semanticMatcher = new SemanticMatcher(
                className, true, true, false, (float).24);

        // Load all the DocumentedMethods composing a class using its goal file
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(goalOutputFile).getFile());

        Gson gson = new GsonBuilder().create();

        // This is the job that normally the JavaElementsCollector would do in Toradocu. For simplicity of test those code elements are stored in a Json.
        JsonStreamParser parser = new JsonStreamParser(new FileReader(new File(classLoader.getResource(codeElementsFile).getFile())));
        while(parser.hasNext())
        {
            collectedMethods.add(gson.fromJson(parser.next(), SimpleMethodCodeElement.class));
        }

        try {
            semanticMatcher.run(file, collectedMethods);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //TODO generalize the test cases.
    @Test
    public void testAll(){
        String goalOutput= "goals/freecol-0.11.6/net.sf.freecol.common.model.Unit_goal.json";
        String codeElements = "code-elements/net.sf.freecol.common.model.Unit_codeElements.json";
        String className = codeElements.substring(codeElements.indexOf("/")+1, codeElements.indexOf("_"));

        try {
            testStopwordsRemoval(className, goalOutput, codeElements);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        goalOutput= "goals/jgrapht/org.jgrapht.Graph_goal.json";
        codeElements = "code-elements/org.jgrapht.Graph_codeElements.json";
        className = codeElements.substring(codeElements.indexOf("/")+1, codeElements.indexOf("_"));

        try {
            testStopwordsRemoval(className, goalOutput, codeElements);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


    }
}
