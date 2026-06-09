package ru.sagenotes.ocrservice.repository;

import org.springframework.data.repository.CrudRepository;
import ru.sagenotes.ocrservice.model.OCRModel;

import java.util.List;
import java.util.UUID;

public interface OCRRepository extends CrudRepository<OCRModel, UUID> {
    List<OCRModel> findAllByNoteId(UUID noteId);
}
