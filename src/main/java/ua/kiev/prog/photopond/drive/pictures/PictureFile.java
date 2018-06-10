package ua.kiev.prog.photopond.drive.pictures;

import ua.kiev.prog.photopond.drive.directories.Directory;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

import static ua.kiev.prog.photopond.drive.directories.Directory.SEPARATOR;

@Entity
@Table(name = "pictureFiles")
public class PictureFile implements Serializable {

    private static final long serialVersionUID = -6304466568923584573L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String filename;

    @NotNull
    @ManyToOne
    private Directory directory;

    @Transient
    private byte[] data;

    public PictureFile() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        if (!isFilenameCorrect(filename)) {
            throw new IllegalArgumentException("Incorrect filename");
        }
        this.filename = filename;
    }

    public Directory getDirectory() {
        return directory;
    }

    public void setDirectory(Directory directory) {
        this.directory = directory;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public String getFullPath() {
        if (directory == null) {
            throw new IllegalStateException("");
        }
        if (filename == null) {
            throw new IllegalStateException("");
        }
        return directory.getFullPath() + SEPARATOR + filename;
    }

    public String getPath() {
        if (directory == null) {
            throw new IllegalStateException("");
        }
        if (filename == null) {
            throw new IllegalStateException("");
        }
        if(directory.isRoot()) {
            return directory.getPath() + filename;
        } else {
            return directory.getPath() + SEPARATOR + filename;
        }
    }

    public boolean isNew() {
        return id == null || id == Long.MIN_VALUE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PictureFile that = (PictureFile) o;
        return Objects.equals(filename, that.filename) &&
                Objects.equals(directory, that.directory) &&
                Arrays.equals(data, that.data);
    }

    @Override
    public int hashCode() {

        int result = Objects.hash(filename, directory);
        result = 31 * result + Arrays.hashCode(data);
        return result;
    }

    @Override
    public String toString() {
        return "PictureFile{" +
                "id=" + id +
                ", filename='" + filename + '\'' +
                ", directory={" + directory + "}" +
                '}';
    }

    public static boolean isFilenameCorrect(String filename) {
        if (filename == null || filename.isEmpty()) {
            return false;
        } else if (filename.contains(SEPARATOR)) {
            return false;
        }
        return true;
    }


}
