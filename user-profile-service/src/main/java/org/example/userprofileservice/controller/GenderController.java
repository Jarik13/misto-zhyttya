package org.example.userprofileservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.userprofileservice.dto.gender.GenderResponse;
import org.example.userprofileservice.mapper.GenderMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Gender API", description = "Operations related to genders")
@RestController
@RequestMapping("/api/v1/genders")
public class GenderController {
    @Operation(summary = "Get all genders", description = "Returns a list of all available gender options")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved gender list"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<List<GenderResponse>> getGenders() {
        return ResponseEntity.ok(GenderMapper.mapGenders());
    }
}
