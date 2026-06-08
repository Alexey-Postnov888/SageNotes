package ru.sagenotes.ocrservice.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.sagenotes.ocrservice.dto.OCRRequestListDTO;
import ru.sagenotes.ocrservice.dto.OCRResponseDTO;
import ru.sagenotes.ocrservice.dto.OCRResponseListDTO;
import ru.sagenotes.ocrservice.service.OCRService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ocr")
@Validated
public class OCRController {

    private final OCRService ocrService;

    @PostMapping("/upload")
    @PreAuthorize("#jwt.getClaim('sub') != null")
    public OCRResponseListDTO handleFileUpload(
            @Valid @RequestBody OCRRequestListDTO dto,
            @AuthenticationPrincipal Jwt jwt) {
        return ocrService.process(dto);
    }

    @GetMapping("/note/{note_id}")
    @PreAuthorize("#jwt.getClaim('sub') != null")
    public OCRResponseListDTO getOCRbyNote(
            @PathVariable @NotBlank String note_id,
            @AuthenticationPrincipal Jwt jwt) {
        return ocrService.getOCRbyNote(note_id);
    }

    @GetMapping("/fid/{fid}")
    @PreAuthorize("#jwt.getClaim('sub') != null")
    public OCRResponseDTO getOCR(
            @PathVariable @NotBlank String fid,
            @AuthenticationPrincipal Jwt jwt) {
        return ocrService.getOCR(fid);
    }
}