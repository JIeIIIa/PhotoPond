package ua.kiev.prog.photopond.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.annotation.Order;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ua.kiev.prog.photopond.transfer.New;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static ua.kiev.prog.photopond.annotation.profile.ProfileConstants.DATABASE_STORAGE;
import static ua.kiev.prog.photopond.annotation.profile.ProfileConstants.DEV;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles({DEV, DATABASE_STORAGE, "unitTest", "test", "testDB"})
class UniqueLoginValidatorTest {
    @TestConfiguration
    @Order(Integer.MIN_VALUE)
    static class UniqueLoginValidatorTestConfiguration implements CommandLineRunner {
        @MockBean
        private UserInfoService userInfoService;

        @Override
        public void run(String... args) {
            when(userInfoService.findUserByLogin(any(String.class)))
                    .thenAnswer(invocationOnMock -> Optional.of(
                            UserInfoDTOBuilder.getInstance().login((String) invocationOnMock.getArguments()[0]).build()
                    ));
        }
    }

    @Autowired
    private Validator validator;

    @Autowired
    private UserInfoService userInfoService;

    private UserInfoDTO userInfoDTO;

    @BeforeEach
    void setUp() {
        userInfoDTO = UserInfoDTOBuilder.getInstance()
                .login("Login")
                .oldPassword("oldPassword")
                .password("password")
                .passwordConfirmation("password")
                .build();
    }

    @Test
    void valid() {
        //Given
        when(userInfoService.existsByLogin(any())).thenReturn(true);

        //When
        Set<ConstraintViolation<UserInfoDTO>> constraintViolations = validator.validate(userInfoDTO, New.class);

        //Then
        assertThat(constraintViolations).hasSize(1);
    }

    @Test
    void notValid() {
        //Given
        when(userInfoService.existsByLogin(any())).thenReturn(false);

        //When
        Set<ConstraintViolation<UserInfoDTO>> constraintViolations = validator.validate(userInfoDTO, New.class);

        //Then
        assertThat(constraintViolations).hasSize(0);
    }
}