package io.scinapse.api.validator;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class WebsiteValidator implements ConstraintValidator<Website, String> {

    private static final UrlValidator WEBSITE_VALIDATOR = new UrlValidator(new String[] { "http", "https" });

    @Override
    public void initialize(Website constraintAnnotation) {}

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (StringUtils.isBlank(value)) {
            return true;
        }
        return WEBSITE_VALIDATOR.isValid(value);
    }

}
