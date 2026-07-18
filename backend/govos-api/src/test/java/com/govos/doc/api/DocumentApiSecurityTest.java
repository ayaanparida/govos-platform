package com.govos.doc.api;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentApiSecurityTest {

    @Test
    void shouldProtectDocumentEndpointsWithPermissions() {
        long protectedMethods = Arrays.stream(DocumentController.class.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(PreAuthorize.class))
                .count();

        assertThat(protectedMethods).isGreaterThanOrEqualTo(13);
    }

    @Test
    void shouldRequireDocReadForFindDocument() throws NoSuchMethodException {
        var method = DocumentController.class.getDeclaredMethod(
                "findDocument", java.util.UUID.class, jakarta.servlet.http.HttpServletRequest.class);

        assertThat(method.getAnnotation(PreAuthorize.class).value())
                .contains("DOC_READ");
    }

    @Test
    void shouldRequireDocWriteForCreateDocument() throws NoSuchMethodException {
        var method = DocumentController.class.getDeclaredMethod(
                "createDocument",
                com.govos.doc.dto.document.CreateDocumentRequest.class,
                jakarta.servlet.http.HttpServletRequest.class);

        assertThat(method.getAnnotation(PreAuthorize.class).value())
                .contains("DOC_WRITE");
    }

    @Test
    void shouldRequireDocDeleteForDeleteDocument() throws NoSuchMethodException {
        var method = DocumentController.class.getDeclaredMethod(
                "deleteDocument", java.util.UUID.class, jakarta.servlet.http.HttpServletRequest.class);

        assertThat(method.getAnnotation(PreAuthorize.class).value())
                .contains("DOC_DELETE");
    }

    @Test
    void shouldRequireDocShareForShareOperations() throws NoSuchMethodException {
        var method = DocumentShareController.class.getDeclaredMethod(
                "createShare",
                com.govos.doc.dto.share.CreateShareRequest.class,
                jakarta.servlet.http.HttpServletRequest.class);

        assertThat(method.getAnnotation(PreAuthorize.class).value())
                .contains("DOC_SHARE");
    }

    @Test
    void shouldRequireDocAdminForStorageProviderOperations() throws NoSuchMethodException {
        var method = StorageProviderController.class.getDeclaredMethod(
                "createStorageProvider",
                com.govos.doc.dto.storage.CreateStorageProviderRequest.class,
                jakarta.servlet.http.HttpServletRequest.class);

        assertThat(method.getAnnotation(PreAuthorize.class).value())
                .contains("DOC_ADMIN");
    }
}
