package ua.kiev.prog.photopond.exception;

import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import ua.kiev.prog.photopond.drive.exception.DirectoryException;
import ua.kiev.prog.photopond.drive.exception.DirectoryModificationException;
import ua.kiev.prog.photopond.drive.exception.DriveException;
import ua.kiev.prog.photopond.drive.exception.PictureFileException;
import ua.kiev.prog.photopond.facebook.exception.*;
import ua.kiev.prog.photopond.twitter.exception.*;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class PhotoPondExceptionTest {
    @TestFactory
    Stream<DynamicContainer> allExceptionTests() {
        return Stream.of(
                packageExceptions("//exception", Stream.of(
                        AccessDeniedException.class, AddToRepositoryException.class, PhotoPondException.class,
                        RepositoryException.class)),
                packageExceptions("//drive.exception", Stream.of(
                        DirectoryException.class, DirectoryModificationException.class,
                        DriveException.class, PictureFileException.class)),
                packageExceptions("//facebook.exception", Stream.of(
                        AssociateFBAccountException.class, DisassociateFBAccountException.class,
                        FBAccountAlreadyAssociateException.class, FBAuthenticationException.class,
                        FBException.class
                )),
                packageExceptions("//twitter.exception", Stream.of(
                        AssociateTwitterAccountException.class, CustomTwitterException.class,
                        DisassociateTwitterAccountException.class, NotFoundTwitterAssociatedAccountException.class,
                        TweetPublishingException.class, TwitterAccountAlreadyAssociateException.class,
                        TwitterAuthenticationException.class
                ))
        );
    }

    DynamicContainer packageExceptions(String shortName, Stream<Class<? extends Exception>> classes) {
        return DynamicContainer.dynamicContainer(
                shortName,
                classes.map(this::exceptionTests)
        );
    }

    DynamicContainer exceptionTests(Class<? extends Exception> clazz) {
        return DynamicContainer.dynamicContainer(clazz.getName(),
                Stream.of(
                        DynamicTest.dynamicTest("default", () -> {
                            Exception exception = clazz.getConstructor().newInstance();
                            assertThat(exception).isNotNull();
                        }),
                        DynamicTest.dynamicTest("message", () -> {
                            Exception exception = clazz.getConstructor(String.class).newInstance("some message");
                            assertThat(exception)
                                    .isNotNull()
                                    .hasMessage("some message")
                                    .hasNoCause();
                        }),
                        DynamicTest.dynamicTest("message and cause", () -> {
                            Exception exception = clazz.getConstructor(String.class, Throwable.class)
                                    .newInstance("some message", new RuntimeException());
                            assertThat(exception)
                                    .isNotNull()
                                    .hasMessage("some message")
                                    .hasCause(new RuntimeException());
                        }),
                        DynamicTest.dynamicTest("cause", () -> {
                            Exception exception = clazz.getConstructor(Throwable.class)
                                    .newInstance(new RuntimeException());
                            assertThat(exception)
                                    .isNotNull()
                                    .hasMessage("java.lang.RuntimeException")
                                    .hasCause(new RuntimeException());
                        }),
                        DynamicTest.dynamicTest("message, cause, enableSuppression, writableStackTrace", () -> {
                            Exception exception = clazz.getConstructor(String.class, Throwable.class, boolean.class, boolean.class)
                                    .newInstance("some message", new RuntimeException(), true, true);
                            assertThat(exception)
                                    .isNotNull()
                                    .hasMessage("some message")
                                    .hasCause(new RuntimeException());
                        })
                )
        );
    }
}