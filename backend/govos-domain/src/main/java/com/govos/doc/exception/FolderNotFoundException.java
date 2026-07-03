package com.govos.doc.exception;

import java.util.UUID;

public class FolderNotFoundException extends DocException {

    public FolderNotFoundException(UUID id) {
        super("Folder not found with id: " + id);
    }

    public FolderNotFoundException(String code) {
        super("Folder not found with code: " + code);
    }
}
