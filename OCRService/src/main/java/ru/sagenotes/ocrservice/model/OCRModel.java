package ru.sagenotes.ocrservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Entity
public class OCRModel {

    @Getter
    @Id
    private String fid;
    @Getter
    @Column(columnDefinition = "TEXT")
    private String text;
    @Getter
    private String noteId;

}
