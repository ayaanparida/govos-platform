package com.govos.doc.valueobject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentChecksumTest {

    @Test
    void shouldCreateWithValueAndSupportSetters() {
        DocumentChecksum checksum = new DocumentChecksum("abc123");

        assertThat(checksum.getValue()).isEqualTo("abc123");

        checksum.setValue("def456");
        assertThat(checksum.getValue()).isEqualTo("def456");
    }

    @Test
    void shouldSupportDefaultConstructor() {
        DocumentChecksum checksum = new DocumentChecksum();
        checksum.setValue("hash");

        assertThat(checksum.getValue()).isEqualTo("hash");
    }
}
