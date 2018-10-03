package ua.kiev.prog.photopond.twitter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import twitter4j.auth.RequestToken;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class TwitterRequestTokenStorage {
    private Map<String, String> map = new LinkedHashMap<>();

    @Value("${twitter.request-token-storage.max-size:100}")
    private int maxSize;

    void setMaxSize(int maxSize) {
        if (maxSize < 1) {
            throw new IllegalArgumentException("maxSize must be greater than 0: maxSize = " + maxSize);
        }
        this.maxSize = maxSize;
    }

    public synchronized void add(String token, String tokenSecret) {
        removeOldEntities();
        map.put(token, tokenSecret);
    }

    public synchronized void add(RequestToken requestToken) {
        add(requestToken.getToken(), requestToken.getTokenSecret());
    }

    private void removeOldEntities() {
        while (map.size() > 0 && map.size() >= maxSize) {
            map.entrySet().remove(map.entrySet().iterator().next());
        }
    }

    synchronized Optional<RequestToken> retrieveAndRemoveRequestToken(String token) {
        if (map.containsKey(token)) {
            String tokenSecret = map.get(token);
            map.remove(token);
            return Optional.of(new RequestToken(token, tokenSecret));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public String toString() {
        return "TwitterRequestTokenStorage{" +
                "maxSize=" + maxSize +
                ", map.keySet=" + map.keySet() +
                '}';
    }
}
