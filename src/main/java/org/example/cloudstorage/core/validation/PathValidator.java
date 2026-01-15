package org.example.cloudstorage.core.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PathValidator implements ConstraintValidator<ValidPath, String> {

    private static final String VALID_PATH_REGEX = "^\\/?$|^(?!\\/{2})(?!.*\\/\\/)[A-Za-z0-9._-]+(?:\\/[A-Za-z0-9._-]+)*\\/?$\n";

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        if (s == null || s.isEmpty() || !s.matches(VALID_PATH_REGEX)) {
            return false;
        }

        return true;
    }
}
