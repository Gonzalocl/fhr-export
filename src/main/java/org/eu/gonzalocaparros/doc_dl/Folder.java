package org.eu.gonzalocaparros.doc_dl;

import java.util.Arrays;
import java.util.Collection;

public record Folder(Integer id, Integer counterDocuments, Integer counterSubfolders, String name) {
    public static final Collection<String> KNOWN_FIELDS = Arrays.asList(
            "id",
            "active",
            "counterDocuments",
            "counterSubfolders",
            "identifier",
            "isCompany",
            "isEmployee",
            "isExpense",
            "isReadonly",
            "managePermission",
            "name",
            "showOnly",
            "slug",
            "space",
            "type",
            "url",
            "__typename",
            "parentFolder",
            "rootParentFolder"
    );
}
