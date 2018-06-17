package ua.kiev.prog.photopond.drive;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import ua.kiev.prog.photopond.drive.directories.DirectoryDiskAndDatabaseRepository;
import ua.kiev.prog.photopond.drive.directories.DirectoryDiskAndDatabaseRepositoryImpl;
import ua.kiev.prog.photopond.drive.directories.DirectoryJpaRepository;
import ua.kiev.prog.photopond.drive.pictures.PictureFileDiskAndDatabaseRepositoryImpl;
import ua.kiev.prog.photopond.drive.pictures.PictureFileJpaRepository;
import ua.kiev.prog.photopond.drive.pictures.PictureFileRepository;

@TestConfiguration
@Profile("unitTest")
public class DriveServiceImplITConfiguration {
    @Value("${folders.basedir.location}")
    private String foldersBaseDir;

    @Bean
    public DirectoryDiskAndDatabaseRepository directoryDiskAndDatabaseRepository(DirectoryJpaRepository jpaRepository) {
        DirectoryDiskAndDatabaseRepositoryImpl instance = new DirectoryDiskAndDatabaseRepositoryImpl(jpaRepository);
        instance.setFoldersBasedir(foldersBaseDir);
        System.out.println("directoryDiskAndDatabaseRepository:    foldersBaseDir = " + foldersBaseDir);

        return instance;
    }

    @Bean
    public PictureFileRepository pictureFileRepository(PictureFileJpaRepository jpaRepository) {
        PictureFileDiskAndDatabaseRepositoryImpl instance = new PictureFileDiskAndDatabaseRepositoryImpl(jpaRepository);
        instance.setFoldersBasedir(foldersBaseDir);
        System.out.println("pictureFileRepository:    foldersBaseDir = " + foldersBaseDir);

        return instance;
    }
}
