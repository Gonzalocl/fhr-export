package org.eu.gonzalocaparros.doc_dl;

import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

public class Calls {

    public static JsonObject graphqlCallSaveAndParseJsonObject(Body body, Path savePath) throws IOException, InterruptedException {

        if (Files.exists(savePath)) {
            throw new IllegalStateException("Error calling: " + body.toLog() + "; File already exists: " + savePath);
        }

        var request = HttpRequest.newBuilder()
                .uri(URI.create(App.cfg.baseUrl() + "/graphql"))
                .POST(HttpRequest.BodyPublishers.ofString(App.gson.toJson(body)))
                .setHeader("content-type", "application/json")
                .setHeader("cookie", App.cfg.sessionCookie())
                .build();

        var response = App.httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        Files.writeString(savePath, response.body());

        if (response.statusCode() != 200) {
            throw new IllegalStateException("Error calling: " + body.toLog() + "; Request response: " + response + "; Save path: " + savePath);
        }

        return App.gson.fromJson(response.body(), JsonObject.class);
    }

    public static JsonObject documentDownloadAndMetadata(Document documentMeta, Path savePath, Collection<String> errors) throws IOException, InterruptedException {

        if (Files.exists(savePath)) {
            throw new IllegalStateException("Error downloading document: " + documentMeta + "; File already exists: " + savePath);
        }

        var documentRequest = HttpRequest.newBuilder()
                .uri(URI.create(App.cfg.baseUrl() + "/" + documentMeta.file()))
                .GET()
                .setHeader("cookie", App.cfg.sessionCookie())
                .build();

        var documentResponse = App.httpClient.send(documentRequest, HttpResponse.BodyHandlers.ofFile(savePath));

        if (documentResponse.statusCode() != 200) {
            errors.add("Error downloading document: " + documentMeta + "; Request response: " + documentResponse + "; Save path:" + savePath);
        }

        var saveMetadataPath = savePath.resolveSibling(savePath.getFileName() + ".json");

        if (Files.exists(saveMetadataPath)) {
            throw new IllegalStateException("Error downloading document metadata: " + documentMeta + "; File already exists: " + saveMetadataPath);
        }

        var metadataRequest = HttpRequest.newBuilder()
                .uri(URI.create(App.cfg.baseUrl() + "/" + documentMeta.fileUrl()))
                .GET()
                .setHeader("content-type", "application/json")
                .setHeader("cookie", App.cfg.sessionCookie())
                .build();

        var metadataResponse = App.httpClient.send(metadataRequest, HttpResponse.BodyHandlers.ofString());

        Files.writeString(saveMetadataPath, metadataResponse.body());

        if (metadataResponse.statusCode() != 200) {
            throw new IllegalStateException("Error downloading document metadata: " + documentMeta + "; Request response: " + metadataResponse + "; Save path:" + saveMetadataPath);
        }

        return App.gson.fromJson(metadataResponse.body(), JsonObject.class);
    }
}
