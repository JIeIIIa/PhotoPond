package ua.kiev.prog.photopond;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("ua.kiev.prog.photopond")
public class ApplicationRunner {
    private static Logger log = LogManager.getLogger(ApplicationRunner.class);

    public static void main(String[] args) {
        SpringApplication.run(ApplicationRunner.class, args);
        log.info("Application is run");
    }


}
