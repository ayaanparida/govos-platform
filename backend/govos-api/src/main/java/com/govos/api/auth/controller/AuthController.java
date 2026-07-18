package com.govos.api.auth.controller;

import com.govos.api.auth.request.LoginRequest;
import com.govos.api.auth.request.LogoutRequest;
import com.govos.api.auth.request.RefreshTokenRequest;
import com.govos.api.auth.response.CurrentUserResponse;
import com.govos.api.auth.response.LoginResponse;
import com.govos.api.auth.response.LogoutResponse;
import com.govos.api.auth.service.AuthApplicationService;
import com.govos.api.common.response.ApiResponse;
import com.govos.api.common.response.ErrorResponse;
import com.govos.api.common.util.ApiConstants;
import com.govos.api.common.util.RequestContextUtils;
import com.govos.security.annotation.CurrentUser;
import com.govos.security.jwt.JwtPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiConstants.BASE_PATH + "/auth")
@Tag(name = "Authentication", description = "Platform authentication and session management")
public class AuthController {

    private final AuthApplicationService authApplicationService;

    public AuthController(AuthApplicationService authApplicationService) {
        this.authApplicationService = authApplicationService;
    }

    @PostMapping("/login")
    @Operation(
            summary = "Authenticate user",
            description = "Validates credentials and issues JWT access and opaque refresh tokens.",
            security = {})
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Login successful",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "data": {
                                        "accessToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhMWIyYzNkNC1lNWY2LTc4OTAtYWJjZC1lZjEyMzQ1Njc4OTAiLCJ1c2VybmFtZSI6Impkb2UifQ.signature",
                                        "refreshToken": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
                                        "expiresIn": 900,
                                        "tokenType": "Bearer",
                                        "user": {
                                          "userId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                                          "username": "jdoe",
                                          "email": "john.doe@gov.example",
                                          "roles": ["OFFICER"],
                                          "permissions": ["idm:user:read"]
                                        }
                                      },
                                      "timestamp": "2026-07-17T17:30:00Z",
                                      "requestId": "req-123"
                                    }
                                    """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Invalid credentials",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Account disabled or locked",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "422",
                    description = "Password policy violation",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ApiResponse<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        LoginResponse response = authApplicationService.login(request, httpRequest);
        return ApiResponse.ok(response, null, RequestContextUtils.resolveRequestId(httpRequest));
    }

    @PostMapping("/refresh")
    @Operation(
            summary = "Refresh access token",
            description = "Issues a new access token from a valid refresh token and rotates the refresh token.",
            security = {})
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Token refreshed",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "data": {
                                        "accessToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhMWIyYzNkNC1lNWY2LTc4OTAtYWJjZC1lZjEyMzQ1Njc4OTAiLCJ1c2VybmFtZSI6Impkb2UifQ.signature",
                                        "refreshToken": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
                                        "expiresIn": 900,
                                        "tokenType": "Bearer",
                                        "user": {
                                          "userId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                                          "username": "jdoe",
                                          "email": "john.doe@gov.example",
                                          "roles": ["OFFICER"],
                                          "permissions": ["idm:user:read"]
                                        }
                                      },
                                      "requestId": "req-456"
                                    }
                                    """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Invalid or expired refresh token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ApiResponse<LoginResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest) {
        LoginResponse response = authApplicationService.refresh(request);
        return ApiResponse.ok(response, null, RequestContextUtils.resolveRequestId(httpRequest));
    }

    @PostMapping("/logout")
    @Operation(
            summary = "Logout session",
            description = "Revokes the refresh token and closes the login session.",
            security = {})
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Logout successful",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "data": {
                                        "message": "Session revoked successfully"
                                      },
                                      "requestId": "req-789"
                                    }
                                    """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Invalid refresh token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ApiResponse<LogoutResponse> logout(
            @Valid @RequestBody LogoutRequest request,
            HttpServletRequest httpRequest) {
        LogoutResponse response = authApplicationService.logout(request);
        return ApiResponse.ok(response, null, RequestContextUtils.resolveRequestId(httpRequest));
    }

    @GetMapping("/me")
    @Operation(
            summary = "Current authenticated user",
            description = "Returns the authenticated user profile and effective authorities from the JWT access token.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Current user profile",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "data": {
                                        "authenticated": true,
                                        "userId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                                        "username": "jdoe",
                                        "email": "john.doe@gov.example",
                                        "roles": ["OFFICER"],
                                        "permissions": ["idm:user:read", "idm:user:write"]
                                      },
                                      "requestId": "req-321"
                                    }
                                    """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Missing or invalid JWT access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Authenticated principal unavailable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ApiResponse<CurrentUserResponse> currentUser(
            @CurrentUser JwtPrincipal user,
            HttpServletRequest httpRequest) {
        CurrentUserResponse response = authApplicationService.currentUser(user);
        return ApiResponse.ok(response, null, RequestContextUtils.resolveRequestId(httpRequest));
    }
}
