package ua.kiev.prog.photopond.drive.pictures;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PictureFileDataTest {

    @Test
    void changeData() {
        //Given
        PictureFileData pictureFileData = new PictureFileData();

        //When
        pictureFileData.setData(new byte[]{1, 2, 3, 4, 5, 6, 7});

        //Then
        assertThat(pictureFileData.getSize()).isEqualTo(7);
        assertThat(pictureFileData.getData()).isEqualTo(new byte[]{1, 2, 3, 4, 5, 6, 7});
    }

    @Test
    void equals() {
        EqualsVerifier.forClass(PictureFileData.class)
                .usingGetClass()
                .withIgnoredFields("size")
                .verify();
    }
}