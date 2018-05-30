package ua.kiev.prog.photopond.drive.directories;

import ua.kiev.prog.photopond.user.UserInfo;

public class DirectoryBuilder {
    private Long id = Long.MIN_VALUE;

    private UserInfo owner = new UserInfo();

    private String path = Directory.SEPARATOR;

    public DirectoryBuilder id(Long id) {
        this.id = id;
        return this;
    }

    public DirectoryBuilder owner(UserInfo owner) {
        this.owner = owner;
        return this;
    }

    public DirectoryBuilder path(String path) {
        this.path = path;
        return this;
    }

    public DirectoryBuilder from(Directory source) {
        id = source.getId();
        owner = source.getOwner();
        path = source.getPath();
        return this;
    }

    public Directory build() {
        Directory directory = new Directory();
        directory.setId(id);
        directory.setOwner(owner);
        directory.setPath(path);

        return directory;
    }
}
