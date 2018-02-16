package ua.kiev.prog.photopond.drive.directories;

import ua.kiev.prog.photopond.user.UserInfo;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Entity
@Table(name = "directories")
public class Directory implements Serializable {

    private static final long serialVersionUID = 5004333612018429325L;

    public static final String SEPARATOR = "/";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String path;

    private Integer level;

    @NotNull
    @ManyToOne(cascade = CascadeType.ALL)
    private UserInfo owner;

    public Directory() {
        level = 0;
    }

    public Directory(UserInfo owner) {
        this.owner = owner;
    }

    public Directory(UserInfo owner, String path) {
        this.owner = owner;
        setPath(path);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("Null or empty path");
        } else if (!path.startsWith(SEPARATOR)){
            throw new IllegalArgumentException("Path must start with SEPARATOR");
        } else if (path.length() >= 2*SEPARATOR.length() && path.endsWith(SEPARATOR)) {
            throw new IllegalArgumentException("Path cannot end with SEPARATOR");
        }
        Pattern pattern = Pattern.compile(".*" + SEPARATOR + "{2}.*");
        Matcher matcher = pattern.matcher(path);
        if (matcher.find()) {
            throw new IllegalArgumentException("Two or more SEPARATORS together");
        }
        this.path = path;
        updateLevel(path);
    }

    private void updateLevel(String path) {
        level = 0;
        if (path == null) {
            return;
        }
        Pattern pattern = Pattern.compile(SEPARATOR);
        Matcher matcher = pattern.matcher(path);
        while (matcher.find()) {
            level++;
        }
    }

    public Integer getLevel() {
        return level;
    }

    public UserInfo getOwner() {
        return owner;
    }

    public void setOwner(UserInfo owner) {
        if (owner == null) {
            throw new IllegalArgumentException("Owner must be not null");
        }
        this.owner = owner;
    }

    public String getOwnerFolder() {
        if (owner == null || owner.getLogin() == null || owner.getLogin().isEmpty()) {
            throw new IllegalStateException("Illegal directory owner");
        }
        String ownerFolder = SEPARATOR + owner.getLogin();
        return ownerFolder;
    }

    public String getName() {
        if (path == null) {
            throw new IllegalArgumentException("Path is null");
        }
        String directoryName = path.substring(path.lastIndexOf(SEPARATOR) + 1, path.length());

        return directoryName;
    }

    public String getParentPath() {
        if (path == null) {
            throw new IllegalArgumentException("Path is null");
        }
        String parentDirectoryPath = "";

        int index = path.lastIndexOf(SEPARATOR);
        if (path.length() > 1 && index == 0) {
            parentDirectoryPath = SEPARATOR;
        } else {
            parentDirectoryPath += path.substring(0, index);
        }

        return parentDirectoryPath;
    }

    public String getFullPath() {
        String fullPath = getOwnerFolder();
        if (!SEPARATOR.equals(path)) {
            fullPath += path;
        }
        return fullPath;
    }

    public List<String> getDirectoryNames() {
        if (owner == null) {
            throw new IllegalStateException("Null directory owner");
        }
        String path = owner.getLogin() + getPath();
        String[] namesArray = path.split(SEPARATOR);
        List<String> names = Arrays.asList(namesArray);

        return names;
    }

    public boolean isRoot() {
        return SEPARATOR.equals(path);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Directory directory = (Directory) o;

        if (!path.equals(directory.path)) return false;
        if (!level.equals(directory.level)) return false;
        return owner != null ? owner.equals(directory.owner) : directory.owner == null;
    }

    @Override
    public int hashCode() {
        int result = path.hashCode();
        result = 31 * result + level.hashCode();
        result = 31 * result + (owner != null ? owner.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Directory{" +
                "id=" + id +
                ", path='" + path + '\'' +
                ", level=" + level +
                ", owner=[" + owner.getLogin() +
                "]}";
    }
}