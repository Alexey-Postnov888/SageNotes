package ru.sagenotes.ocrservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class OCRResponseDTO {

    private String fid;
    private String text;
}
