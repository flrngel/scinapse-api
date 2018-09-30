package io.scinapse.api.validator;

import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class NoSpecialCharsValidator implements ConstraintValidator<NoSpecialChars, String> {

    private static final Pattern SPECIAL_CHARACTERS = Pattern.compile("[\\p{Punct}&&[^.']]");

    @Override
    public void initialize(NoSpecialChars constraintAnnotation) {}

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (StringUtils.isBlank(value)) {
            return true;
        }
        return !SPECIAL_CHARACTERS.matcher(value).find();
    }

}
