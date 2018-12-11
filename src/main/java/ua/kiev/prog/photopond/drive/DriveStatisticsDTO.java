package ua.kiev.prog.photopond.drive;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

import static org.springframework.context.i18n.LocaleContextHolder.getLocale;

public class DriveStatisticsDTO {
    private String login;

    @JsonIgnore
    private Long directoriesSize;

    private Long pictureCount;

    public DriveStatisticsDTO() {
    }

    public DriveStatisticsDTO(String login) {
        this.login = login;
    }

    public DriveStatisticsDTO(String login, Long directoriesSize) {
        this.login = login;
        this.directoriesSize = directoriesSize;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public Long getDirectoriesSize() {
        return directoriesSize;
    }

    public void setDirectoriesSize(Long directoriesSize) {
        this.directoriesSize = directoriesSize;
    }

    public Long getPictureCount() {
        return pictureCount;
    }

    public void setPictureCount(Long pictureCount) {
        this.pictureCount = pictureCount;
    }

    @JsonProperty
    public String sizeInKiloBytes() {
        return String.format(getLocale(), "%.2f", this.directoriesSize / 1024.0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DriveStatisticsDTO that = (DriveStatisticsDTO) o;
        return Objects.equals(login, that.login) &&
                Objects.equals(directoriesSize, that.directoriesSize) &&
                Objects.equals(pictureCount, that.pictureCount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(login, directoriesSize, pictureCount);
    }
}
