package ru.sagenotes.ocrservice.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Data
public class OCRRequestDTO {

    private String fid;
    private MultipartFile imageFile;
}
