package ua.kiev.prog.photopond.facebook;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import ua.kiev.prog.photopond.Utils.TestDataStreams;

import static org.assertj.core.api.Assertions.assertThat;

class FBUserDTOBuilderTest implements TestDataStreams {


    @DisplayName("Only email is set")
    @ParameterizedTest(name = "[{index}] ==> fbId = ''{0}''")
    @MethodSource({"nullable", "strings"})
    void email(String email) {
        //When
        FBUserDTO fbUserDTO = FBUserDTOBuilder.getInstance().email(email).build();

        //Then
        assertThat(fbUserDTO.getEmail()).isEqualTo(email);
        assertThat(fbUserDTO).isEqualToIgnoringGivenFields(new FBUserDTO(), "email");
    }

    @DisplayName("Only fbId is set")
    @ParameterizedTest(name = "[{index}] ==> fbId = ''{0}''")
    @MethodSource({"nullable", "strings"})
    void fbId(String fbId) {
        //When
        FBUserDTO fbUserDTO = FBUserDTOBuilder.getInstance().fbId(fbId).build();

        //Then
        assertThat(fbUserDTO.getFbId()).isEqualTo(fbId);
        assertThat(fbUserDTO).isEqualToIgnoringGivenFields(new FBUserDTO(), "fbId");
    }

    @DisplayName("Only name is set")
    @ParameterizedTest(name = "[{index}] ==> fbId = ''{0}''")
    @MethodSource({"nullable", "strings"})
    void name(String name) {
        //When
        FBUserDTO fbUserDTO = FBUserDTOBuilder.getInstance().name(name).build();

        //Then
        assertThat(fbUserDTO.getName()).isEqualTo(name);
        assertThat(fbUserDTO).isEqualToIgnoringGivenFields(new FBUserDTO(), "name");
    }
}