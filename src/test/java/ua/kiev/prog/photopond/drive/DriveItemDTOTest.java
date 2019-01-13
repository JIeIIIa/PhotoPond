package ua.kiev.prog.photopond.drive;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

class DriveItemDTOTest {

    @Test
    void equals() {
        EqualsVerifier.forClass(DriveItemDTO.class)
                .usingGetClass()
                .withIgnoredFields("creationDate", "creationDateString")
                .suppress(Warning.NONFINAL_FIELDS)
                .verify();
    }
}