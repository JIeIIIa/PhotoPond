package ua.kiev.prog.photopond.core;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class BindingErrorDTOTest {

    @Test
    void equals() {
        EqualsVerifier.forClass(BindingErrorDTO.class).usingGetClass().verify();
    }
}