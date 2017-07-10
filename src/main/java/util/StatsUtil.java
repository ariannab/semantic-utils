package util;

import matching.ConceptualMatcher;
import matching.SemanticMatch;
import matching.SemanticMatcher;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Set;

/**
 * Created by arianna on 27/06/17.
 */
public class StatsUtil {

    public static void computeStats(SemanticMatcher matcher){
        try {
            File file = new File("semanticStats.csv");
            FileWriter writer = new FileWriter(file, true);

            if(matcher instanceof ConceptualMatcher)
                writer.append("Conceptual Matcher");
            else
                writer.append("Basic Vector Matcher");
            writer.append("\n");

            writer.append("Class Name");
            writer.append(",");
            writer.append(matcher.className);
            writer.append("\n");

            int totCorrect = 0;
            int totPartial = 0;
            int totFail = 0;
            for(SemanticMatch sm : matcher.semanticMatches){
                if(sm.topCandidateIsCorrect){
                    totCorrect++;
                }else if(sm.candidateFound){
                    totPartial++;
                }else
                    totFail++;
            }

            writer.append("Correct matches");
            writer.append(",");
            writer.append(String.valueOf(totCorrect));
            writer.append("\n");
            writer.append("Partially correct matches");
            writer.append(",");
            writer.append(String.valueOf(totPartial));
            writer.append("\n");
            writer.append("Failed matches");
            writer.append(",");
            writer.append(String.valueOf(totFail));

            writer.append("\n\n");
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
