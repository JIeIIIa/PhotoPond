package ua.kiev.prog.photopond.core;

import java.util.Objects;

public class BindingErrorDTO {
    private final String objectName;

    private final String message;


    public BindingErrorDTO(String objectName, String message) {
        this.objectName = objectName;
        this.message = message;
    }

    public String getObjectName() {
        return objectName;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BindingErrorDTO that = (BindingErrorDTO) o;
        return Objects.equals(objectName, that.objectName) &&
                Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(objectName, message);
    }

    @Override
    public String toString() {
        return "BindingErrorDTO{" +
                "objectName='" + objectName + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
