package com.govos.org.service;

import com.govos.idm.entity.User;
import com.govos.idm.exception.UserNotFoundException;
import com.govos.idm.repository.UserRepository;
import com.govos.org.dto.AssignUserOrganizationRequest;
import com.govos.org.dto.UserOrganizationDto;
import com.govos.org.entity.Organization;
import com.govos.org.entity.UserOrganization;
import com.govos.org.exception.DuplicateAssignmentException;
import com.govos.org.exception.OrganizationNotFoundException;
import com.govos.org.exception.UserOrganizationNotFoundException;
import com.govos.org.mapper.UserOrganizationMapper;
import com.govos.org.repository.OrganizationRepository;
import com.govos.org.repository.UserOrganizationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class UserOrganizationServiceImpl implements UserOrganizationService {

    private final UserOrganizationRepository userOrganizationRepository;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final UserOrganizationMapper userOrganizationMapper;

    public UserOrganizationServiceImpl(
            UserOrganizationRepository userOrganizationRepository,
            UserRepository userRepository,
            OrganizationRepository organizationRepository,
            UserOrganizationMapper userOrganizationMapper) {
        this.userOrganizationRepository = userOrganizationRepository;
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.userOrganizationMapper = userOrganizationMapper;
    }

    @Override
    public UserOrganizationDto getById(UUID id) {
        return userOrganizationMapper.toDto(findActiveById(id));
    }

    @Override
    public List<UserOrganizationDto> getByUserId(UUID userId) {
        return userOrganizationRepository.findByUser_IdAndDeletedFalse(userId).stream()
                .map(userOrganizationMapper::toDto)
                .toList();
    }

    @Override
    public UserOrganizationDto getDefaultByUserId(UUID userId) {
        return userOrganizationRepository.findByUser_IdAndDefaultOrganizationTrueAndDeletedFalse(userId)
                .map(userOrganizationMapper::toDto)
                .orElseThrow(() -> new UserOrganizationNotFoundException(userId));
    }

    @Override
    @Transactional
    public UserOrganizationDto assign(AssignUserOrganizationRequest request) {
        if (userOrganizationRepository.existsByUser_IdAndOrganization_IdAndDeletedFalse(
                request.userId(), request.organizationId())) {
            throw new DuplicateAssignmentException(
                    "User already assigned to organization userId=" + request.userId()
                            + ", organizationId=" + request.organizationId());
        }

        User user = userRepository.findByIdAndDeletedFalse(request.userId())
                .orElseThrow(() -> new UserNotFoundException(request.userId()));
        Organization organization = organizationRepository.findByIdAndDeletedFalse(request.organizationId())
                .orElseThrow(() -> new OrganizationNotFoundException(request.organizationId()));

        boolean defaultOrg = Boolean.TRUE.equals(request.defaultOrganization());
        if (defaultOrg) {
            clearDefaultForUser(request.userId());
        }

        UserOrganization entity = new UserOrganization();
        entity.setUser(user);
        entity.setOrganization(organization);
        entity.setDefaultOrganization(defaultOrg);
        entity.setActive(request.active() != null ? request.active() : true);
        entity.setDeleted(false);

        return userOrganizationMapper.toDto(userOrganizationRepository.save(entity));
    }

    @Override
    @Transactional
    public void revoke(UUID id) {
        UserOrganization entity = findActiveById(id);
        entity.setDeleted(true);
        entity.setActive(false);
        entity.setDefaultOrganization(false);
        userOrganizationRepository.save(entity);
    }

    @Override
    @Transactional
    public UserOrganizationDto setDefault(UUID id) {
        UserOrganization entity = findActiveById(id);
        clearDefaultForUser(entity.getUser().getId());
        entity.setDefaultOrganization(true);
        return userOrganizationMapper.toDto(userOrganizationRepository.save(entity));
    }

    private void clearDefaultForUser(UUID userId) {
        userOrganizationRepository.findByUser_IdAndDefaultOrganizationTrueAndDeletedFalse(userId)
                .ifPresent(existing -> {
                    existing.setDefaultOrganization(false);
                    userOrganizationRepository.save(existing);
                });
    }

    private UserOrganization findActiveById(UUID id) {
        return userOrganizationRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new UserOrganizationNotFoundException(id));
    }
}
