package ua.kiev.prog.photopond.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static java.util.Collections.addAll;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

@Service
public class BindingErrorResolverImpl implements BindingErrorResolver {
    private static final String JOIN_DELIMITER = ";";

    private final MessageSource messageSource;

    @Autowired
    public BindingErrorResolverImpl(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public String resolveMessage(String key, Object[] arguments, Locale locale) {
        return messageSource.getMessage(key, arguments, locale);
    }

    @Override
    public String resolveMessage(String key, Locale locale) {
        return resolveMessage(key, null, locale);
    }

    @Override
    public String resolveMessage(ObjectError error, Locale locale) {
        List<String> keys = retrieveKeys(error);
        for (String key : keys) {
            String message = messageSource.getMessage(key, error.getArguments(), locale);
            if (!Objects.equals(key, message)) {
                return message;
            }
        }

        return "";
    }

    @Override
    public BindingErrorDTO resolve(ObjectError error, Locale locale) {
        String name = makeName(error);
        List<String> keys = retrieveKeys(error);
        for (String key : keys) {
            String message = messageSource.getMessage(key, error.getArguments(), locale);
            if (!Objects.equals(key, message)) {
                return new BindingErrorDTO(name, message);
            }
        }

        return new BindingErrorDTO(name, "");

    }

    private String makeName(ObjectError error) {
        String name = error.getObjectName();
        if (error instanceof FieldError) {
            name += "." + ((FieldError) error).getField();
        } else {
            name = error.getCode() + "." + name;
        }
        return name;
    }

    @Override
    public List<String> resolveAllMessages(List<ObjectError> errors, Locale locale) {
        return errors.stream()
                .map(e -> resolveMessage(e, locale))
                .collect(toList());
    }

    @Override
    public List<BindingErrorDTO> resolveAll(List<ObjectError> errors, Locale locale) {
        return errors.stream()
                .map(e -> resolve(e, locale))
                .collect(toList());
    }

    @Override
    public String resolveAndJoinAll(List<ObjectError> errors, Locale locale) {
        return errors.stream()
                .map(e -> resolveMessage(e, locale))
                .collect(joining(JOIN_DELIMITER));
    }

    private List<String> retrieveKeys(ObjectError error) {
        ArrayList<String> list = new ArrayList<>();
        if (isNull(error)) {
            return list;
        }

        String defaultMessage = error.getDefaultMessage();
        if (nonNull(defaultMessage) && !defaultMessage.isEmpty()) {
            list.add(defaultMessage);
            if (defaultMessage.matches("\\{.+\\}")) {
                list.add(defaultMessage.substring(1, defaultMessage.length() - 1));
            }
        }

        if (nonNull(error.getCodes())) {
            addAll(list, error.getCodes());
        }

        return list;
    }
}
