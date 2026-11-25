package org.eu.gonzalocaparros.doc_dl;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/*
To get the cookies, log in, run the code in the JavaScript console and click anywhere in the page
document.onclick = () => navigator.clipboard.writeText(document.cookie)

The necessary is _factorial_session_v2 which is a HttpOnly cookie, it can't be accessed with JavaScript.
To get it go to DevTools > Application > Cookies > https://app.factorialhr.com/ > _factorial_session_v2
*/

// TODO
// faltan llamadas esparcidas

public class App {

    public static final Gson gson = new Gson();
    public static final HttpClient httpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
    public static final LinkedList<String> globalErrors = new LinkedList<>();
    public static final LinkedList<String> errorFiles = new LinkedList<>();
    private static final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    private static final String[][] documentMetadataKeys = {
            {"id"},
            {"arCreatedAt", "ar_created_at"},
            {"file"},
            {"filename"},
            {"fileUrl", "file_url"}
    };
    public static Configuration cfg;
    public static Path rootOutputPath;
    public static Path errorOutputPath;

    public static void main(String[] args) throws IOException, InterruptedException {

        cfg = getArgsConfigurationOrDefault(args);
        rootOutputPath = Path.of(cfg.rootOutputFile());
        errorOutputPath = rootOutputPath.resolve(cfg.errorOutputFile());

        Files.createDirectories(rootOutputPath);

        var body = Operation.FOLDERS.getBody(new Operation.RootFoldersVariables(cfg.employeeId()));
        var rootFolders = CallsIf.graphqlCallSaveAndParseJsonObject(body, rootOutputPath.resolve(rootOutputPath.getFileName() + "-Folders.json"));

        body = Operation.DOCUMENTS.getBody(new Operation.RootDocumentsVariables(cfg.employeeId()));
        var rootDocuments = CallsIf.graphqlCallSaveAndParseJsonObject(body, rootOutputPath.resolve(rootOutputPath.getFileName() + "-Documents.json"));

        var errors = new LinkedList<String>();

        processFolders(rootFolders, rootOutputPath, errors);
        processDocuments(rootDocuments, rootOutputPath, errors);

        logErrors(errors, rootOutputPath);

        if (globalErrors.isEmpty() && errorFiles.isEmpty()) {
            System.out.println("\n\nNo errors.");
            return;
        }

        var errorSummary = String.join("\n", globalErrors) + "\n\nError Summary:\n" + String.join("\n", errorFiles);
        System.out.println(errorSummary);
        Files.writeString(errorOutputPath, errorSummary, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    private static Collection<Folder> processFolders(JsonObject folders, Path parentFolderPath, List<String> errors) {
        var returnFolders = new LinkedList<Folder>();
        try {
            folders.getAsJsonObject("data")
                    .getAsJsonObject("documents")
                    .getAsJsonArray("folders")
                    .forEach(f -> processFolder(f.getAsJsonObject(), parentFolderPath, errors).ifPresent(returnFolders::add));
        } catch (Throwable t) {
            errors.add(String.format("Exception processFolders: %s; %s", folders, parentFolderPath));
            errors.add(stackTraceToString(t));
        }

        return returnFolders;
    }

    private static Optional<Folder> processFolder(JsonObject folder, Path parentFolderPath, List<String> errors) {
        try {
            return Optional.of(processFolder(folder, parentFolderPath));
        } catch (Throwable t) {
            errors.add(String.format("Exception processFolder: %s; %s", folder, parentFolderPath));
            errors.add(stackTraceToString(t));
        }
        return Optional.empty();
    }

    private static Folder processFolder(JsonObject folderMeta, Path parentFolderPath) {
        var folder = gson.fromJson(folderMeta, Folder.class);
        var folderPath = parentFolderPath.resolve(folder.id() + "-" + folder.name());

        try {
            Files.createDirectories(folderPath);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        var errors = new LinkedList<String>();

        try {
            var body = Operation.FOLDERS.getBody(new Operation.Folder0FoldersVariables(List.of(folder.id())));
            var folderMetadata = CallsIf.graphqlCallSaveAndParseJsonObject(body, folderPath.resolve(folderPath.getFileName() + "-Folders0.json"));
            checkFolderMetadata(folderMeta, folderMetadata, errors);
        } catch (Throwable t) {
            errors.add(String.format("Exception calling Folder0Folders: %s; %s; %s", folder, parentFolderPath, folderPath));
            errors.add(stackTraceToString(t));
        }

        Collection<Folder> folders = null;
        try {
            var body = Operation.FOLDERS.getBody(new Operation.Folder1FoldersVariables(cfg.employeeId(), folder.id()));
            var folderFolders = CallsIf.graphqlCallSaveAndParseJsonObject(body, folderPath.resolve(folderPath.getFileName() + "-Folders1.json"));
            folders = processFolders(folderFolders, folderPath, errors);
        } catch (Throwable t) {
            errors.add(String.format("Exception calling Folder1Folders: %s; %s; %s", folder, parentFolderPath, folderPath));
            errors.add(stackTraceToString(t));
        }

        Collection<Document> documents = null;
        try {
            var body = Operation.DOCUMENTS.getBody(new Operation.FolderDocumentsVariables(cfg.employeeId(), folder.id()));
            var folderDocuments = CallsIf.graphqlCallSaveAndParseJsonObject(body, folderPath.resolve(folderPath.getFileName() + "-Documents.json"));
            // TODO
            // check document id not duplicated
            documents = processDocuments(folderDocuments, folderPath, errors);
        } catch (Throwable t) {
            errors.add(String.format("Exception calling FolderDocuments: %s; %s; %s", folder, parentFolderPath, folderPath));
            errors.add(stackTraceToString(t));
        }

        checkFolderCounters(folder, folders, documents, errors);

        logErrors(errors, folderPath);
        return folder;
    }

    private static void checkFolderMetadata(JsonObject folderMeta, JsonObject folderMetadata, List<String> errors) {
        var folders = folderMetadata.getAsJsonObject("data")
                .getAsJsonObject("documents")
                .getAsJsonArray("folders");

        if (folders.size() != 1) {
            throw new IllegalStateException(String.format("Folder metadata not 1: %s", folderMetadata));
        }

        var folder = folders.get(0);

        if (!folderMeta.equals(folder)) {
            errors.add(String.format("Folder metadata differ: %s; %s", folderMeta, folder));
        }

        var fields = new HashSet<>(folderMeta.keySet());
        fields.removeAll(Folder.KNOWN_FIELDS);

        if (fields.size() != 0) {
            errors.add("New unknown folder fields: " + String.join(", ", fields));
        }
    }

    private static void checkFolderCounters(Folder folder, Collection<Folder> folders, Collection<Document> documents, LinkedList<String> errors) {

        if (folders == null) {
            errors.add("checkFolderCounters: folders is null: " + folder);
        } else if (folder.counterSubfolders() != folders.size()) {
            errors.add(String.format("checkFolderCounters: folder count differ: %s; %s", folder, folders.size()));
        }

        if (documents == null) {
            errors.add("checkFolderCounters: documents is null: " + folder);
        } else if (folder.counterDocuments() != documents.size()) {
            errors.add(String.format("checkFolderCounters: documents count differ: %s; %s", folder, documents.size()));
        }

    }

    private static Collection<Document> processDocuments(JsonObject documents, Path parentFolderPath, List<String> errors) {
        var returnDocuments = new LinkedList<Document>();
        try {
            documents.getAsJsonObject("data")
                    .getAsJsonObject("documents")
                    .getAsJsonArray("documents")
                    .forEach(d -> processDocument(d.getAsJsonObject(), parentFolderPath, errors).ifPresent(returnDocuments::add));
        } catch (Throwable t) {
            errors.add(String.format("Exception processDocuments: %s; %s", documents, parentFolderPath));
            errors.add(stackTraceToString(t));
        }

        return returnDocuments;
    }

    private static Optional<Document> processDocument(JsonObject document, Path parentFolderPath, List<String> errors) {
        try {
            return Optional.of(processDocument(document, parentFolderPath));
        } catch (Throwable t) {
            errors.add(String.format("Exception processDocument: %s; %s", document, parentFolderPath));
            errors.add(stackTraceToString(t));
        }
        return Optional.empty();
    }

    private static Document processDocument(JsonObject documentMeta, Path parentFolderPath) {
        var document = gson.fromJson(documentMeta, Document.class);
        var documentPath = parentFolderPath.resolve(document.id() + "-" + document.filename());

        var errors = new LinkedList<String>();

        try {
            var documentMetadata = CallsIf.documentDownloadAndMetadata(document, documentPath, errors);
            // TODO
            // check document id not duplicated
            checkDocumentMetadata(document, documentMeta, documentMetadata, errors);
        } catch (Throwable t) {
            errors.add("Exception while downloading document: " + document + "; Save path:" + documentPath);
            errors.add(stackTraceToString(t));
        }

        logErrors(errors, documentPath);
        return document;
    }

    private static void checkDocumentMetadata(Document document, JsonObject documentMeta, JsonObject documentMetadata, List<String> errors) {
        for (String[] k : documentMetadataKeys) {
            var ak = k[0];
            var bk = k.length == 2 ? k[1] : k[0];
            var av = documentMeta.getAsJsonPrimitive(ak);
            var bv = documentMetadata.getAsJsonPrimitive(bk);

            if (!Objects.equals(av, bv)) {
                errors.add(String.format("Document metadata key differ; %s: %s; %s: %s", ak, av, bk, bv));
            }
        }

        if (!String.format("/documents/%s?download=true", document.id()).equals(document.file())) {
            errors.add("Document file field not as expected: " + document.file());
        }

        if (!String.format("/documents/%s", document.id()).equals(document.fileUrl())) {
            errors.add("Document file fileUrl not as expected: " + document.fileUrl());
        }

        var fields = new HashSet<>(documentMeta.keySet());
        fields.removeAll(Document.KNOWN_FIELDS);

        if (fields.size() != 0) {
            errors.add("New unknown document fields: " + String.join(", ", fields));
        }
    }

    public static Path getResourcePath(String name) throws URISyntaxException {
        InputStream resourceAsStream = classLoader.getResourceAsStream(name);
        System.out.println(resourceAsStream);
        try {
            System.out.println(new String(resourceAsStream.readAllBytes()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Path.of(classLoader.getResource(name).toURI());
    }

    private static Configuration getArgsConfigurationOrDefault(String[] args) {
        if (args.length == 1) {

            if (!Files.exists(Path.of(args[0]))) {
                throw new IllegalArgumentException("Configuration file does not exists: " + args[0]);
            }

            return Configuration.loadConfiguration(args[0]);
        }

        return Configuration.loadConfiguration();
    }

    private static String stackTraceToString(Throwable t) {
        var stackTrace = new StringWriter();
        t.printStackTrace(new PrintWriter(stackTrace));
        return stackTrace.toString();
    }

    private static void logErrors(List<String> errors, Path path) {
        try {
            logErrorsWrapped(errors, path);
        } catch (Throwable t) {
            globalErrors.addAll(errors);
            globalErrors.add("Exception logging");
            globalErrors.add(stackTraceToString(t));
            globalErrors.add("\n");
        }
    }

    private static void logErrorsWrapped(List<String> errors, Path path) {
        if (errors.isEmpty()) {
            System.out.println("OK:    " + path);
            return;
        }

        System.out.println("ERROR: " + path);

        var errorPath = (Files.isDirectory(path) ? path : path.getParent()).resolve(path.getFileName() + "_error.log");
        errorFiles.add(errorPath.toString());

        if (!Files.exists(errorPath)) {
            try {
                Files.writeString(errorPath, String.join("\n", errors));
                return;
            } catch (IOException e) {
                errors.add(String.format("Exception writing log file: %s; %s", errorPath, path));
                throw new IllegalStateException(e);
            }
        }

        errors.add("Log file already exits: " + errorPath);
        throw new IllegalStateException("Log file already exits: " + errorPath);
    }
}
