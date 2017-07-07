package util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import matching.SemanticMatch;
import matching.SemanticMatcher;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Created by arianna on 27/06/17.
 */
public class OutputUtil {
    /**
     * Exports the result in a JSON format.
     *
     * @throws IOException if there were problems accessing the file
     */
    public static void exportTojson(boolean wmd, boolean concSim) throws IOException {
        String resultFile="";
        if(wmd)
            resultFile = SemanticMatcher.fileName+"_wmd.json";
        else if(concSim)
           resultFile = SemanticMatcher.fileName+"_concSim_results.json";
        else
            resultFile = SemanticMatcher.fileName+"_vectors_results.json";


        File file = new File(resultFile);
        try {
            Files.deleteIfExists(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        if(!SemanticMatcher.semanticMatches.isEmpty()) {
            BufferedWriter writer = new BufferedWriter(new FileWriter(resultFile, true));
            for (SemanticMatch sm : SemanticMatcher.semanticMatches) {
                    JsonParser jp = new JsonParser();
                    JsonElement je = jp.parse(gson.toJson(sm));
                    String prettyJsonString = gson.toJson(je);

                    writer.write(prettyJsonString+"\n");
                }
            writer.close();
        }
    }
}
