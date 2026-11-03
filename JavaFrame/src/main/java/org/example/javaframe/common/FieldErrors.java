package org.example.javaframe.common;

import java.util.List;

public record FieldErrors(List<FieldErrorItem> fields) {

    public record FieldErrorItem(String field, String message) {
    }
}