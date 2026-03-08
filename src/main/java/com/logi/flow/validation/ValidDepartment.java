package com.logi.flow.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Custom constraint annotation for the Validation Layer demo.
 *
 * Validates that the department value belongs to an allowed set.
 * Paired with {@link DepartmentValidator} which implements the actual check.
 *
 * Execution order in Flow 2 (POST /api/users):
 *   ...MessageConverter.read → @InitBinder → @Valid → DepartmentValidator.isValid() → Service...
 */
@Documented
@Constraint(validatedBy = DepartmentValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDepartment {

    String message() default "Department must be one of: Engineering, Marketing, HR, Finance, Operations";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
