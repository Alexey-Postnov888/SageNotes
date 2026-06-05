package ru.sagenotes.ocrservice.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.sagenotes.ocrservice.dto.OCRResponseDTO;
import ru.sagenotes.ocrservice.service.OCRService;
import ru.sagenotes.ocrservice.util.OCRProcessor;

import java.io.File;
import java.io.IOException;

@Service
@Slf4j
public class OCRServiceImpl implements OCRService {

    @Override
    public OCRResponseDTO processImage(MultipartFile file) {

        OCRResponseDTO result = new OCRResponseDTO();
        try {
            File tempFile = File.createTempFile("ocr",".tmp");
            file.transferTo(tempFile);

            OCRProcessor ocrProcessor = new OCRProcessor();
            String extractedText = ocrProcessor.extractTextFromImage(tempFile);

            tempFile.delete();

            result.setExtractedText(extractedText);

        } catch (IOException e) {
            log.info("Error in processing image::{}", String.valueOf(e));
        }

        return result;
    }
}
