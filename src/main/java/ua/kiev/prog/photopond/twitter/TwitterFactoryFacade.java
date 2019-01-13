package ua.kiev.prog.photopond.twitter;

import twitter4j.Twitter;

public interface TwitterFactoryFacade {
    Twitter getTwitterInstance();

    Twitter getTwitterInstance(TwitterUser twitterUser);
}
