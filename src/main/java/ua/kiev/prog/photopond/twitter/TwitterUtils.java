package ua.kiev.prog.photopond.twitter;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;

import static ua.kiev.prog.photopond.twitter.TwitterConstants.getConsumerKey;
import static ua.kiev.prog.photopond.twitter.TwitterConstants.getConsumerSecret;

public class TwitterUtils {
    static Twitter getTwitterInstance() {
        ConfigurationBuilder configurationBuilder = baseConfigurationBuilder();

        return new TwitterFactory(configurationBuilder.build()).getInstance();
    }

    static Twitter getTwitterInstance(AccessToken accessToken) {
        ConfigurationBuilder configurationBuilder = baseConfigurationBuilder();
        configurationBuilder.setOAuthAccessToken(accessToken.getToken());
        configurationBuilder.setOAuthAccessTokenSecret(accessToken.getTokenSecret());

        return new TwitterFactory(configurationBuilder.build()).getInstance();
    }

    static Twitter getTwitterInstance(TwitterUser twitterUser) {
        AccessToken accessToken = new AccessToken(twitterUser.getToken(), twitterUser.getTokenSecret());

        return getTwitterInstance(accessToken);
    }

    private static ConfigurationBuilder baseConfigurationBuilder() {
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
        configurationBuilder.setApplicationOnlyAuthEnabled(false);
        configurationBuilder.setOAuthConsumerKey(getConsumerKey());
        configurationBuilder.setOAuthConsumerSecret(getConsumerSecret());
        return configurationBuilder;
    }
}
