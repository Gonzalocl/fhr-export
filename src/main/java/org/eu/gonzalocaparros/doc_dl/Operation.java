package org.eu.gonzalocaparros.doc_dl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.List;

public enum Operation {
    FOLDERS("Folders", FoldersVariables.class),
    DOCUMENTS("Documents", DocumentsVariables.class);

    private static final String QUERIES_PATH = "queries/";
    private static final String VARIABLES_PATH = "variables/";

    private final String name;
    private final Class<? extends Variables> variablesClass;

    Operation(String name, Class<? extends Variables> variablesClass) {
        this.name = name;
        this.variablesClass = variablesClass;
    }

    private static void addCollectionToJsonObject(JsonObject jsonObject, String name, Collection collection) {
        if (collection.isEmpty()) {
            return;
        }

        var jsonArray = new JsonArray(collection.size());
        var obj = collection.stream().findAny().get();

        switch (obj) {
            case Number o -> ((Collection<Number>) collection).forEach(jsonArray::add);
            case String o -> ((Collection<String>) collection).forEach(jsonArray::add);
            case Boolean o -> ((Collection<Boolean>) collection).forEach(jsonArray::add);
            default -> throw new IllegalStateException("Unexpected value: " + obj);
        }

        jsonObject.add(name, jsonArray);
    }

    private String getName() {
        return name;
    }

    private Class<? extends Variables> getVariablesClass() {
        return variablesClass;
    }

    private String loadQuery() {
        try {
            return Files.readString(App.getResourcePath(QUERIES_PATH + getName()));
        } catch (IOException | URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    public Body getBody(Variables variables) {
        if (!getVariablesClass().isInstance(variables)) {
            throw new IllegalArgumentException();
        }

        return new Body(getName(), variables.getJsonObject(), loadQuery());
    }

    interface Variables {

        String getVariablesTemplatePath();

        default JsonObject getJsonObject() {
            BufferedReader bufferedReader;
            try {
                var resourcePath = App.getResourcePath(VARIABLES_PATH + getVariablesTemplatePath());
                bufferedReader = Files.newBufferedReader(resourcePath);
            } catch (IOException | URISyntaxException e) {
                throw new IllegalStateException(e);
            }
            var jsonObject = App.gson.fromJson(bufferedReader, JsonObject.class);

            for (Field field : getClass().getDeclaredFields()) {
                try {
                    var obj = field.get(this);

                    switch (obj) {
                        case Number o -> jsonObject.addProperty(field.getName(), o);
                        case String o -> jsonObject.addProperty(field.getName(), o);
                        case Boolean o -> jsonObject.addProperty(field.getName(), o);
                        case List o -> addCollectionToJsonObject(jsonObject, field.getName(), o);
                        default -> throw new IllegalStateException("Unexpected value: " + obj);
                    }

                } catch (IllegalAccessException e) {
                    throw new IllegalStateException(e);
                }
            }

            return jsonObject;
        }

    }

    interface FoldersVariables extends Variables {
    }

    interface DocumentsVariables extends Variables {
    }

    record RootFoldersVariables(int employeeId) implements FoldersVariables {
        @Override
        public String getVariablesTemplatePath() {
            return "RootFoldersVariables.json";
        }
    }

    record Folder0FoldersVariables(List<Integer> ids) implements FoldersVariables {
        @Override
        public String getVariablesTemplatePath() {
            return "Folder0FoldersVariables.json";
        }
    }

    record Folder1FoldersVariables(int employeeId, int parentFolderId) implements FoldersVariables {
        @Override
        public String getVariablesTemplatePath() {
            return "Folder1FoldersVariables.json";
        }
    }

    record FolderDocumentsVariables(int employeeId, int folderId) implements DocumentsVariables {
        @Override
        public String getVariablesTemplatePath() {
            return "FolderDocumentsVariables.json";
        }
    }

    record RootDocumentsVariables(int employeeId) implements DocumentsVariables {
        @Override
        public String getVariablesTemplatePath() {
            return "RootDocumentsVariables.json";
        }
    }

//    record TestVariables(int i, float f, double d, String s, boolean b,
//                         List<Integer> li, List<Float> lf, List<Double> ld, List<String> ls,
//                         List<Boolean> lb) implements Operation.Variables {
//        @Override
//        public String getVariablesTemplatePath() {
//            return "RootFoldersVariables.json";
//        }
//    }
}
