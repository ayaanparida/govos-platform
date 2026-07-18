package com.govos.doc.storage.health;

import com.govos.doc.storage.port.StorageHealth;
import com.govos.doc.storage.port.StorageHealthStatus;
import com.govos.doc.storage.service.DocumentStorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StorageHealthIndicatorTest {

    @Mock private DocumentStorageService documentStorageService;
    @InjectMocks private StorageHealthIndicator storageHealthIndicator;

    @Test
    void shouldExposeProviderHealthStatus() {
        when(documentStorageService.health()).thenReturn(StorageHealth.up());

        assertThat(storageHealthIndicator.status()).isEqualTo(StorageHealthStatus.UP);
    }
}
