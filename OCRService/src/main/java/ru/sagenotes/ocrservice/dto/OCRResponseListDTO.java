package ru.sagenotes.ocrservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class OCRResponseListDTO {

    private String noteId;
    private List<OCRResponseDTO> files;
}
