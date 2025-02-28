package org.eu.gonzalocaparros.doc_dl;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public record Configuration(String baseUrl, String sessionCookie, int employeeId, String rootOutputFile, String errorOutputFile) {
    private static final String CONFIGURATION_DEFAULT_FILE = "configuration.json";

    public static Configuration loadConfiguration() {
        return loadConfiguration(CONFIGURATION_DEFAULT_FILE);
    }

    public static Configuration loadConfiguration(String configurationFile) {

        final BufferedReader bufferedReader;
        try {
            bufferedReader = Files.newBufferedReader(Path.of(configurationFile));
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }

        return App.gson.fromJson(bufferedReader, Configuration.class);
    }
}
