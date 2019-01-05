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
        setDirectoriesSize(directoriesSize);
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
        if (directoriesSize < 0) {
            throw new IllegalArgumentException("Directories size should be greater than 0. Actual value: " + directoriesSize);
        }
        this.directoriesSize = directoriesSize;
    }

    public Long getPictureCount() {
        return pictureCount;
    }

    public void setPictureCount(Long pictureCount) {
        if (pictureCount < 0) {
            throw new IllegalArgumentException("Pictures count should be greater than 0. Actual value: " + pictureCount);
        }
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
