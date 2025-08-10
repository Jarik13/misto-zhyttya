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

@Tag(name = "API статей", description = "Операції, пов'язані зі статтю користувача")
@RestController
@RequestMapping("/api/v1/genders")
public class GenderController {
    @Operation(summary = "Отримати всі статі", description = "Повертає список усіх доступних варіантів статі")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список статей успішно отримано"),
            @ApiResponse(responseCode = "500", description = "Внутрішня помилка сервера")
    })
    @GetMapping
    public ResponseEntity<List<GenderResponse>> getGenders() {
        return ResponseEntity.ok(GenderMapper.mapGenders());
    }
}
