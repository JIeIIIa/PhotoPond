package ua.kiev.prog.photopond.drive.pictures;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import ua.kiev.prog.photopond.drive.directories.Directory;
import ua.kiev.prog.photopond.drive.directories.DirectoryBuilder;
import ua.kiev.prog.photopond.user.UserInfo;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class PictureFileBuilderTest {

    @Test
    public void id() {
        //Given
        Long id = 777L;

        //When
        PictureFile file = PictureFileBuilder.getInstance()
                .id(id)
                .filename("filename.jpg").build();

        //Then
        assertThat(file.getId())
                .isEqualTo(id);
    }

    @Test
    public void filename() {
        //Given
        String filename = "filename.jpg";

        //When
        PictureFile file = PictureFileBuilder.getInstance()
                .filename(filename).build();

        //Then
        assertThat(file.getFilename())
                .isEqualTo(filename);
    }

    @Test
    public void directory() {
        //Given
        Directory directory = new Directory();

        //When
        PictureFile file = PictureFileBuilder.getInstance()
                .directory(directory)
                .filename("filename.jpg").build();

        //Then
        assertThat(file.getDirectory())
                .isEqualTo(directory);
    }

    @Test
    public void data() {
        //Given
        byte[] data = {1, 2, 3};

        //When
        PictureFile file = PictureFileBuilder.getInstance()
                .data(data)
                .filename("filename.jpg").build();

        //Then
        assertThat(file.getData())
                .isEqualTo(data);
    }

    @Test
    public void from() {
        //Given
        Directory directory = new DirectoryBuilder().id(1L).path("/").owner(new UserInfo()).build();
        Directory expectedDirectory = new DirectoryBuilder().from(directory).build();
        PictureFile file = new PictureFile();
        file.setId(777L);
        file.setDirectory(directory);
        file.setFilename("Test.jpg");
        file.setData(new byte[]{1, 2, 3, 4, 5, 6, 7});

        //When
        PictureFile result = PictureFileBuilder.getInstance().from(file).build();

        //Then
        assertThat(result.getId()).isEqualTo(777L);
        assertThat(result.getDirectory()).isEqualToComparingFieldByField(expectedDirectory);
        assertThat(result.getFilename()).isEqualTo("Test.jpg");
        assertThat(result.getData()).isEqualTo(new byte[]{1, 2, 3, 4, 5, 6, 7});
    }

    @Test(expected = IllegalArgumentException.class)
    public void build() {
        //When
        PictureFileBuilder.getInstance().build();
    }
}