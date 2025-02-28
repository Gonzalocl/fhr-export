package org.eu.gonzalocaparros.doc_dl;

import com.google.gson.JsonObject;

public record Body(String operationName, JsonObject variables, String query) {
    public String toLog() {
        return "(" + operationName + ", " + variables + ")";
    }
}
