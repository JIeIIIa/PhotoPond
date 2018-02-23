package ua.kiev.prog.photopond.drive;

import ua.kiev.prog.photopond.drive.directories.Directory;

import java.util.List;

public class Content {
    private Directory currentDirectory;

    private List<Directory> topSubDirectories;

    private List<Directory> parents;

    public Directory getCurrentDirectory() {
        return currentDirectory;
    }

    public void setCurrentDirectory(Directory currentDirectory) {
        this.currentDirectory = currentDirectory;
    }

    public List<Directory> getTopSubDirectories() {
        return topSubDirectories;
    }

    public void setTopSubDirectories(List<Directory> topSubDirectories) {
        this.topSubDirectories = topSubDirectories;
    }

    public List<Directory> getParents() {
        return parents;
    }

    public void setParents(List<Directory> parents) {
        this.parents = parents;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Content content = (Content) o;

        if (currentDirectory != null ? !currentDirectory.equals(content.currentDirectory) : content.currentDirectory != null) return false;
        if (topSubDirectories != null ? !topSubDirectories.equals(content.topSubDirectories) : content.topSubDirectories != null) return false;
        return parents != null ? parents.equals(content.parents) : content.parents == null;
    }

    @Override
    public int hashCode() {
        int result = currentDirectory != null ? currentDirectory.hashCode() : 0;
        result = 31 * result + (topSubDirectories != null ? topSubDirectories.hashCode() : 0);
        result = 31 * result + (parents != null ? parents.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Content{" +
                "currentDirectory=" + currentDirectory +
                ", topSubDirectories=" + topSubDirectories +
                ", parents=" + parents +
                '}';
    }
}
