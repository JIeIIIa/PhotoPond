package ua.kiev.prog.photopond.drive;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import ua.kiev.prog.photopond.drive.directories.DirectoryDiskAndDatabaseRepositoryImpl;
import ua.kiev.prog.photopond.drive.directories.DirectoryJpaRepository;
import ua.kiev.prog.photopond.drive.directories.DirectoryRepository;
import ua.kiev.prog.photopond.drive.pictures.PictureFileDiskAndDatabaseRepositoryImpl;
import ua.kiev.prog.photopond.drive.pictures.PictureFileJpaRepository;
import ua.kiev.prog.photopond.drive.pictures.PictureFileRepository;

@TestConfiguration
@Profile({"unitTest", "test"})
public class DriveServiceImplITConfiguration {

    private static final Logger LOG = LogManager.getLogger(DriveServiceImplITConfiguration.class);

    private String foldersBaseDir;

//    private String generatedFoldersBaseDir;

    @Value("${folders.basedir.location}")
    public void setFoldersBaseDir(String foldersBaseDir) {
        this.foldersBaseDir = foldersBaseDir;
//        this.generatedFoldersBaseDir = foldersBaseDir + "/" + ThreadLocalRandom.current().nextInt();
    }

    private DirectoryDiskAndDatabaseRepositoryImpl directoryRepository;
    private PictureFileDiskAndDatabaseRepositoryImpl fileRepository;

    @Bean
    public DirectoryRepository directoryRepository(DirectoryJpaRepository jpaRepository, String generatedFoldersBaseDir) {
        DirectoryDiskAndDatabaseRepositoryImpl instance = new DirectoryDiskAndDatabaseRepositoryImpl(jpaRepository);
        instance.setFoldersBasedir(generatedFoldersBaseDir);
        LOG.info("directoryDiskAndDatabaseRepository:    foldersBaseDir = " + generatedFoldersBaseDir);
        this.directoryRepository = instance;

        return instance;
    }

    @Bean
    public PictureFileRepository fileRepository(PictureFileJpaRepository jpaRepository, String generatedFoldersBaseDir) {
        PictureFileDiskAndDatabaseRepositoryImpl instance = new PictureFileDiskAndDatabaseRepositoryImpl(jpaRepository);
        instance.setFoldersBasedir(generatedFoldersBaseDir);
        LOG.info("pictureFileRepository:    foldersBaseDir = " + generatedFoldersBaseDir);
        this.fileRepository = instance;

        return instance;
    }

    @Bean
    public String generatedFoldersBaseDir() {
        return foldersBaseDir;// + "/" + ThreadLocalRandom.current().nextInt();
    }
}
