package ua.kiev.prog.photopond.drive.pictures;

import ua.kiev.prog.photopond.drive.directories.Directory;

public class PictureFileBuilder {
    private Long id;
    private String filename;
    private Directory directory;
    private byte[] data;

    private PictureFileBuilder() {
    }

    static public PictureFileBuilder getInstance() {
        return new PictureFileBuilder();
    }

    public PictureFileBuilder id(Long id) {
        this.id = id;
        return this;
    }

    public PictureFileBuilder filename(String filename) {
        this.filename = filename;
        return this;
    }

    public PictureFileBuilder directory(Directory directory) {
        this.directory = directory;
        return this;
    }

    public PictureFileBuilder data(byte[] data) {
        this.data = data;
        return this;
    }

    public PictureFileBuilder from(PictureFile pictureFile) {
        id = pictureFile.getId();
        directory = pictureFile.getDirectory();
        filename = pictureFile.getFilename();
        data = pictureFile.getData();

        return this;
    }

    public PictureFile build() {
        PictureFile file = new PictureFile();
        file.setId(id);
        file.setFilename(filename);
        file.setDirectory(directory);
        if (data == null) {
            file.setData(new byte[0]);
        } else {
            file.setData(data);
        }

        return file;
    }

}
