package ua.kiev.prog.photopond.drive.pictures;

import java.io.Serializable;

import static ua.kiev.prog.photopond.drive.directories.Directory.SEPARATOR;

public class PictureFileDTO implements Serializable {
    private String directoryPath;

    private String filename;

    public String getDirectoryPath() {
        return directoryPath;
    }

    public void setDirectoryPath(String directoryPath) {
        this.directoryPath = directoryPath;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getPath() {
        if (SEPARATOR.equals(directoryPath)) {
            return directoryPath + filename;
        } else {
            return directoryPath + SEPARATOR + filename;
        }
    }
}
