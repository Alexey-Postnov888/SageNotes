package ru.sagenotes.ocrservice.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class OCRRequestDTO {

    private MultipartFile imageFile;
}
