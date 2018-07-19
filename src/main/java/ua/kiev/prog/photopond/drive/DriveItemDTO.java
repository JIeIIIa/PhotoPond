package ua.kiev.prog.photopond.drive;

import java.util.Objects;

public class DriveItemDTO {
    private String name;
    private String parentUri;
    private DriveItemType type;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParentUri() {
        return parentUri;
    }

    public void setParentUri(String parentUri) {
        this.parentUri = parentUri;
    }

    public DriveItemType getType() {
        return type;
    }

    public void setType(DriveItemType type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DriveItemDTO that = (DriveItemDTO) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(parentUri, that.parentUri) &&
                type == that.type;
    }

    @Override
    public int hashCode() {

        return Objects.hash(name, parentUri, type);
    }

    @Override
    public String toString() {
        return "DriveItemDTO{" +
                "name='" + name + '\'' +
                ", parentUri='" + parentUri + '\'' +
                ", type=" + type +
                '}';
    }
}
