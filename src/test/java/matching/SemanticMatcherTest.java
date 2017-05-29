package matching;

import org.junit.Test;

/**
 * Created by arianna on 29/05/17.
 */
public class SemanticMatcherTest {
    static String freecolPath= "resources/freecol-0.11.6/net.sf.freecol.common.model.Unit_goal.json";


    @Test
    public void testStopwordsRemoval(){
        SemanticMatcher semanticMatcher = new SemanticMatcher(
                "net.sf.freecol.common.model.Unit", true, false, false, (float).26);

        semanticMatcher.run(freecolPath);
    }
}
