package ua.kiev.prog.photopond.drive;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.i18n.LocaleContextHolder;
import ua.kiev.prog.photopond.Utils.TestDataStreams;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class DriveItemDTOBuilderTest implements TestDataStreams {

    @MockBean
    private LocaleContextHolder localeContextHolder;

    @ParameterizedTest(name = "[{index}] ==> name = {0}")
    @MethodSource(value = {"strings"})
    void name(String name) {
        //When
        DriveItemDTO driveItemDTO = DriveItemDTOBuilder.getInstance().name(name).build();

        //Then
        assertThat(driveItemDTO.getName())
                .isNotNull()
                .isEqualTo(name);
    }

    @ParameterizedTest(name = "[{index}] ==> parentUri = {0}")
    @MethodSource(value = {"strings"})
    void parentUri(String parentUri) {
        //When
        DriveItemDTO driveItemDTO = DriveItemDTOBuilder.getInstance().parentUri(parentUri).build();

        //Then
        assertThat(driveItemDTO.getParentUri())
                .isNotNull()
                .isEqualTo(parentUri);
    }

    static Stream<DriveItemType> driveItemTypeStream() {
        return Stream.of(DriveItemType.DIR, DriveItemType.FILE);
    }


    @ParameterizedTest(name = "[{index}] ==> type = {0}")
    @MethodSource(value = {"driveItemTypeStream"})
    void type(DriveItemType type) {
        //When
        DriveItemDTO driveItemDTO = DriveItemDTOBuilder.getInstance().type(type).build();

        //Then
        assertThat(driveItemDTO.getType())
                .isNotNull()
                .isEqualTo(type);
    }

    @Test
    void creationDate() {
        //Given
        final Date date = new Date();

        //When
        DriveItemDTO driveItemDTO = DriveItemDTOBuilder.getInstance().creationDate(date).build();

        //Then
        assertThat(driveItemDTO.getCreationDate())
                .isNotNull()
                .isEqualTo(date);
    }

    static Stream<Arguments> creationDateData() {
        return Stream.of(
                Arguments.of(new GregorianCalendar(2019, Calendar.FEBRUARY, 14, 1, 52, 2).getTime(),
                        new Locale("en", "EN"),
                        "2/14/19 1:52:02 AM"),
                Arguments.of(new GregorianCalendar(2019, Calendar.FEBRUARY, 14, 14, 52, 35).getTime(),
                        new Locale("en", "EN"),
                        "2/14/19 2:52:35 PM"),
                Arguments.of(new GregorianCalendar(2019, Calendar.FEBRUARY, 14, 13, 52, 47).getTime(),
                        new Locale("ru", "RU"),
                        "14.02.19 13:52:47"),
                Arguments.of(new GregorianCalendar(2019, Calendar.FEBRUARY, 14, 21, 12, 59).getTime(),
                        new Locale("uk", "UK"),
                        "14.02.19 21:12:59")
        );
    }

    @ParameterizedTest(name = "[{index}] ==> locale = {1}, date = {2}")
    @MethodSource(value = {"creationDateData"})
    @Disabled(value = "enable after localization will be changed")
    //see: https://bugs.openjdk.java.net/browse/JDK-8218903
    void creationDateString(Date date, Locale locale, String dateAsString) {
        //Given
        LocaleContextHolder.setLocale(locale);

        //When
        DriveItemDTO driveItemDTO = DriveItemDTOBuilder.getInstance().creationDate(date).build();

        //Then
        assertThat(driveItemDTO.getCreationDateString())
                .isNotNull()
                .isEqualTo(dateAsString);
    }

    @Test
    void defaultBuild() {
        //When
        DriveItemDTO driveItemDTO = DriveItemDTOBuilder.getInstance().build();

        //Then
        assertThat(driveItemDTO.getName()).isNull();
        assertThat(driveItemDTO.getParentUri()).isNull();
        assertThat(driveItemDTO.getType()).isNull();
        assertThat(driveItemDTO.getCreationDate()).isNull();
        assertThat(driveItemDTO.getCreationDateString()).isEmpty();
    }
}