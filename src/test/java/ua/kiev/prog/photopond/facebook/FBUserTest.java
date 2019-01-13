package ua.kiev.prog.photopond.facebook;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class FBUserTest {
    @Test
    void equals() {
        EqualsVerifier.forClass(FBUser.class)
                .usingGetClass()
                .verify();
    }
}