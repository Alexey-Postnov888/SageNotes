package ru.sagenotes.ocrservice.repository;

import org.springframework.data.repository.CrudRepository;
import ru.sagenotes.ocrservice.model.OCRModel;

public interface OCRRepository extends CrudRepository<OCRModel, String> {
}
