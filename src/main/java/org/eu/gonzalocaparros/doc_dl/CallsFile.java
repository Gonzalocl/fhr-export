package org.eu.gonzalocaparros.doc_dl;

import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

public class CallsFile {

    public static JsonObject graphqlCallSaveAndParseJsonObject(Body body, Path savePath) throws IOException, InterruptedException {
        return App.gson.fromJson(Files.newBufferedReader(savePath), JsonObject.class);
    }

    public static JsonObject documentDownloadAndMetadata(Document documentMeta, Path savePath, Collection<String> errors) throws IOException, InterruptedException {
        var saveMetadataPath = savePath.resolveSibling(savePath.getFileName() + ".json");
        return App.gson.fromJson(Files.newBufferedReader(saveMetadataPath), JsonObject.class);
    }
}
