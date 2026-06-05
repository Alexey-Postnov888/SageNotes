package ru.sagenotes.ocrservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Data
public class OCRRequestDTO {

    @NotNull
    private String fid;
    @NotNull
    private MultipartFile imageFile;
}
