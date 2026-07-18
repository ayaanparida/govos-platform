package com.govos.doc.valueobject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FileSizeTest {

    @Test
    void shouldCreateWithSizeBytes() {
        FileSize fileSize = new FileSize(1024L);

        assertThat(fileSize.getSizeBytes()).isEqualTo(1024L);
    }

    @Test
    void shouldSupportDefaultConstructorAndSetter() {
        FileSize fileSize = new FileSize();
        fileSize.setSizeBytes(1L);

        assertThat(fileSize.getSizeBytes()).isEqualTo(1L);
    }

    @Test
    void shouldAcceptLargeBoundaryValue() {
        FileSize fileSize = new FileSize(Long.MAX_VALUE);

        assertThat(fileSize.getSizeBytes()).isEqualTo(Long.MAX_VALUE);
    }
}
