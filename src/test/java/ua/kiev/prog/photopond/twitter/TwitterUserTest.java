package ua.kiev.prog.photopond.twitter;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class TwitterUserTest {

    @Test
    void equals() {
        EqualsVerifier.forClass(TwitterUser.class)
                .usingGetClass()
                .verify();
    }
}