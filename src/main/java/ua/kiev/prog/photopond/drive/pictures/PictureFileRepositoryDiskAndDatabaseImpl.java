package ua.kiev.prog.photopond.drive.pictures;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import ua.kiev.prog.photopond.drive.directories.Directory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static ua.kiev.prog.photopond.drive.directories.Directory.SEPARATOR;

@Repository
public class PictureFileRepositoryDiskAndDatabaseImpl implements PictureFileRepository {

    private static Logger log = LogManager.getLogger(PictureFileRepositoryDiskAndDatabaseImpl.class);

    @Value("${folders.basedir}")
    private String foldersBaseDir;

    private final PictureFileJpaRepository pictureFileJpaRepository;

    @Autowired
    public PictureFileRepositoryDiskAndDatabaseImpl(PictureFileJpaRepository pictureFileJpaRepository) {
        this.pictureFileJpaRepository = pictureFileJpaRepository;
    }

    public void setFoldersBasedir(String foldersBaseDir) {
        this.foldersBaseDir = foldersBaseDir;
    }

    @Override
    public PictureFile findById(long id) throws PictureFileException {
        log.traceEntry();

        PictureFile file = pictureFileJpaRepository.findById(id);
        if(file == null) {
            log.debug("File with id = {} not found.", id);
            throw new PictureFileException("File with id = " + id + " not found.");
        }
        log.trace("try to load data");
        loadData(file);

        return file;
    }

    @Override
    public List<PictureFile> findByDirectory(Directory source) throws PictureFileException {
        log.traceEntry();
        List<PictureFile> foundFiles = pictureFileJpaRepository.findByDirectory(source);
        log.trace("foundFiles.size() == {}", foundFiles.size());
        log.trace("try to load data");
        loadData(foundFiles);

        return foundFiles;
    }

    @Override
    public List<PictureFile> findByDirectoryAndFilename(Directory source, String filename) throws PictureFileException {
        log.traceEntry();
        List<PictureFile> foundFiles = pictureFileJpaRepository.findByDirectoryAndFilename(source, filename);
        log.trace("foundFiles.size() == {}", foundFiles.size());
        log.trace("try to load data");
        loadData(foundFiles);
        return foundFiles;
    }

    @Override
    public PictureFile save(PictureFile file) throws PictureFileException {
        log.traceEntry();
        if (file.isNew()) {
            List<PictureFile> files = pictureFileJpaRepository.findByDirectoryAndFilename(file.getDirectory(), file.getFilename());
            if (!files.isEmpty()) {
                log.debug("File with name '{}' already exists", file.getFullPath());
                throw new PictureFileException("File with name '" + file.getFullPath() + "' already exists");
            }
        }
        saveData(file);
        return pictureFileJpaRepository.save(file);
    }

    @Override
    public void deleteByDirectoryAndFilename(Directory directory, String filename) throws PictureFileException {
        try {
            long cnt = pictureFileJpaRepository.removeByDirectoryAndFilename(directory, filename);
            if (cnt == 0) {
                throw new PictureFileException("Failure delete file " + filename + " in " + directory);
            }
            Files.deleteIfExists(Paths.get(foldersBaseDir + directory.getFullPath()
                    + SEPARATOR + filename));
        } catch (IOException e) {
            throw new PictureFileException("Failure deleting file", e);
        }
    }

    @Override
    public void move(Directory directory, String filename, Directory targetDirectory, String targetFilename) throws PictureFileException {
        PictureFile file = pictureFileJpaRepository.findFirstByDirectoryAndFilename(directory, filename);

        try {
            Files.move(
                    Paths.get(foldersBaseDir + directory.getFullPath() + SEPARATOR + filename),
                    Paths.get(foldersBaseDir + targetDirectory.getFullPath() + SEPARATOR + targetFilename)
            );

            log.info("before: {}", file);
            file.setDirectory(targetDirectory);
            file.setFilename(targetFilename);
            pictureFileJpaRepository.saveAndFlush(file);
            log.info("after: {}", file);
        } catch (IOException e) {
            throw new PictureFileException(e);
        }
    }

    private void loadData(PictureFile file) throws PictureFileException {
        log.traceEntry();
        byte[] data = new byte[0];

        try {
            String pathOnDisk = getFilePathOnDisk(file);
            Path path = Paths.get(pathOnDisk);

            if (Files.exists(path)) {
                data = Files.readAllBytes(path);
            }
        } catch (IOException | IllegalArgumentException | IllegalStateException e) {
            log.debug("Read data from {} error.", file);
        }
        file.setData(data);
    }

    private void loadData(List<PictureFile> files) throws PictureFileException {
        log.traceEntry();
        for (PictureFile file : files) {
            loadData(file);
        }

    }

    private void saveData(PictureFile file) throws PictureFileException {
        log.traceEntry();
        try {
            String pathOnDisk = getFilePathOnDisk(file);
            Files.write(Paths.get(pathOnDisk), file.getData());
        } catch (IOException | IllegalArgumentException | IllegalStateException e) {
            throw new PictureFileException("Save data in file error.", e);
        }
    }

    private String getDirectoryPathOnDisk(PictureFile file) {
        log.traceEntry();
        if (file == null || file.getDirectory() == null) {
            throw new IllegalArgumentException("Incorrect state file: " + file);
        }
        String pathOnDisk = foldersBaseDir + file.getDirectory().getFullPath();
        if (Files.notExists(Paths.get(pathOnDisk))) {
            throw new IllegalStateException("Directory not found on disk");
        }
        return log.traceExit(pathOnDisk);
    }

    private String getFilePathOnDisk(PictureFile file) throws PictureFileException {
        log.traceEntry();
        String pathOnDisk = getDirectoryPathOnDisk(file) + SEPARATOR + file.getFilename();
        return log.traceExit(pathOnDisk);
    }
}
