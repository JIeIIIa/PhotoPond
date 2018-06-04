package ua.kiev.prog.photopond.Utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

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
        String tail = uri.replaceFirst("/(\\w|\\s)+/" + login + "/(\\w|\\s)+", "");
        if (tail.isEmpty()) {
            tail = SEPARATOR;
        }

        return tail;
    }

    public static String getUriTail(HttpServletRequest request, String login) {
        String tail = urlDecode(request.getRequestURI());
        return getUriTail(tail, login);
    }


}
