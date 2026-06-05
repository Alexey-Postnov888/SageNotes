package ru.sagenotes.ocrservice.service;

import org.springframework.web.multipart.MultipartFile;
import ru.sagenotes.ocrservice.dto.OCRResponseDTO;

public interface OCRService {

    OCRResponseDTO processImage(MultipartFile file);
}
