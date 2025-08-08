package org.example.userprofileservice.model;

import lombok.Getter;

@Getter
public enum Gender {
    MALE(0),
    FEMALE(1),
    TRANSGENDER(2),
    OTHER(3);

    private final long id;

    Gender(long id) {
        this.id = id;
    }

    public static Gender fromId(long id) {
        for (Gender g : values()) {
            if (g.id == id) {
                return g;
            }
        }
        throw new IllegalArgumentException("Unknown gender id: " + id);
    }
}
