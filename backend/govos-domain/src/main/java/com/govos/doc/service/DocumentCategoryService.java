package com.govos.doc.service;

import com.govos.doc.dto.category.CreateDocumentCategoryRequest;
import com.govos.doc.dto.category.UpdateDocumentCategoryRequest;
import com.govos.doc.entity.DocumentCategory;

import java.util.UUID;

public interface DocumentCategoryService {

    DocumentCategory createCategory(CreateDocumentCategoryRequest request);

    DocumentCategory updateCategory(UUID id, UpdateDocumentCategoryRequest request);

    void deleteCategory(UUID id);

    DocumentCategory restoreCategory(UUID id);
}
