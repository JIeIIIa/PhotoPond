package ua.kiev.prog.photopond.user;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SettingsPageUtilsTest {

    @Test
    void url() {
        //Given
        Authentication authentication = new UsernamePasswordAuthenticationToken("userLogin", null, Collections.emptyList());
        String expectedUrl = mageSettingsUrl("userLogin");

        //When
        String url = SettingsPageUtils.url(authentication);

        //Then
        assertThat(url).isEqualTo(expectedUrl);
    }

    @Test
    void urlAuthenticationIsNull() {
        //When
        String url = SettingsPageUtils.url(null);

        //Then
        assertThat(url).isEqualTo("");
    }

    @Test
    void urlNotAuthenticate() {
        //Given
        Authentication authentication = new UsernamePasswordAuthenticationToken("userName", null, Collections.emptyList());
        authentication.setAuthenticated(false);
        String expectedUrl = "";

        //When
        String url = SettingsPageUtils.url(authentication);

        //Then
        assertThat(url).isEqualTo(expectedUrl);
    }

    @Test
    void socials() {
        //Given
        Authentication authentication = new UsernamePasswordAuthenticationToken("userName", null, Collections.emptyList());
        String expectedUrl = "";
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

        //When
        SettingsPageUtils.socials(authentication, redirectAttributes);

        //Then
        assertThat(redirectAttributes.getFlashAttributes()).containsKey("startItem");
        assertThat(redirectAttributes.getFlashAttributes().get("startItem")).isEqualTo(3);
    }

    @Test
    void socialsAuthenticationIsNull() {
        //Given
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

        //When
        assertThrows(IllegalArgumentException.class, () -> SettingsPageUtils.socials(null, redirectAttributes));
    }


    @Test
    void socialsNotAuthenticate() {
        //Given
        Authentication authentication = new UsernamePasswordAuthenticationToken("userName", null, Collections.emptyList());
        authentication.setAuthenticated(false);
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

        //When
        assertThrows(IllegalArgumentException.class, () -> SettingsPageUtils.socials(authentication, redirectAttributes));
    }

    @Test
    void socialsRedirectedAttributeIsNull() {
        //Given
        Authentication authentication = new UsernamePasswordAuthenticationToken("userName", null, Collections.emptyList());

        //When
        assertThrows(IllegalArgumentException.class, () -> SettingsPageUtils.socials(authentication, null));
    }

    private String mageSettingsUrl(String login) {
        return "/user/" + login + "/settings";
    }
}