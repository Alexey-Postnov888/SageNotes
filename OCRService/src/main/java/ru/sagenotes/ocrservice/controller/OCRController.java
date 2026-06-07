package ru.sagenotes.ocrservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.sagenotes.ocrservice.dto.OCRRequestListDTO;
import ru.sagenotes.ocrservice.dto.OCRResponseDTO;
import ru.sagenotes.ocrservice.dto.OCRResponseListDTO;
import ru.sagenotes.ocrservice.service.OCRService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ocr")
public class OCRController {

    private final OCRService ocrService;

    @PostMapping("/upload")
    public OCRResponseListDTO handleFileUpload(@Valid @ModelAttribute OCRRequestListDTO dto) {
        return ocrService.process(dto);
    }

    @GetMapping()
    public OCRResponseDTO getOCR(String fid) {
        return ocrService.getOCR(fid);
    }
}