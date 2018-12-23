package ua.kiev.prog.photopond.twitter;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import twitter4j.auth.RequestToken;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class TwitterRequestTokenStorageTest {

    private TwitterRequestTokenStorage instance;

    @BeforeEach
    public void setUp() {
        instance = new TwitterRequestTokenStorage();
    }

    @Test
    public void addAsStrings() {
        //Given
        String token = "123";

        //When
        instance.add(token, "4567");

        //Then
        Assertions.assertThat(instance.toString())
                .contains("map.keySet=[" + token + "]");
    }

    @Test
    public void addAsRequestToken() {
        //Given
        String token = "123";
        RequestToken requestToken = new RequestToken(token, "4567");

        //When
        instance.add(requestToken);

        //Then
        Assertions.assertThat(instance.toString())
                .contains("map.keySet=[" + token + "]");
    }

    @Test
    public void setMaxSize() {
        //Given
        final int MAX_SIZE = 45;

        //When
        instance.setMaxSize(MAX_SIZE);

        //Then
        Assertions.assertThat(instance.toString())
                .contains("maxSize=" + MAX_SIZE + ", ");
    }

    @Test
    public void setZeroAsMaxSize() {
        //When
        assertThrows(IllegalArgumentException.class,
                () -> instance.setMaxSize(-1)
        );
    }

    @Test
    public void setNegativeNumberAsMaxSize() {
        //When
        assertThrows(IllegalArgumentException.class,
                () -> instance.setMaxSize(-123)
        );
    }

    @Test
    public void addToFullMap() {
        //Given
        instance.setMaxSize(3);
        instance.add("1", "234");
        instance.add("22", "567");
        instance.add("333", "890");

        //When
        instance.add("4444", "qwerty");

        //Then
        Assertions.assertThat(instance.toString())
                .contains("map.keySet=[22, 333, 4444]");
    }

    @Test
    public void retrieveAndRemoveRequestToken() {
        //Given
        instance.setMaxSize(3);
        instance.add("1", "234");
        instance.add("22", "567");
        instance.add("333", "890");

        //When
        instance.retrieveAndRemoveRequestToken("22");

        //Then
        Assertions.assertThat(instance.toString())
                .contains("map.keySet=[1, 333]");
    }

    @Test
    public void retrieveAndRemoveUnknownRequestToken() {
        //Given
        instance.setMaxSize(3);
        instance.add("1", "234");
        instance.add("22", "567");
        instance.add("333", "890");

        //When
        instance.retrieveAndRemoveRequestToken("4444");

        //Then
        Assertions.assertThat(instance.toString())
                .contains("map.keySet=[1, 22, 333]");
    }
}