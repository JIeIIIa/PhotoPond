package ua.kiev.prog.photopond.drive.pictures;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static ua.kiev.prog.photopond.drive.directories.Directory.SEPARATOR;
import static ua.kiev.prog.photopond.drive.pictures.PictureFile.isFilenameCorrect;

@RunWith(Parameterized.class)
public class PictureFileIsFilenameCorrectTest {

    private String filename;
    private boolean expectedValue;

    public PictureFileIsFilenameCorrectTest(String filename, boolean expectedValue) {
        this.filename = filename;
        this.expectedValue = expectedValue;
    }

    @Parameters(name = "{index}: filename == {0}; result == {1}")
    public static Collection<Object[]> data() {
        Object[][] data = new Object[][] {
                {null, false},
                {"", false},
                {SEPARATOR + "fileName", false},
                { "fileName"+SEPARATOR, false},
                {"file" + SEPARATOR + "Name", false},
                {"correctFileName.jpg", true}
        };
        return Arrays.asList(data);
    }

    @Test
    public void isFilenameCorrectTest() {
        boolean value = isFilenameCorrect(filename);

        assertThat(value).isEqualTo(expectedValue);
    }
}
