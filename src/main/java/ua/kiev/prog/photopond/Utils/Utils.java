package ua.kiev.prog.photopond.Utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class Utils {

    private static final String URL_ENCODING = "utf-8";

    public static String urlDecode(String url) {
        try {
            return URLDecoder.decode(url, URL_ENCODING);
        } catch (UnsupportedEncodingException e) {
            return url;
        }
    }

    public static String urlEncode(String url) {
        try {
            return URLEncoder.encode(url, URL_ENCODING);
        } catch (UnsupportedEncodingException e) {
            return url;
        }
    }

}
