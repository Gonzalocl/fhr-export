package org.eu.gonzalocaparros.doc_dl.util;

import com.google.gson.JsonObject;
import org.eu.gonzalocaparros.doc_dl.App;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class BodyQueryExtractor {

    public static void main(String[] args) throws IOException {

        Files.list(RequestBodyExtractor.outputFolder)
                .forEach(BodyQueryExtractor::extractAndSaveQuery);
    }

    private static void extractAndSaveQuery(Path bodyPath) {
        try {
            var query = extractQuery(bodyPath);
            Files.writeString(Path.of(bodyPath.toAbsolutePath() + "_query"), query);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String extractQuery(Path bodyPath) throws IOException {

        var body = App.gson.fromJson(Files.newBufferedReader(bodyPath), JsonObject.class);

        return body.getAsJsonPrimitive("query").getAsString();
    }
}
