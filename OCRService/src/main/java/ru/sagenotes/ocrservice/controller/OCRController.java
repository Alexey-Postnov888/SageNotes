package ru.sagenotes.ocrservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.sagenotes.ocrservice.dto.OCRResponseDTO;
import ru.sagenotes.ocrservice.service.OCRService;

@RestController
@RequestMapping("/api/ocr")
public class OCRController {

    @Autowired
    private OCRService ocrService;

    @PostMapping("/upload")
    public OCRResponseDTO handleFileUpload(@RequestParam("imageFile") MultipartFile imageFile) {
        return ocrService.processImage(imageFile);
    }
}