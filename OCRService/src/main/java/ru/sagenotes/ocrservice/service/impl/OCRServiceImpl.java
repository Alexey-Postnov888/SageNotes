package ru.sagenotes.ocrservice.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.sagenotes.ocrservice.dto.OCRRequestListDTO;
import ru.sagenotes.ocrservice.dto.OCRResponseDTO;
import ru.sagenotes.ocrservice.dto.OCRResponseListDTO;
import ru.sagenotes.ocrservice.model.OCRModel;
import ru.sagenotes.ocrservice.repository.OCRRepository;
import ru.sagenotes.ocrservice.service.OCRService;
import ru.sagenotes.ocrservice.util.OCRProcessor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class OCRServiceImpl implements OCRService {

    private final OCRRepository repository;

    @Autowired
    public OCRServiceImpl(OCRRepository repository) {
        this.repository = repository;
    }

    @Override
    public OCRResponseListDTO process(OCRRequestListDTO dto) {
        String noteId = dto.getNoteId();

        List<OCRResponseDTO> files = new ArrayList<>();
        dto.getFiles().forEach( x ->
                files.add(new OCRResponseDTO(
                        x.getFid(),
                        processFile(x.getImageFile()))
                )
        );

        files.forEach(x -> createOCRModel(x.getFid(), x.getText(), noteId));
        return new OCRResponseListDTO(noteId, files);
    }

    public String processFile(MultipartFile file) {

        try {
            File tempFile = File.createTempFile("ocr",".tmp");
            file.transferTo(tempFile);

            OCRProcessor ocrProcessor = new OCRProcessor();
            String extractedText = ocrProcessor.extractTextFromImage(tempFile);

            tempFile.delete();

            return extractedText;

        } catch (IOException e) {
            log.info("Error in processing image::{}", String.valueOf(e));
        }

        return null;
    }

    @Override
    public void createOCRModel(String fid, String text, String noteId) {
        OCRModel ocrModel = new OCRModel(fid, text, noteId);
        repository.save(ocrModel);
    }

    @Override
    public OCRResponseDTO getOCR(String fid) {
        Optional<OCRModel> model = repository.findById(fid);

        return model.map(ocrModel -> new OCRResponseDTO(ocrModel.getFid(), ocrModel.getText())).orElse(null);
    }
}
