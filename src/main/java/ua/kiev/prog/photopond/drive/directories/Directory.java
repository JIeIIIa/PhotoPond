package ua.kiev.prog.photopond.drive.directories;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import ua.kiev.prog.photopond.user.UserInfo;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Entity
@Table(name = "directories")
public class Directory implements Serializable {

    private static final long serialVersionUID = 5004333612018429325L;

    public static final String SEPARATOR = "/";

    private static final String REGEX_MORE_THAN_ONE_SEPARATOR = ".*" + SEPARATOR + "{2}.*";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String path;

    private Integer level;

    @NotNull
    @ManyToOne()
    @OnDelete(action = OnDeleteAction.CASCADE)
    private UserInfo owner;

    public Directory() {
        path = "";
        level = 0;
    }

    public Directory(UserInfo owner) {
        this();
        setOwner(owner);
    }

    public Directory(UserInfo owner, String path) {
        this(owner);
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
        isPathCorrect(path);
        this.path = path;
        updateLevel(path);
    }

    private static void isPathCorrect(String path) {
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("Null or empty path");
        } else if (!path.startsWith(SEPARATOR)) {
            throw new IllegalArgumentException("Path must start with SEPARATOR");
        } else if (path.length() >= 2 * SEPARATOR.length() && path.endsWith(SEPARATOR)) {
            throw new IllegalArgumentException("Path cannot end with SEPARATOR");
        }
        Pattern pattern = Pattern.compile(REGEX_MORE_THAN_ONE_SEPARATOR);
        Matcher matcher = pattern.matcher(path);
        if (matcher.find()) {
            throw new IllegalArgumentException("Two or more SEPARATORS together");
        }
    }

    private void updateLevel(String path) {
        level = 0;

        if (isRoot(path)) {
            level = -1;
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
        return SEPARATOR + owner.getLogin();
    }

    public String getName() {
        return getName(this.path);
    }

    public String nameForBreadcrumb() {
        String name = getName();
        if (isRoot()) {
            name = "..";
        }
        return name;
    }

    public static String getName(String path) {
        return path.substring(path.lastIndexOf(SEPARATOR) + 1, path.length());
    }

    public String parentPath() {
        return retrieveParentPath(this.path);
    }

    public static String retrieveParentPath(String path) {
        if (path == null) {
            throw new IllegalArgumentException("Path is null");
        } else if (path.isEmpty()) {
            throw new IllegalArgumentException("Path is empty");
        }
        String parentDirectoryPath = "";

        int index = path.lastIndexOf(SEPARATOR);
        if (path.length() > 1 && index <= 0) {
            parentDirectoryPath = SEPARATOR;
        } else {
            parentDirectoryPath += path.substring(0, index);
        }

        return parentDirectoryPath;
    }

    public String getFullPath() {
        String fullPath = getOwnerFolder();
        if (!isRoot(path)) {
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

        return Arrays.asList(namesArray);
    }

    public boolean isRoot() {
        return isRoot(path);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Directory directory = (Directory) o;
        return Objects.equals(path, directory.path) &&
                Objects.equals(level, directory.level) &&
                Objects.equals(owner, directory.owner);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, level, owner);
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

    public static String buildPath(String firstSubDirectoryName, String... subDirectoryNames) {
        StringBuilder stringBuilder = new StringBuilder();
        if (firstSubDirectoryName == null || firstSubDirectoryName.isEmpty()) {
            throw new IllegalArgumentException("FirstSubDirectoryName cannot be null or empty");
        }
        if (!isRoot(firstSubDirectoryName)) {
            concatPath(stringBuilder, firstSubDirectoryName);
        }
        for (String subDirectoryName : subDirectoryNames) {
            if (subDirectoryName == null) {
                throw new IllegalArgumentException("Found null name in subDirectoryNames. Cannot build path.");
            }
            if (subDirectoryName.isEmpty()) {
                throw new IllegalArgumentException("Found empty name in subDirectoryNames. Cannot build path.");
            }
            if (!isRoot(subDirectoryName)) {
                concatPath(stringBuilder, subDirectoryName);
            }
        }
        String path = stringBuilder.toString();
        if (path.isEmpty()) {
            path = SEPARATOR;
        }
        isPathCorrect(path);
        return path;
    }

    public static String appendToPath(String path, String name) {
        if (name == null || name.isEmpty()) {
            return path;
        }
        return buildPath(path, name);
    }

    public static boolean isRoot(String path) {
        return SEPARATOR.equals(path);
    }

    private static void concatPath(StringBuilder stringBuilder, String subDirectoryName) {
        if (!subDirectoryName.startsWith(SEPARATOR) && !stringBuilder.toString().endsWith(SEPARATOR)) {
            stringBuilder.append(SEPARATOR);
        }
        stringBuilder.append(subDirectoryName);
    }
}
