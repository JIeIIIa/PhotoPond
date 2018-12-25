package ua.kiev.prog.photopond.drive;

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

import javax.annotation.PostConstruct;
import java.util.concurrent.ThreadLocalRandom;

@TestConfiguration
@Profile({"unitTest", "test"})
public class DriveServiceImplITConfiguration {

    private String foldersBaseDir;

    private String generatedFoldersBaseDir;

    @Value("${folders.basedir.location}")
    public void setFoldersBaseDir(String foldersBaseDir) {
        this.foldersBaseDir = foldersBaseDir;
        this.generatedFoldersBaseDir = foldersBaseDir + "/" + ThreadLocalRandom.current().nextInt();
    }

    private DirectoryDiskAndDatabaseRepositoryImpl directoryRepository;
    private PictureFileDiskAndDatabaseRepositoryImpl fileRepository;

    @PostConstruct
    public void postConstruct() {
        this.directoryRepository.setFoldersBasedir(generatedFoldersBaseDir);
        this.fileRepository.setFoldersBasedir(generatedFoldersBaseDir);
    }

    @Bean
    public DirectoryRepository directoryRepository(DirectoryJpaRepository jpaRepository) {
        DirectoryDiskAndDatabaseRepositoryImpl instance = new DirectoryDiskAndDatabaseRepositoryImpl(jpaRepository);
        instance.setFoldersBasedir(generatedFoldersBaseDir);
        System.out.println("directoryDiskAndDatabaseRepository:    foldersBaseDir = " + generatedFoldersBaseDir);
        this.directoryRepository = instance;

        return instance;
    }

    @Bean
    public PictureFileRepository fileRepository(PictureFileJpaRepository jpaRepository) {
        PictureFileDiskAndDatabaseRepositoryImpl instance = new PictureFileDiskAndDatabaseRepositoryImpl(jpaRepository);
        instance.setFoldersBasedir(generatedFoldersBaseDir);
        System.out.println("pictureFileRepository:    foldersBaseDir = " + generatedFoldersBaseDir);
        this.fileRepository = instance;

        return instance;
    }

    @Bean
    public String generatedFoldersBaseDir() {
        return generatedFoldersBaseDir;
    }
}
