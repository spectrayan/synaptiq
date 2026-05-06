package com.synaptiq.shared.domain;

/**
 * Specification pattern — domain-level business rule validation.
 */
@FunctionalInterface
public interface Specification<T> {

    boolean isSatisfiedBy(T candidate);

    default String unsatisfiedReason(T candidate) {
        return "Specification not satisfied";
    }
}
