package ua.kiev.prog.photopond.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static ua.kiev.prog.photopond.Utils.Utils.deleteDirectoryWithContents;

@Configuration
public class BasedirStartupRunner implements CommandLineRunner {
    private static Logger log = LogManager.getLogger(BasedirStartupRunner.class);

    @Value("${folders.basedir.location}")
    private String foldersBaseDir;

    @Value("${folders.basedir.refresh:false}")
    private boolean refreshBasedir;

    @Override
    public void run(String... strings) throws Exception {
        log.traceEntry("BasedirStartupRunner");
        if (refreshBasedir) {
            log.debug("Refresh base folder: {}", foldersBaseDir);
            refreshBaseFolder(foldersBaseDir);
        } else {
            log.debug("Refreshing base folder is not required");
        }
    }

    private void refreshBaseFolder(String foldersBaseDir) {
        log.traceEntry();
        if (Files.notExists(Paths.get(foldersBaseDir))) {
            log.info("Folder '{}' not exists", foldersBaseDir);
            return;
        }
        try {
            Path directory = Paths.get(foldersBaseDir);
            deleteDirectoryWithContents(directory);
        } catch (IOException e) {
            log.error("Failed to delete directory: {}", foldersBaseDir);
            throw new ExceptionInInitializerError("Failed refresh BaseFolder");
        }
    }


}
