package ua.kiev.prog.photopond.core;

import org.springframework.validation.ObjectError;

import java.util.List;
import java.util.Locale;

public interface BindingErrorResolver {
    String resolveMessage(String key, Object[] arguments, Locale locale);

    String resolveMessage(String key, Locale locale);

    String resolveMessage(ObjectError error, Locale locale);

    BindingErrorDTO resolve(ObjectError error, Locale locale);

    List<String> resolveAllMessages(List<ObjectError> errors, Locale locale);

    List<BindingErrorDTO> resolveAll(List<ObjectError> errors, Locale locale);

    String resolveAndJoinAll(List<ObjectError> errors, Locale locale);
}
