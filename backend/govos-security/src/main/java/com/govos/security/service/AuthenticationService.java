package com.govos.security.service;

import com.govos.security.model.AuthenticationRequest;
import com.govos.security.model.AuthenticationResult;

/**
 * Credential authentication orchestration.
 */
public interface AuthenticationService {

    AuthenticationResult authenticate(AuthenticationRequest request);
}
