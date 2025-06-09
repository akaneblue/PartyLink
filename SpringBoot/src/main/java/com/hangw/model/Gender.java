package com.hangw.model;

import java.util.Optional;

public enum Gender {
	MALE, FEMALE, ANY;
	
	public static Optional<Gender> fromString(String value) {
        try {
            return Optional.of(Gender.valueOf(value.toUpperCase()));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
