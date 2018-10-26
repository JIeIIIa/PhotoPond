package ua.kiev.prog.photopond.drive;

import org.apache.commons.text.CharacterPredicates;
import org.apache.commons.text.RandomStringGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import ua.kiev.prog.photopond.drive.directories.Directory;
import ua.kiev.prog.photopond.user.UserInfoDTO;
import ua.kiev.prog.photopond.user.UserInfoService;

@Configuration
@Order
public class DriveFillerStartupRunner implements CommandLineRunner {
    private static final Logger LOG = LogManager.getLogger(DriveFillerStartupRunner.class);

    private static final char[][] CHAR_PAIRS = {{'a', 'z'}, {'A', 'Z'}, {'0', '9'}};

    private final UserInfoService userInfoService;

    private final DriveService driveService;

    private boolean enableDirectoryGenerator;
    private int directoryNameMinLength;
    private int directoryNameMaxLength;
    private int directoriesCount;

    private RandomStringGenerator randomStringGenerator;

    @Autowired
    public DriveFillerStartupRunner(UserInfoService userInfoService, DriveService driveService) {
        LOG.info("Create instance of {}", DriveFillerStartupRunner.class);
        this.userInfoService = userInfoService;
        this.driveService = driveService;

        randomStringGenerator = new RandomStringGenerator.Builder()
                .withinRange(CHAR_PAIRS)
                .filteredBy(CharacterPredicates.LETTERS, CharacterPredicates.DIGITS)
                .build();
    }

    @Value("${photopond.directory-generator.enable:false}")
    public void setEnableDirectoryGenerator(boolean enableDirectoryGenerator) {
        this.enableDirectoryGenerator = enableDirectoryGenerator;
    }

    @Value("${ephotopond.directory-generator.name.length.min:3}")
    public void setDirectoryNameMinLength(int directoryNameMinLength) {
        this.directoryNameMinLength = directoryNameMinLength;
    }

    @Value("${photopond.directory-generator.name.length.max:40}")
    public void setDirectoryNameMaxLength(int directoryNameMaxLength) {
        this.directoryNameMaxLength = directoryNameMaxLength;
    }

    @Value("${photopond.directory-generator.count:14}")
    public void setDirectoriesCount(int directoriesCount) {
        this.directoriesCount = directoriesCount;
    }

    @Override
    public void run(String... args) {
        if (enableDirectoryGenerator) {
            LOG.debug("Start creating directories with random name for all users");
            userInfoService.findAllUsers().forEach(this::createDirectories);
            LOG.debug("Directories have been successfully created");
        }
    }

    private void createDirectories(UserInfoDTO userInfoDTO) {
        for (int i = 0; i < directoriesCount; i++) {
            String directoryName = randomStringGenerator.generate(directoryNameMinLength, directoryNameMaxLength);
            driveService.addDirectory(userInfoDTO.getLogin(), Directory.SEPARATOR, directoryName);
        }
    }
}
