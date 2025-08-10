package org.example.authservice.dto.gender;

import lombok.Getter;

@Getter
public enum Gender {
    MALE(0L),
    FEMALE(1L),
    TRANSGENDER(2L),
    OTHER(3L);

    private final Long id;

    Gender(Long id) {
        this.id = id;
    }
}

