package com.govos.doc.valueobject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VersionNumberTest {

    @Test
    void shouldCreateWithValueOnly() {
        VersionNumber versionNumber = new VersionNumber(1);

        assertThat(versionNumber.getValue()).isEqualTo(1);
        assertThat(versionNumber.getLabel()).isNull();
    }

    @Test
    void shouldCreateWithValueAndLabel() {
        VersionNumber versionNumber = new VersionNumber(2, "v2");

        assertThat(versionNumber.getValue()).isEqualTo(2);
        assertThat(versionNumber.getLabel()).isEqualTo("v2");
    }

    @Test
    void shouldSupportSetters() {
        VersionNumber versionNumber = new VersionNumber();
        versionNumber.setValue(10);
        versionNumber.setLabel("release");

        assertThat(versionNumber.getValue()).isEqualTo(10);
        assertThat(versionNumber.getLabel()).isEqualTo("release");
    }
}
