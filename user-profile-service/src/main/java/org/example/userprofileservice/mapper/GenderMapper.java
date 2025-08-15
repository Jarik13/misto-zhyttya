package org.example.userprofileservice.mapper;

import org.example.userprofileservice.dto.gender.GenderResponse;
import org.example.userprofileservice.model.Gender;

import java.util.Arrays;
import java.util.List;

public class GenderMapper {
    public static List<GenderResponse> mapGenders() {
        return Arrays.stream(Gender.values())
                .map(gender -> new GenderResponse(
                        (long) gender.ordinal(),
                        gender.name()
                ))
                .toList();
    }
}
