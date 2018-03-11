package ua.kiev.prog.photopond.drive.pictures;

import ua.kiev.prog.photopond.drive.directories.Directory;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Arrays;

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
    @ManyToOne(/*cascade = CascadeType.REMOVE */)
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
        return id == null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PictureFile that = (PictureFile) o;

        if (filename != null ? !filename.equals(that.filename) : that.filename != null) return false;
        if (directory != null ? !directory.equals(that.directory) : that.directory != null) return false;
        return Arrays.equals(data, that.data);
    }

    @Override
    public String toString() {
        return "PictureFile{" +
                "id=" + id +
                ", filename='" + filename + '\'' +
                ", directory=" + directory +
                '}';
    }

    @Override
    public int hashCode() {
        int result = filename != null ? filename.hashCode() : 0;
        result = 31 * result + (directory != null ? directory.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(data);
        return result;
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
