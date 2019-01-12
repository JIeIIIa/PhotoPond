package ua.kiev.prog.photopond.twitter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;

import static ua.kiev.prog.photopond.twitter.TwitterConstants.getConsumerKey;
import static ua.kiev.prog.photopond.twitter.TwitterConstants.getConsumerSecret;

@Component
class TwitterFactoryFacadeImpl implements TwitterFactoryFacade {

    private static final Logger LOG = LogManager.getLogger(TwitterFactoryFacadeImpl.class);

    public TwitterFactoryFacadeImpl() {
        LOG.info("Create instance of {}", TwitterFactoryFacadeImpl.class);
    }

    public Twitter getTwitterInstance() {
        LOG.trace("Create Twitter instance with default settings");
        ConfigurationBuilder configurationBuilder = baseConfigurationBuilder();

        return new TwitterFactory(configurationBuilder.build()).getInstance();
    }

    public Twitter getTwitterInstance(TwitterUser twitterUser) {
        LOG.trace("Create Twitter instance for user: {}", twitterUser);
        AccessToken accessToken = new AccessToken(twitterUser.getToken(), twitterUser.getTokenSecret());

        return getTwitterInstance(accessToken);
    }

    private Twitter getTwitterInstance(AccessToken accessToken) {
        LOG.traceEntry("accessToken.userId = {}", accessToken.getUserId());
        ConfigurationBuilder configurationBuilder = baseConfigurationBuilder();
        configurationBuilder.setOAuthAccessToken(accessToken.getToken());
        configurationBuilder.setOAuthAccessTokenSecret(accessToken.getTokenSecret());

        return new TwitterFactory(configurationBuilder.build()).getInstance();
    }

    private ConfigurationBuilder baseConfigurationBuilder() {
        LOG.trace("Create base configuration");
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
        configurationBuilder.setApplicationOnlyAuthEnabled(false);
        configurationBuilder.setOAuthConsumerKey(getConsumerKey());
        configurationBuilder.setOAuthConsumerSecret(getConsumerSecret());
        return configurationBuilder;
    }
}
