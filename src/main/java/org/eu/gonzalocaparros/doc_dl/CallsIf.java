package org.eu.gonzalocaparros.doc_dl;

import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

public class CallsIf {

    public static JsonObject graphqlCallSaveAndParseJsonObject(Body body, Path savePath) throws IOException, InterruptedException {
        if (Files.exists(savePath)) {
            return CallsFile.graphqlCallSaveAndParseJsonObject(body, savePath);
        }
        return Calls.graphqlCallSaveAndParseJsonObject(body, savePath);
    }

    public static JsonObject documentDownloadAndMetadata(Document documentMeta, Path savePath, Collection<String> errors) throws IOException, InterruptedException {
        if (Files.exists(savePath)) {
            return CallsFile.documentDownloadAndMetadata(documentMeta, savePath, errors);
        }
        return Calls.documentDownloadAndMetadata(documentMeta, savePath, errors);
    }
}
