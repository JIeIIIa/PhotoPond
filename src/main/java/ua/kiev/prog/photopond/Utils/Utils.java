package ua.kiev.prog.photopond.Utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import static ua.kiev.prog.photopond.drive.directories.Directory.SEPARATOR;

public class Utils {
    private static Logger log = LogManager.getLogger(Utils.class);

    private static final String URL_ENCODING = "utf-8";

    public static String urlDecode(String url) {
        log.traceEntry("Decode   [ url = '{}' ]", url);
        try {
            return URLDecoder.decode(url, URL_ENCODING);
        } catch (UnsupportedEncodingException e) {
            log.error("Cannot decode [ url = '{}' ]", url);
            return url;
        }
    }

    public static String urlEncode(String url) {
        log.traceEntry("Encode   [ url = '{}' ]", url);
        try {
            return URLEncoder.encode(url, URL_ENCODING);
        } catch (UnsupportedEncodingException e) {
            log.error("Cannot encode [ url = '{}' ]", url);
            return url;
        }
    }

    public static String getUriTail(String uri, String login) {
        String tail = uri.replaceFirst("/user/" + login + "/((files)|(drive))", "");
        if (tail.isEmpty()) {
            tail = SEPARATOR;
        }

        return tail;
    }

    public static String getUriTail(HttpServletRequest request, String login) {
        String tail = urlDecode(request.getRequestURI());
        return getUriTail(tail, login);
    }

    public static void deleteDirectoryWithContents(Path directory) throws IOException {
        Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

}
