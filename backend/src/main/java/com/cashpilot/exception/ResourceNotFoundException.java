package com.cashpilot.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException of(String entityName, Long id) {
        return new ResourceNotFoundException(entityName + " não encontrado(a) com o id: " + id);
    }

}
