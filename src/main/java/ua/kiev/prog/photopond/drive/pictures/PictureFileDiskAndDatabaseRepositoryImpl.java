package ua.kiev.prog.photopond.drive.pictures;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;
import ua.kiev.prog.photopond.annotation.profile.DiskDatabaseStorage;
import ua.kiev.prog.photopond.drive.directories.Directory;

import javax.persistence.TransactionRequiredException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static ua.kiev.prog.photopond.drive.directories.Directory.SEPARATOR;

@SuppressWarnings("Duplicates")
@Repository
@DiskDatabaseStorage
public class PictureFileDiskAndDatabaseRepositoryImpl implements PictureFileRepository {

    private static final Logger LOG = LogManager.getLogger(PictureFileDiskAndDatabaseRepositoryImpl.class);

    @Value("${folders.basedir.location}")
    private String foldersBaseDir;

    private final PictureFileJpaRepository pictureFileJpaRepository;

    @Autowired
    public PictureFileDiskAndDatabaseRepositoryImpl(PictureFileJpaRepository pictureFileJpaRepository) {
        LOG.info("Create instance of {} with parameter {}", PictureFileDiskAndDatabaseRepositoryImpl.class, pictureFileJpaRepository);
        this.pictureFileJpaRepository = pictureFileJpaRepository;
    }

    public void setFoldersBasedir(String foldersBaseDir) {
        this.foldersBaseDir = foldersBaseDir;
    }

    @Override
    public Optional<PictureFile> findById(Long id) throws PictureFileException {
        LOG.traceEntry();

        Optional<PictureFile> file = pictureFileJpaRepository.findById(id);
        file.ifPresent(this::loadData);

        return file;
    }

    @Override
    public List<PictureFile> findByDirectory(Directory source) throws PictureFileException {
        LOG.traceEntry();
        List<PictureFile> foundFiles = pictureFileJpaRepository.findByDirectory(source);
        LOG.trace("foundFiles.size() == {}", foundFiles.size());
        LOG.trace("try to load data");
        loadData(foundFiles);

        return foundFiles;
    }

    @Override
    public List<PictureFile> findByDirectoryAndFilename(Directory source, String filename) throws PictureFileException {
        LOG.traceEntry();
        List<PictureFile> foundFiles = pictureFileJpaRepository.findByDirectoryAndFilename(source, filename);
        LOG.trace("foundFiles.size() == {}", foundFiles.size());
        LOG.trace("try to load data");
        loadData(foundFiles);
        return foundFiles;
    }

    @Override
    public PictureFile save(PictureFile file) throws PictureFileException {
        LOG.traceEntry();
        throwExceptionIfNull(file);

        pictureFileJpaRepository.findFirstByDirectoryAndFilename(file.getDirectory(), file.getFilename())
                .ifPresent(u -> {
                    LOG.debug("File with name '{}' already exists", file.getFullPath());
                    throw new PictureFileException("File with name '" + file.getFullPath() + "' already exists");
                });

        saveData(file);
        PictureFile result = pictureFileJpaRepository.save(file);
        result.setData(file.getData());

        return result;
    }

    @Override
    public void delete(PictureFile file) throws PictureFileException {
        throwExceptionIfNull(file);

        deleteFromDatabase(file);
        deleteFromDisk(file);
    }

    private void deleteFromDatabase(PictureFile file) {
        try {
            pictureFileJpaRepository.delete(file);

        } catch (TransactionRequiredException | DataAccessException | IllegalArgumentException e) {
            LOG.warn("Failure deleting from jpa repository for '{}'", file);
            throw new PictureFileException("Failure deleting from jpa repository: " + file, e);
        }
    }

    private void deleteFromDisk(PictureFile file) {
        try {
            FileUtils.forceDelete(new File(foldersBaseDir + file.getFullPath()));
        } catch (IOException e) {
            LOG.error("Failure deleting from disk for '{}'", file);
        }
    }

    @Override
    public void move(PictureFile file, Directory targetDirectory, String targetFilename) throws PictureFileException {
        throwExceptionIfNull(file);

        if (Objects.equals(file.getDirectory(), targetDirectory) && Objects.equals(file.getFilename(), targetFilename)) {
            return;
        }
        isPossibleToMove(file, targetDirectory, targetFilename);

        Path source = Paths.get(foldersBaseDir + file.getFullPath());
        Path target = Paths.get(foldersBaseDir + targetDirectory.getFullPath() + SEPARATOR + targetFilename);

        try {
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            file.setDirectory(targetDirectory);
            file.setFilename(targetFilename);
            pictureFileJpaRepository.save(file);
        } catch (IOException | IllegalArgumentException e) {
            LOG.warn("Exception while moving PictureFile: '{}' -> '{}' + '{}'", file, targetDirectory, targetFilename);
            throw new PictureFileException("Exception while moving PictureFile '" + file +
                    "' to '" + targetDirectory + "' + '" + targetFilename + "'", e);
        }

        try {
            Files.delete(source);
        } catch (IOException e) {
            LOG.error("Failure deleting from disk for '{}'", source);
        }
    }

    @Override
    public long pictureSize(PictureFile pictureFile) {
        Path path = Paths.get(foldersBaseDir + pictureFile.getFullPath());
        long size = 0L;
        if (!Files.exists(path)) {
            try {
                size = Files.size(path);
            } catch (IOException e) {
                LOG.warn("Cannot retrieve size for {}", pictureFile );
            }
        }
        return size;
    }

    private void throwExceptionIfNull(PictureFile file) throws IllegalArgumentException {
        if (file == null) {
            LOG.debug("PictureFile:   NULL");
            throw new IllegalArgumentException("pictureFile is null");
        }
    }

    private void isPossibleToMove(PictureFile file, Directory targetDirectory, String targetFilename) {
        if (!Files.exists(Paths.get(foldersBaseDir + file.getFullPath()))) {
            LOG.debug("PictureFile with name '{}' not found on disk", file.getFullPath());
            throw new PictureFileException("Cannot move File",
                    new IllegalAccessException("PictureFile with name '" + file.getFullPath() + "' not found on disk")
            );
        }

        if (!Objects.equals(file.getDirectory().getOwner(), targetDirectory.getOwner())) {
            LOG.error("Cannot move file from different users: {} -> {}",
                    file.getDirectory().getOwner().getLogin(),
                    targetDirectory.getOwner().getLogin()
            );
            throw new PictureFileException("Cannot move file from different users: " +
                    file.getDirectory().getOwner().getLogin()  +" -> " +
                    targetDirectory.getOwner().getLogin()
            );
        }

        pictureFileJpaRepository.findFirstByDirectoryAndFilename(targetDirectory, targetFilename)
                .ifPresent(u -> {
                    LOG.debug("File with name {} already exists in directory {}", targetFilename, targetDirectory);
                    throw new PictureFileException("Cannot move file",
                            new IllegalAccessException("File with name '" + targetFilename +
                                    "' already exists in directory '" + targetDirectory + "'")
                    );
                });

        if (!PictureFile.isFilenameCorrect(targetFilename)) {
            LOG.debug("Target filename '{}' is incorrect", targetFilename);
            throw new PictureFileException("Cannot move PictureFile",
                    new IllegalAccessException("Target filename '" + targetFilename + "' is incorrect")
            );
        }
        LOG.traceExit();
    }

    private void loadData(PictureFile file) throws PictureFileException {
        LOG.traceEntry("Try to load data for '{}'", file);
        byte[] data = new byte[0];

        try {
            String pathOnDisk = getFilePathOnDisk(file);
            Path path = Paths.get(pathOnDisk);

            if (Files.exists(path)) {
                data = Files.readAllBytes(path);
            }
        } catch (IOException | IllegalArgumentException | IllegalStateException e) {
            LOG.debug("Read data from {} error.", file);
        }
        file.setData(data);
    }

    private void loadData(List<PictureFile> files) throws PictureFileException {
        LOG.traceEntry();
        for (PictureFile file : files) {
            loadData(file);
        }

    }

    private void saveData(PictureFile file) throws PictureFileException {
        LOG.traceEntry();
        if (file.getData() == null) {
            LOG.warn("Try to save file with null data:   {}", file);
            throw new PictureFileException("Try to save file with null data:   " + file);
        }
        try {
            String pathOnDisk = getFilePathOnDisk(file);
            Files.write(Paths.get(pathOnDisk), file.getData());
        } catch (IOException | IllegalArgumentException | IllegalStateException e) {
            LOG.error("Save data in file error:  {}", file);
            throw new PictureFileException("Save data in file error:   " + file, e);
        }
    }

    private String getDirectoryPathOnDisk(PictureFile file) {
        LOG.traceEntry();
        if (file == null || file.getDirectory() == null) {
            throw new IllegalArgumentException("Incorrect state file: " + file);
        }
        String pathOnDisk = foldersBaseDir + file.getDirectory().getFullPath();
        if (Files.notExists(Paths.get(pathOnDisk))) {
            throw new IllegalStateException("Directory not found on disk");
        }
        return LOG.traceExit(pathOnDisk);
    }

    private String getFilePathOnDisk(PictureFile file) throws PictureFileException {
        LOG.traceEntry();
        String pathOnDisk = getDirectoryPathOnDisk(file) + SEPARATOR + file.getFilename();
        return LOG.traceExit(pathOnDisk);
    }
}
