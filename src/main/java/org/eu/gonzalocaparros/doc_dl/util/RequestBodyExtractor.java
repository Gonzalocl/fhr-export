package org.eu.gonzalocaparros.doc_dl.util;

import com.google.gson.JsonObject;
import org.eu.gonzalocaparros.doc_dl.App;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.StreamSupport;

public class RequestBodyExtractor {

    public static final Path outputFolder = Path.of("request_bodies");
    private static final int MAX_ATTEMPTS = 100;

    public static void main(String[] args) throws IOException {

        var har = App.gson.fromJson(Files.newBufferedReader(Path.of("har/requests.har")), JsonObject.class);
        var entries = har.getAsJsonObject("log").getAsJsonArray("entries");

        Files.createDirectories(outputFolder);

        StreamSupport.stream(entries.spliterator(), false)
                .map(e -> e.getAsJsonObject().getAsJsonObject("request"))
                .filter(r -> r.getAsJsonPrimitive("bodySize").getAsInt() > 0)
                .forEach(RequestBodyExtractor::writeBodyToFile);

    }

    private static void writeBodyToFile(JsonObject request) {
        var mimeType = request.getAsJsonObject("postData").getAsJsonPrimitive("mimeType").getAsString();
        var body = request.getAsJsonObject("postData").getAsJsonPrimitive("text").getAsString();
        var method = request.getAsJsonPrimitive("method").getAsString();
        var url = request.getAsJsonPrimitive("url").getAsString();

        var fileExtension = "application/json".equals(mimeType) ? ".json" : "";
        var fileName = String.join("_", method, url).replace("/", "-");
        var outputFile = outputFolder.resolve(fileName + fileExtension);

        var i = 0;
        while (i < MAX_ATTEMPTS && Files.exists(outputFile)) {
            outputFile = outputFolder.resolve(fileName + "_" + ++i + fileExtension);
        }

        if (i == MAX_ATTEMPTS) {
            System.err.println("ERROR: (" + fileName + ") repeated more than " + MAX_ATTEMPTS + " times.");
            return;
        }

        System.out.println(outputFile);

        try {
            Files.writeString(outputFile, body);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
