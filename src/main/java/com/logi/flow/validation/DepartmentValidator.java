package com.logi.flow.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Custom ConstraintValidator for the Validation Layer demo.
 *
 * Implements the actual validation logic for {@link ValidDepartment}.
 * Spring instantiates this class and calls:
 *   1. initialize() — once at startup when the validator is registered
 *   2. isValid()    — once per request field during @Valid processing
 *
 * Execution position in Flow 2 (POST /api/users):
 *   @InitBinder (trim) → @Valid triggers Bean Validation
 *     → DepartmentValidator.initialize() [startup only]
 *     → DepartmentValidator.isValid()    [per request]
 *     → MethodArgumentNotValidException  [if invalid → GlobalExceptionHandler]
 */
public class DepartmentValidator implements ConstraintValidator<ValidDepartment, String> {

    private static final Logger logger = LoggerFactory.getLogger(DepartmentValidator.class);

    private static final Set<String> ALLOWED_DEPARTMENTS = Set.of(
            "Engineering", "Marketing", "HR", "Finance", "Operations"
    );

    /**
     * Called once when Spring registers this validator (application startup).
     */
    @Override
    public void initialize(ValidDepartment constraintAnnotation) {
        logger.info("✅ VALIDATION - INIT: DepartmentValidator initialized with allowed={}", ALLOWED_DEPARTMENTS);
    }

    /**
     * Called for every POST /api/users request during @Valid processing.
     * Null / blank is accepted here — @NotBlank on the field handles that case separately.
     */
    @Override
    public boolean isValid(String department, ConstraintValidatorContext context) {
        // Optional field: let @NotBlank / @NotNull handle null/blank separately
        if (department == null || department.isBlank()) {
            return true;
        }

        boolean valid = ALLOWED_DEPARTMENTS.contains(department);
        logger.info("✅ VALIDATION - EXECUTING: DepartmentValidator.isValid('{}') → {}", department, valid);
        return valid;
    }
}
