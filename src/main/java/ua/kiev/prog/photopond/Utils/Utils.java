package ua.kiev.prog.photopond.Utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

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

}
