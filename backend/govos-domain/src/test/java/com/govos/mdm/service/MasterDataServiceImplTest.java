package com.govos.mdm.service;

import com.govos.mdm.dto.CreateMasterDataRequest;
import com.govos.mdm.dto.MasterDataDto;
import com.govos.mdm.dto.UpdateMasterDataRequest;
import com.govos.mdm.entity.MasterData;
import com.govos.mdm.exception.MasterDataNotFoundException;
import com.govos.mdm.exception.SystemDefinedMasterDataException;
import com.govos.mdm.mapper.MasterDataMapper;
import com.govos.mdm.repository.MasterDataRepository;
import com.govos.mdm.validator.MasterDataValidator;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MasterDataServiceImplTest {

    @Mock
    private MasterDataRepository masterDataRepository;

    @Mock
    private MasterDataMapper masterDataMapper;

    @Mock
    private MasterDataValidator masterDataValidator;

    @InjectMocks
    private MasterDataServiceImpl masterDataService;

    @Test
    void shouldReturnDtoWhenGetByIdFound() {
        UUID id = UUID.randomUUID();
        MasterData entity = masterData(id, "GENDER", "M", false);
        MasterDataDto dto = dto(entity);

        when(masterDataRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(entity));
        when(masterDataMapper.toDto(entity)).thenReturn(dto);

        assertThat(masterDataService.getById(id)).isEqualTo(dto);
    }

    @Test
    void shouldThrowWhenGetByIdNotFound() {
        UUID id = UUID.randomUUID();
        when(masterDataRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> masterDataService.getById(id))
                .isInstanceOf(MasterDataNotFoundException.class);
    }

    @Test
    void shouldReturnDtoWhenGetByTypeAndKeyFound() {
        MasterData entity = masterData(UUID.randomUUID(), "GENDER", "M", false);
        MasterDataDto dto = dto(entity);

        when(masterDataRepository.findByTypeAndKeyAndDeletedFalse("GENDER", "M"))
                .thenReturn(Optional.of(entity));
        when(masterDataMapper.toDto(entity)).thenReturn(dto);

        assertThat(masterDataService.getByTypeAndKey("GENDER", "M")).isEqualTo(dto);
    }

    @Test
    void shouldReturnListWhenGetByType() {
        MasterData entity = masterData(UUID.randomUUID(), "GENDER", "M", false);
        MasterDataDto dto = dto(entity);

        when(masterDataRepository.findByTypeAndDeletedFalseOrderByDisplayOrderAsc("GENDER"))
                .thenReturn(List.of(entity));
        when(masterDataMapper.toDto(entity)).thenReturn(dto);

        assertThat(masterDataService.getByType("GENDER")).containsExactly(dto);
    }

    @Test
    void shouldCreateAndReturnDto() {
        CreateMasterDataRequest request = new CreateMasterDataRequest(
                "GENDER-M", "GENDER", "M", "Male", "Male gender", 1, false, true);
        MasterData entity = masterData(UUID.randomUUID(), "GENDER", "M", false);
        MasterDataDto dto = dto(entity);

        when(masterDataMapper.toEntity(request)).thenReturn(entity);
        when(masterDataRepository.save(entity)).thenReturn(entity);
        when(masterDataMapper.toDto(entity)).thenReturn(dto);

        assertThat(masterDataService.create(request)).isEqualTo(dto);
        verify(masterDataValidator).validateCreate(request);
        assertThat(entity.getActive()).isTrue();
        assertThat(entity.getSystemDefined()).isFalse();
        assertThat(entity.getDeleted()).isFalse();
    }

    @Test
    void shouldUpdateAndReturnDto() {
        UUID id = UUID.randomUUID();
        MasterData entity = masterData(id, "GENDER", "M", false);
        entity.setVersion(0L);
        UpdateMasterDataRequest request = new UpdateMasterDataRequest(
                "GENDER-M", "GENDER", "M", "Male updated", "desc", 2, false, true, 0L);
        MasterDataDto dto = dto(entity);

        when(masterDataRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(entity));
        when(masterDataRepository.save(entity)).thenReturn(entity);
        when(masterDataMapper.toDto(entity)).thenReturn(dto);

        assertThat(masterDataService.update(id, request)).isEqualTo(dto);
        verify(masterDataValidator).validateUpdate(id, request);
        verify(masterDataMapper).updateEntity(request, entity);
    }

    @Test
    void shouldThrowWhenUpdateSystemDefined() {
        UUID id = UUID.randomUUID();
        MasterData entity = masterData(id, "GENDER", "M", true);
        UpdateMasterDataRequest request = new UpdateMasterDataRequest(
                "GENDER-M", "GENDER", "M", "Male", "desc", 1, true, true, 0L);

        when(masterDataRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> masterDataService.update(id, request))
                .isInstanceOf(SystemDefinedMasterDataException.class);
        verify(masterDataValidator, never()).validateUpdate(eq(id), any());
    }

    @Test
    void shouldThrowWhenUpdateVersionMismatch() {
        UUID id = UUID.randomUUID();
        MasterData entity = masterData(id, "GENDER", "M", false);
        entity.setVersion(1L);
        UpdateMasterDataRequest request = new UpdateMasterDataRequest(
                "GENDER-M", "GENDER", "M", "Male", "desc", 1, false, true, 0L);

        when(masterDataRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> masterDataService.update(id, request))
                .isInstanceOf(OptimisticLockException.class);
    }

    @Test
    void shouldSoftDelete() {
        UUID id = UUID.randomUUID();
        MasterData entity = masterData(id, "GENDER", "M", false);

        when(masterDataRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(entity));

        masterDataService.softDelete(id);

        ArgumentCaptor<MasterData> captor = ArgumentCaptor.forClass(MasterData.class);
        verify(masterDataRepository).save(captor.capture());
        assertThat(captor.getValue().getDeleted()).isTrue();
        assertThat(captor.getValue().getActive()).isFalse();
    }

    @Test
    void shouldThrowWhenSoftDeleteSystemDefined() {
        UUID id = UUID.randomUUID();
        MasterData entity = masterData(id, "GENDER", "M", true);

        when(masterDataRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> masterDataService.softDelete(id))
                .isInstanceOf(SystemDefinedMasterDataException.class);
        verify(masterDataRepository, never()).save(any());
    }

    private MasterData masterData(UUID id, String type, String key, boolean systemDefined) {
        MasterData entity = new MasterData();
        entity.setId(id);
        entity.setType(type);
        entity.setKey(key);
        entity.setValue("Value");
        entity.setSystemDefined(systemDefined);
        entity.setActive(true);
        entity.setDeleted(false);
        return entity;
    }

    private MasterDataDto dto(MasterData entity) {
        return new MasterDataDto(
                entity.getId(),
                entity.getCode(),
                entity.getType(),
                entity.getKey(),
                entity.getValue(),
                entity.getDescription(),
                entity.getDisplayOrder(),
                entity.getSystemDefined(),
                entity.getActive(),
                entity.getVersion(),
                "system",
                Instant.now(),
                "system",
                Instant.now());
    }
}
