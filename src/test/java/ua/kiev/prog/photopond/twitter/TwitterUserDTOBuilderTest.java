package ua.kiev.prog.photopond.twitter;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import ua.kiev.prog.photopond.Utils.TestDataStreams;

import static org.assertj.core.api.Assertions.assertThat;

class TwitterUserDTOBuilderTest implements TestDataStreams {

    @ParameterizedTest(name = "[{index}] ==> name = {0}")
    @MethodSource(value = {"strings"})
    void name(String name) {
        //When
        TwitterUserDTO dto = TwitterUserDTOBuilder.getInstance().name(name).build();

        //Then
        assertThat(dto.getName())
                .isNotNull()
                .isEqualTo(name);
    }

    @ParameterizedTest(name = "[{index}] ==> socialId = {0}")
    @MethodSource(value = {"longs"})
    void socialId(Long socialId) {
        //When
        TwitterUserDTO dto = TwitterUserDTOBuilder.getInstance().socialId(socialId).build();

        //Then
        assertThat(dto.getSocialId())
                .isNotNull()
                .isEqualTo(socialId);
    }

    @ParameterizedTest(name = "[{index}] ==> id = {0}")
    @MethodSource(value = {"longs"})
    void id(Long id) {
        //When
        TwitterUserDTO dto = TwitterUserDTOBuilder.getInstance().id(id).build();

        //Then
        assertThat(dto.getId())
                .isNotNull()
                .isEqualTo(id);
    }
}