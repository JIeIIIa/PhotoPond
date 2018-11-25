package ua.kiev.prog.photopond.drive;

import org.springframework.context.i18n.LocaleContextHolder;

import java.text.DateFormat;
import java.util.Date;
import java.util.Objects;

public class DriveItemDTO {
    private String name;
    private String parentUri;
    private DriveItemType type;
    private Date creationDate;
    private String creationDateString;

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

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;

        updateCreationDateString(creationDate);
    }

    private void updateCreationDateString(Date creationDate) {
        if(Objects.nonNull(creationDate) ){
            DateFormat dateFormat = DateFormat
                    .getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, LocaleContextHolder.getLocale());
            creationDateString = dateFormat.format(creationDate);
        } else {
            creationDateString = "";
        }
    }

    public String getCreationDateString() {
        return creationDateString;
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
                ", creationDate=" + creationDate +
                ", creationDateString=" + creationDateString +
                '}';
    }
}
