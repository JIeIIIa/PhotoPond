package ua.kiev.prog.photopond.drive.pictures;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import ua.kiev.prog.photopond.drive.directories.Directory;
import ua.kiev.prog.photopond.user.UserInfo;

import static org.assertj.core.api.Assertions.assertThat;
import static ua.kiev.prog.photopond.drive.directories.Directory.SEPARATOR;
import static ua.kiev.prog.photopond.drive.directories.Directory.buildPath;

@RunWith(MockitoJUnitRunner.class)
public class PictureFileTest {

    private PictureFile file;

    @Before
    public void setUp() {
        file = new PictureFile();
    }

    @Test
    public void setFilenameSuccess() {
        //Given
        String filename = "someCorrectFilename.jpg";

        //When
        file.setFilename(filename);

        //Then
        assertThat(file.getFilename())
                .isEqualTo(filename);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setFilenameFailure() {
        //When
        file.setFilename(SEPARATOR + "failureFilename.jpg");
    }

    @Test(expected = IllegalStateException.class)
    public void getPathWhenDirectoryNull() {
        file.getPath();
    }

    @Test(expected = IllegalStateException.class)
    public void getFullPathWhenDirectoryNull() {
        file.getFullPath();
    }

    @Test(expected = IllegalStateException.class)
    public void getPathWhenFilenameNull() {
        //Given
        file.setDirectory(new Directory(new UserInfo(), "/path"));

        //When
        file.getPath();
    }

    @Test(expected = IllegalStateException.class)
    public void getFullPathWhenFilenameNull() {
        //Given
        file.setDirectory(new Directory(new UserInfo(), "/path"));

        //When
        file.getFullPath();
    }

    @Test
    public void getPathForRootDirectory() {
        //Given
        String filename = "filename.jpg";
        file.setDirectory(new Directory(new UserInfo(), SEPARATOR));
        file.setFilename(filename);
        String expected = SEPARATOR + filename;

        //When
        String result = file.getPath();

        //Then
        assertThat(result)
                .isEqualTo(expected);
    }

    @Test
    public void getPathForNotRootDirectory() {
        //Given
        String filename = "filename.jpg";
        String path = buildPath("first", "second", "third");
        file.setDirectory(new Directory(new UserInfo(), path));
        file.setFilename(filename);
        String expected = path + SEPARATOR + filename;

        //When
        String result = file.getPath();

        //Then
        assertThat(result)
                .isEqualTo(expected);
    }

    @Test
    public void getFullPathForRootDirectory() {
        //Given
        String filename = "filename.jpg";
        String login = "someUser";
        file.setDirectory(new Directory(new UserInfo(login, "qwerty123!"), SEPARATOR));
        file.setFilename(filename);
        String expected = SEPARATOR + login + SEPARATOR + filename;

        //When
        String result = file.getFullPath();

        //Then
        assertThat(result)
                .isEqualTo(expected);
    }

    @Test
    public void getFullPathForNotRootDirectory() {
        //Given
        String filename = "filename.jpg";
        String login = "someUser";
        String path = buildPath("first", "second", "third");
        file.setDirectory(new Directory(new UserInfo(login, "qwerty123!"), path));
        file.setFilename(filename);
        String expected = SEPARATOR + login + path + SEPARATOR + filename;

        //When
        String result = file.getFullPath();

        //Then
        assertThat(result)
                .isEqualTo(expected);
    }

    @Test
    public void newFile() {
        //Given
        PictureFile file = new PictureFile();

        //Then
        assertThat(file.isNew()).isTrue();
    }

    @Test
    public void newFileWithLongMinValueAsId() {
        //Given
        PictureFile file = new PictureFile();
        file.setId(Long.MIN_VALUE);

        //Then
        assertThat(file.isNew()).isTrue();
    }

    @Test
    public void oldFile() {
        //Given
        PictureFile file = new PictureFile();
        file.setId(123L);

        //Then
        assertThat(file.isNew()).isFalse();
    }
}