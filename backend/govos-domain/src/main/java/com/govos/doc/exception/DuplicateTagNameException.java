package com.govos.doc.exception;

public class DuplicateTagNameException extends DocException {

    public DuplicateTagNameException(String name) {
        super("Document tag name already exists: " + name);
    }
}
