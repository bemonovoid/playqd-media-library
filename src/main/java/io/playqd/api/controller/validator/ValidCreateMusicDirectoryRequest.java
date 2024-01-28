package io.playqd.api.controller.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CreateMusicDirectoryRequestValidator.class)
@Documented
public @interface ValidCreateMusicDirectoryRequest {

  String message() default "{sourcePath.isInvalid}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
