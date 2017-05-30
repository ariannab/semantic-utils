package matching;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonStreamParser;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;

/**
 * Created by arianna on 29/05/17.
 */
public class SemanticMatcherTest {

    static String goalOutput= "goals/freecol-0.11.6/net.sf.freecol.common.model.Unit_goal.json";
    static String methods = "code-elements/net.sf.freecol.common.model.Unit_codeElements.json";

    @Test
    public void testStopwordsRemoval() throws FileNotFoundException {
        ArrayList<SimpleMethodCodeElement> collectedMethods = new ArrayList<SimpleMethodCodeElement>();
        SemanticMatcher semanticMatcher = new SemanticMatcher(
                "net.sf.freecol.common.model.Unit", true, false, false, (float).26);

        // Load all the DocumentedMethods composing a class using its goal file
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(goalOutput).getFile());

        Gson gson = new GsonBuilder().create();

        // This is the job that normally the JavaElementsCollector would do in Toradocu. For simplicity of test those code elements are stored in a Json.
        JsonStreamParser parser = new JsonStreamParser(new FileReader(new File(classLoader.getResource(methods).getFile())));
        while(parser.hasNext())
        {
            collectedMethods.add(gson.fromJson(parser.next(), SimpleMethodCodeElement.class));
        }

        semanticMatcher.run(file, collectedMethods);
    }
}
