package com.govos.doc.valueobject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentPathTest {

    @Test
    void shouldCreateWithPathAndDepth() {
        DocumentPath path = new DocumentPath("/Root/Child", 1);

        assertThat(path.getMaterializedPath()).isEqualTo("/Root/Child");
        assertThat(path.getDepthLevel()).isEqualTo(1);
    }

    @Test
    void shouldSupportDefaultConstructorAndSetters() {
        DocumentPath path = new DocumentPath();
        path.setMaterializedPath("/");
        path.setDepthLevel(0);

        assertThat(path.getMaterializedPath()).isEqualTo("/");
        assertThat(path.getDepthLevel()).isZero();
    }

    @Test
    void shouldAcceptMaxDepthBoundary() {
        DocumentPath path = new DocumentPath("/deep", 50);

        assertThat(path.getDepthLevel()).isEqualTo(50);
    }
}
