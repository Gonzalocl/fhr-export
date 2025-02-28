package org.eu.gonzalocaparros.doc_dl.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import org.eu.gonzalocaparros.doc_dl.App;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

public class BodyChecker {

    private static Set<String> jsonFields = Set.of("operationName", "variables", "query");

    public static void main(String[] args) throws IOException {

        Files.list(RequestBodyExtractor.outputFolder)
                .forEach(BodyChecker::checkAndLogBody);
    }

    private static void checkAndLogBody(Path bodyPath) {
        try {
            var result = checkBody(bodyPath);
            System.out.println(result + ": " + bodyPath);
        } catch (IOException | JsonSyntaxException e) {
            System.out.println("ERROR: " + bodyPath);
//            e.printStackTrace();
        }
    }

    public static boolean checkBody(Path bodyPath) throws IOException {

        var body = App.gson.fromJson(Files.newBufferedReader(bodyPath), JsonObject.class);

        return jsonFields.equals(body.keySet());
    }
}
