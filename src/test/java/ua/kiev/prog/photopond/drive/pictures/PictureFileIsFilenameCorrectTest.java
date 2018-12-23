package ua.kiev.prog.photopond.drive.pictures;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static ua.kiev.prog.photopond.drive.directories.Directory.SEPARATOR;
import static ua.kiev.prog.photopond.drive.pictures.PictureFile.isFilenameCorrect;

public class PictureFileIsFilenameCorrectTest {

    private static Stream<Arguments> dataProvider() {
        return Stream.of(
                Arguments.of(null, false),
                Arguments.of("", false),
                Arguments.of(SEPARATOR + "fileName", false),
                Arguments.of("fileName" + SEPARATOR, false),
                Arguments.of("file" + SEPARATOR + "Name", false),
                Arguments.of("correctFileName.jpg", true)
        );
    }

    @ParameterizedTest(name = "{index}: filename == {0}; result == {1}")
    @MethodSource("dataProvider")
    public void isFilenameCorrectTest(String filename, boolean expectedValue) {
        boolean value = isFilenameCorrect(filename);

        assertThat(value).isEqualTo(expectedValue);
    }
}
