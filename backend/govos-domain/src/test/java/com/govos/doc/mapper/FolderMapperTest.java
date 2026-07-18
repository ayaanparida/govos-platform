package com.govos.doc.mapper;

import com.govos.doc.dto.folder.CreateFolderRequest;
import com.govos.doc.dto.folder.UpdateFolderRequest;
import com.govos.doc.entity.Folder;
import com.govos.doc.support.DocumentTestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FolderMapperTest {

    private FolderMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new FolderMapperImpl();
    }

    @Test
    void shouldMapEntityToResponseFlatteningPathMetadata() {
        Folder entity = DocumentTestFixtures.folder(DocumentTestFixtures.FOLDER_ID);

        var response = mapper.toResponse(entity);

        assertThat(response.id()).isEqualTo(DocumentTestFixtures.FOLDER_ID);
        assertThat(response.materializedPath()).isEqualTo("/Root");
        assertThat(response.depthLevel()).isZero();
        assertThat(response.name()).isEqualTo("Root");
    }

    @Test
    void shouldMapCreateRequestToEntityWithEmbeddedPath() {
        CreateFolderRequest request = DocumentTestFixtures.createFolderRequest();

        Folder entity = mapper.toEntity(request);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getParentFolder()).isNull();
        assertThat(entity.getPathMetadata().getMaterializedPath()).isEqualTo("/Folder");
        assertThat(entity.getPathMetadata().getDepthLevel()).isZero();
        assertThat(entity.getName()).isEqualTo("Folder");
    }

    @Test
    void shouldUpdateEntityIgnoringOrganizationFields() {
        Folder entity = DocumentTestFixtures.folder(DocumentTestFixtures.FOLDER_ID);

        UpdateFolderRequest request = new UpdateFolderRequest(
                "Renamed", null, "/Renamed", 1, false, 0L);
        mapper.updateEntity(request, entity);

        assertThat(entity.getOrganizationId()).isEqualTo(DocumentTestFixtures.ORG_ID);
        assertThat(entity.getName()).isEqualTo("Renamed");
        assertThat(entity.getPathMetadata().getMaterializedPath()).isEqualTo("/Renamed");
        assertThat(entity.getPathMetadata().getDepthLevel()).isEqualTo(1);
    }

    @Test
    void shouldMapTreeResponseWithEmptyChildren() {
        Folder entity = DocumentTestFixtures.folder(DocumentTestFixtures.FOLDER_ID);

        var response = mapper.toTreeResponse(entity);

        assertThat(response.children()).isEmpty();
    }

    @Test
    void shouldMapResponseListWithParentFolder() {
        Folder entity = DocumentTestFixtures.folder(DocumentTestFixtures.FOLDER_ID);
        Folder parent = DocumentTestFixtures.folder(DocumentTestFixtures.CATEGORY_ID);
        entity.setParentFolder(parent);

        var responses = mapper.toResponseList(java.util.List.of(entity));

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().parentFolderId()).isEqualTo(DocumentTestFixtures.CATEGORY_ID);
        assertThat(mapper.toResponse(null)).isNull();
    }
}
