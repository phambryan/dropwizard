package com.codahale.dropwizard.validation;

import org.junit.Test;

import javax.validation.Validation;
import javax.validation.Validator;

import static org.fest.assertions.api.Assertions.assertThat;

public class PortRangeValidatorTest {
    @SuppressWarnings("PublicField")
    public static class Example {
        @PortRange
        public int port = 8080;

        @PortRange(min = 10000, max = 15000)
        public int otherPort = 10001;
    }


    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    private final Example example = new Example();

    @Test
    public void acceptsNonPrivilegedPorts() throws Exception {
        example.port = 2048;

        assertThat(validator.validate(example))
                .isEmpty();
    }

    @Test
    public void acceptsDynamicPorts() throws Exception {
        example.port = 0;

        assertThat(validator.validate(example))
                .isEmpty();
    }

    @Test
    public void allowsForCustomMinimumPorts() throws Exception {
        example.otherPort = 8080;

        assertThat(ConstraintViolations.format(validator.validate(example)))
                .containsOnly("otherPort must be between 10000 and 15000 (was 8080)");
    }

    @Test
    public void allowsForCustomMaximumPorts() throws Exception {
        example.otherPort = 16000;

        assertThat(ConstraintViolations.format(validator.validate(example)))
                .containsOnly("otherPort must be between 10000 and 15000 (was 16000)");
    }
}
