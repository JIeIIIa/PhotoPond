package ua.kiev.prog.photopond.drive;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ua.kiev.prog.photopond.Utils.TestDataStreams;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DriveStatisticsDTOTest implements TestDataStreams {

    private static Stream<Arguments> sizeValues() {
        return Stream.of(
                Arguments.of(1024L, String.format("%.2f", 1.00)),
                Arguments.of(2345L, String.format("%.2f", 2.29)),
                Arguments.of(5876L, String.format("%.2f", 5.74)),
                Arguments.of(7958L, String.format("%.2f", 7.77))
        );
    }

    @DisplayName("Directories size in kilobytes")
    @ParameterizedTest(name = "[{index}] ==> size = ''{0}'' kB")
    @MethodSource(value = "sizeValues")
    void sizeInKiloBytes(Long size, String expectedValue) {
        //Given
        DriveStatisticsDTO driveStatisticsDTO = new DriveStatisticsDTO();
        driveStatisticsDTO.setDirectoriesSize(size);

        //When
        String value = driveStatisticsDTO.sizeInKiloBytes();

        //Then
        assertThat(value).isEqualTo(expectedValue);
    }



    @DisplayName("Directories size less than zero")
    @ParameterizedTest(name = "[{index}] ==> size = {0}")
    @MethodSource("negativeLongs")
    void directoriesSizeLessZero(Long size) {
        //Given
        DriveStatisticsDTO driveStatisticsDTO = new DriveStatisticsDTO();

        //When
        Executable executable = () -> driveStatisticsDTO.setDirectoriesSize(size);

        //Then
        assertThrows(IllegalArgumentException.class, executable);
    }

    @DisplayName("Pictures count less than zero")
    @ParameterizedTest(name = "[{index}] ==> size = {0}")
    @MethodSource("negativeLongs")
    void pictureCountLessZero(Long count) {
        //Given
        DriveStatisticsDTO driveStatisticsDTO = new DriveStatisticsDTO();

        //When
        Executable executable = () -> driveStatisticsDTO.setPictureCount(count);

        //Then
        assertThrows(IllegalArgumentException.class, executable);
    }
}