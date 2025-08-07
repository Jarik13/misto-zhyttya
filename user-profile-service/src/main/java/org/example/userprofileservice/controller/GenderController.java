package org.example.userprofileservice.controller;

import org.example.userprofileservice.dto.gender.GenderResponse;
import org.example.userprofileservice.mapper.GenderMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/genders")
public class GenderController {
    @GetMapping
    public ResponseEntity<List<GenderResponse>> getGenders() {
        return ResponseEntity.ok(GenderMapper.mapGenders());
    }
}
