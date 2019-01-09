package ua.kiev.prog.photopond.facebook;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

class FBUserDTOTest {

    @Test
    void equals() {
        EqualsVerifier.forClass(FBUserDTO.class)
                .usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS)
                .verify();
    }
}