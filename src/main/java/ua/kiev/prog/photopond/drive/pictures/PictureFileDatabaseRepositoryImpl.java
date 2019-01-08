package ua.kiev.prog.photopond.drive.pictures;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;
import ua.kiev.prog.photopond.annotation.profile.DatabaseStorage;
import ua.kiev.prog.photopond.drive.directories.Directory;
import ua.kiev.prog.photopond.drive.exception.PictureFileException;

import javax.persistence.TransactionRequiredException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@SuppressWarnings("Duplicates")
@Repository
@DatabaseStorage
public class PictureFileDatabaseRepositoryImpl implements PictureFileRepository {

    private static final Logger LOG = LogManager.getLogger(PictureFileDatabaseRepositoryImpl.class);

    private final PictureFileJpaRepository pictureFileJpaRepository;

    private final PictureFileDataJpaRepository pictureFileDataJpaRepository;

    @Autowired
    public PictureFileDatabaseRepositoryImpl(PictureFileJpaRepository pictureFileJpaRepository, PictureFileDataJpaRepository pictureFileDataJpaRepository) {
        LOG.info("Create instance of {} with parameter {}", PictureFileDatabaseRepositoryImpl.class, pictureFileJpaRepository);
        this.pictureFileJpaRepository = pictureFileJpaRepository;
        this.pictureFileDataJpaRepository = pictureFileDataJpaRepository;
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
    }

    private void deleteFromDatabase(PictureFile file) {
        try {
            pictureFileJpaRepository.delete(file);

        } catch (TransactionRequiredException | DataAccessException | IllegalArgumentException e) {
            LOG.warn("Failure deleting from jpa repository for '{}'", file);
            throw new PictureFileException("Failure deleting from jpa repository: " + file, e);
        }
    }

    @Override
    public void move(PictureFile file, Directory targetDirectory, String targetFilename) throws PictureFileException {
        throwExceptionIfNull(file);

        if (Objects.equals(file.getDirectory(), targetDirectory) && Objects.equals(file.getFilename(), targetFilename)) {
            return;
        }
        isPossibleToMove(file, targetDirectory, targetFilename);

        try {
            file.setDirectory(targetDirectory);
            file.setFilename(targetFilename);
            pictureFileJpaRepository.save(file);
        } catch (IllegalArgumentException e) {
            LOG.warn("Exception while moving PictureFile: '{}' -> '{}' + '{}'", file, targetDirectory, targetFilename);
            throw new PictureFileException("Exception while moving PictureFile '" + file +
                    "' to '" + targetDirectory + "' + '" + targetFilename + "'", e);
        }
    }

    @Override
    public long pictureSize(PictureFile pictureFile) {
        return pictureFileDataJpaRepository.findByPictureFile(pictureFile)
                .map(PictureFileData::getSize)
                .orElse(0L);
    }

    private void throwExceptionIfNull(PictureFile file) throws IllegalArgumentException {
        if (file == null) {
            LOG.debug("PictureFile:   NULL");
            throw new IllegalArgumentException("pictureFile is null");
        }
    }

    private void isPossibleToMove(PictureFile file, Directory targetDirectory, String targetFilename) {
        if (!Objects.equals(file.getDirectory().getOwner(), targetDirectory.getOwner())) {
            LOG.error("Cannot move file from different users: {} -> {}",
                    file.getDirectory().getOwner().getLogin(),
                    targetDirectory.getOwner().getLogin()
            );
            throw new PictureFileException("Cannot move file from different users: " +
                    file.getDirectory().getOwner().getLogin() + " -> " +
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

        byte[] data = pictureFileDataJpaRepository.findByPictureFile(file)
                .map(PictureFileData::getData)
                .orElseGet(() -> new byte[0]);

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
        PictureFileData pictureFileData = new PictureFileData(file, file.getData());
        pictureFileDataJpaRepository.save(pictureFileData);
    }
}
