package com.gens.fleetapi.infra.exception;

public class ResourceNotFoundException extends RuntimeException {

    private final String resourceName;
    private final transient Object resourceId;

    public ResourceNotFoundException(String resourceName, Object resourceId) {
        super(String.format("%s not found with ID: %s", resourceName, resourceId));
        this.resourceName = resourceName;
        this.resourceId = resourceId;
    }

    public ResourceNotFoundException(String resourceName, Object resourceId, Throwable cause) {
        super(String.format("%s not found with ID: %s", resourceName, resourceId), cause);
        this.resourceName = resourceName;
        this.resourceId = resourceId;
    }

    public String getResourceName() {
        return resourceName;
    }

    public Object getResourceId() {
        return resourceId;
    }


}