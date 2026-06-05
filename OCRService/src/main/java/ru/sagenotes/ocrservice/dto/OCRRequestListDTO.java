package ru.sagenotes.ocrservice.dto;

import lombok.Data;

import java.util.List;

@Data
public class OCRRequestListDTO {

    private String noteId;
    private List<OCRRequestDTO> files;
}
