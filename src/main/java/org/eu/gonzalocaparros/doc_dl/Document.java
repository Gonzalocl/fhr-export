package org.eu.gonzalocaparros.doc_dl;

import java.util.Arrays;
import java.util.Collection;

public record Document(Integer id, String file, String filename, String fileUrl) {
    public static final Collection<String> KNOWN_FIELDS = Arrays.asList(
            "id",
            "arCreatedAt",
            "contentType",
            "deletedAt",
            "extension",
            "file",
            "filename",
            "fileSize",
            "fileUrl",
            "fromTemplate",
            "incidenceMessageId",
            "isCompanyDocument",
            "isPendingAssignment",
            "leaveId",
            "notificationStatus",
            "permissionUpdate",
            "public",
            "signatureStatus",
            "signees",
            "space",
            "status",
            "templateId",
            "type",
            "createdAt",
            "updatedAt",
            "__typename",
            "_policies",
            "company",
            "author",
            "employee",
            "folder"
    );
}
