package ua.kiev.prog.photopond.drive.directories;

import ua.kiev.prog.photopond.user.UserInfo;

public class DirectoryBuilder {
    private Directory directory = new Directory();

    public DirectoryBuilder id(Long id) {
        directory.setId(id);
        return this;
    }

    public DirectoryBuilder owner(UserInfo owner) {
        directory.setOwner(owner);
        return this;
    }

    public DirectoryBuilder path(String name) {
        directory.setPath(name);
        return this;
    }

    public DirectoryBuilder from(Directory source) {
        directory.setId(source.getId());
        directory.setOwner(source.getOwner());
        directory.setPath(source.getPath());
        return this;
    }

    public Directory build() {
        return directory;
    }
}
