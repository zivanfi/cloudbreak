package com.sequenceiq.cloudbreak.api.model.imagecatalog;

import static com.sequenceiq.cloudbreak.validation.HttpContentSizeValidator.MAX_IN_BYTES;
import static com.sequenceiq.cloudbreak.validation.ImageCatalogValidator.FAILED_TO_GET_WITH_EXCEPTION;
import static com.sequenceiq.cloudbreak.validation.ImageCatalogValidator.INVALID_JSON_IN_RESPONSE;
import static com.sequenceiq.cloudbreak.validation.ImageCatalogValidator.INVALID_JSON_STRUCTURE_IN_RESPONSE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.validation.Configuration;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Response.Status.Family;
import javax.ws.rs.core.Response.StatusType;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sequenceiq.cloudbreak.api.endpoint.v4.imagecatalog.base.ImageCatalogV4Base;
import com.sequenceiq.cloudbreak.api.helper.HttpHelper;
import com.sequenceiq.cloudbreak.validation.HttpContentSizeValidator;

@RunWith(MockitoJUnitRunner.class)
public class ImageCatalogV4BaseTest {

    public static final String FAILED_TO_GET_BY_FAMILY_TYPE = "Failed to get response by the specified URL '%s' due to: '%s'!";

    private static final String INVALID_MESSAGE = "A valid image catalog must be available on the given URL";

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageCatalogV4BaseTest.class);

    @Mock
    private StatusType statusType;

    private ConstraintValidatorContext constraintValidatorContext;

    private ConstraintViolationBuilder constraintViolationBuilder;

    private NodeBuilderCustomizableContext nodeBuilderCustomizableContext;

    @Mock
    private HttpHelper httpHelper;

    @Mock
    private HttpContentSizeValidator httpContentSizeValidator;

    private Validator validator;

    @Before
    public void setUp() {
        Configuration<?> cfg = Validation.byDefaultProvider().configure();
        cfg.messageInterpolator(new ParameterMessageInterpolator());
        validator = cfg.buildValidatorFactory().getValidator();

        constraintViolationBuilder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
        nodeBuilderCustomizableContext = mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class);
        constraintValidatorContext = mock(ConstraintValidatorContext.class);

        when(constraintValidatorContext.buildConstraintViolationWithTemplate(anyString())).thenReturn(constraintViolationBuilder);
        when(constraintViolationBuilder.addPropertyNode(anyString())).thenReturn(nodeBuilderCustomizableContext);
        when(nodeBuilderCustomizableContext.addConstraintViolation()).thenReturn(constraintValidatorContext);

        when(statusType.getFamily()).thenReturn(Family.SUCCESSFUL);
        when(httpContentSizeValidator.isValid(anyString(), any(ConstraintValidatorContext.class))).thenReturn(true);
        when(httpHelper.getContentLength(anyString())).thenReturn(new ImmutablePair<>(statusType, MAX_IN_BYTES));
    }

    @Test
    public void testContentNotAvailable() {
        String url = "http://protocol.com";
        String reasonPhrase = "Invalid reason phrase";
        String failedToGetMessage = String.format(FAILED_TO_GET_BY_FAMILY_TYPE, url, reasonPhrase);
        AtomicBoolean returnValue = new AtomicBoolean(false);

        when(statusType.getFamily()).thenReturn(Family.OTHER);
        when(statusType.getReasonPhrase()).thenReturn(reasonPhrase);
        when(httpHelper.getContent(anyString())).thenReturn(new ImmutablePair<>(statusType, reasonPhrase));
        when(constraintValidatorContext.buildConstraintViolationWithTemplate(failedToGetMessage)).thenReturn(constraintViolationBuilder);

        ImageCatalogV4Base i = new ImageCatalogV4Base();
        i.setName("testname");
        i.setUrl(url);

        Set<ConstraintViolation<ImageCatalogV4Base>> violations = validator.validate(i);

        assertEquals(2L, violations.size());
        assertTrue(violations.stream().allMatch(cv -> {
            if (cv.getMessage().equals(INVALID_MESSAGE)) {
                returnValue.set(true);
            } else {
                LOGGER.info("Assertion is failing, because of actual message is '{}'", cv.getMessage());
                LOGGER.info("Expected message {}", INVALID_MESSAGE);
            }
            if (cv.getMessage().equals(failedToGetMessage)) {
                returnValue.set(true);
            } else {
                LOGGER.info("Assertion is failing, because of actual message is '{}'", cv.getMessage());
                LOGGER.info("Expected message {}", failedToGetMessage);
            }
            return returnValue.get();
        }));
    }

    @Test
    public void testContentStructureNotValid() {
        AtomicBoolean returnValue = new AtomicBoolean(false);

        when(statusType.getFamily()).thenReturn(Family.OTHER);
        when(constraintValidatorContext.buildConstraintViolationWithTemplate(INVALID_JSON_STRUCTURE_IN_RESPONSE)).thenReturn(constraintViolationBuilder);

        ImageCatalogV4Base i = new ImageCatalogV4Base();
        i.setName("testname");
        i.setUrl("http://protocol.com");

        Set<ConstraintViolation<ImageCatalogV4Base>> violations = validator.validate(i);

        assertEquals(2L, violations.size());
        assertTrue(violations.stream().allMatch(cv -> {
            if (cv.getMessage().equals(INVALID_MESSAGE)) {
                returnValue.set(true);
            } else {
                LOGGER.info("Assertion is failing, because of actual message is '{}'", cv.getMessage());
                LOGGER.info("Expected message {}", INVALID_MESSAGE);
            }
            if (cv.getMessage().equals(INVALID_JSON_STRUCTURE_IN_RESPONSE)) {
                returnValue.set(true);
            } else {
                LOGGER.info("Assertion is failing, because of actual message is '{}'", cv.getMessage());
                LOGGER.info("Expected message {}", INVALID_JSON_STRUCTURE_IN_RESPONSE);
            }
            return returnValue.get();
        }));
    }

    @Test
    public void testContentNotAValidJSON() {
        when(httpHelper.getContent(anyString())).thenReturn(new ImmutablePair<>(statusType, "{[]}"));
        when(constraintValidatorContext.buildConstraintViolationWithTemplate(INVALID_JSON_IN_RESPONSE)).thenReturn(constraintViolationBuilder);

        ImageCatalogV4Base i = new ImageCatalogV4Base();
        i.setName("testname");
        i.setUrl("http://protocol.com");

        Set<ConstraintViolation<ImageCatalogV4Base>> violations = validator.validate(i);

        assertEquals(2L, violations.size());
        assertTrue(violations.stream().allMatch(cv -> {
            LOGGER.info("Assertion is failing, because of {}", cv.getMessage());
            return cv.getMessage().equals(INVALID_MESSAGE) || cv.getMessage().equals(INVALID_JSON_IN_RESPONSE);
        }));
    }

    @Test
    public void testWhenWebTargetFailsWithException() {
        when(httpHelper.getContent(anyString())).thenThrow(ProcessingException.class);

        ImageCatalogV4Base i = new ImageCatalogV4Base();
        i.setName("testname");
        i.setUrl("http://protocol.com");

        Set<ConstraintViolation<ImageCatalogV4Base>> violations = validator.validate(i);

        assertEquals(2L, violations.size());
        String failsWithExceptionMessage = String.format(FAILED_TO_GET_WITH_EXCEPTION, i.getUrl());
        when(constraintValidatorContext.buildConstraintViolationWithTemplate(failsWithExceptionMessage)).thenReturn(constraintViolationBuilder);
        assertTrue(violations.stream().allMatch(cv -> {
            LOGGER.info("Assertion is failing, because of {}", cv.getMessage());
            return cv.getMessage().equals(INVALID_MESSAGE) || cv.getMessage().equals(failsWithExceptionMessage);
        }));
    }

    @Test
    public void testWhenContentIsTooBig() {
        when(constraintValidatorContext.buildConstraintViolationWithTemplate(INVALID_MESSAGE)).thenReturn(constraintViolationBuilder);

        ImageCatalogV4Base i = new ImageCatalogV4Base();
        i.setName("testname");
        i.setUrl("http://protocol.com");

        Set<ConstraintViolation<ImageCatalogV4Base>> violations = validator.validate(i);

        assertEquals(1L, violations.size());
        assertTrue(violations.stream().allMatch(cv -> cv.getMessage().equals(INVALID_MESSAGE)));
    }
}
