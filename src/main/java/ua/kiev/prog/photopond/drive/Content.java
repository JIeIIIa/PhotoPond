package ua.kiev.prog.photopond.drive;

import ua.kiev.prog.photopond.drive.directories.Directory;
import ua.kiev.prog.photopond.drive.pictures.PictureFile;

import java.util.LinkedList;
import java.util.List;

public class Content {
    private Directory currentDirectory;

    private List<Directory> topSubDirectories;

    private List<Directory> parents;

    private List<PictureFile> files;

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

    public List<PictureFile> getFiles() {
        return files;
    }

    public void setFiles(List<PictureFile> files) {
        this.files = files;
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
                ", files=" + files +
                '}';
    }

    public static class ContentBuilder {
        private Directory currentDirectory;

        private List<Directory> topSubDirectories;

        private List<Directory> parents;

        private List<PictureFile> files;

        private ContentBuilder() {

        }

        public static ContentBuilder getInstance() {
            return new ContentBuilder();
        }

        public ContentBuilder currentDirectory(Directory currentDirectory) {
            this.currentDirectory = currentDirectory;
            return this;
        }

        public ContentBuilder topSubDirectories(List<Directory> topSubDirectories) {
            this.topSubDirectories = topSubDirectories;
            return this;
        }

        public ContentBuilder parents(List<Directory> parents) {
            this.parents = parents;
            return this;
        }

        public ContentBuilder files(List<PictureFile> files) {
            this.files = files;
            return this;
        }

        public Content build() {
            Content content = new Content();
            content.setCurrentDirectory(this.currentDirectory);
            content.setTopSubDirectories(checkList(this.topSubDirectories));
            content.setParents(checkList(this.parents));
            content.setFiles(checkList(this.files));
            return content;
        }

        private <T> List<T> checkList(List<T> list) {
            if (list == null) {
                return new LinkedList<>();
            } else {
                return list;
            }
        }
    }
}
