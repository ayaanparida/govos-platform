package com.govos.api.platform.controller;

import com.govos.api.common.response.ApiResponse;
import com.govos.api.common.response.ErrorResponse;
import com.govos.api.common.util.ApiConstants;
import com.govos.api.common.util.RequestContextUtils;
import com.govos.api.platform.response.BuildResponse;
import com.govos.api.platform.response.HealthResponse;
import com.govos.api.platform.response.ModuleResponse;
import com.govos.api.platform.response.PlatformInfoResponse;
import com.govos.api.platform.response.PlatformVersionResponse;
import com.govos.api.platform.service.PlatformApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(ApiConstants.BASE_PATH + "/platform")
@Tag(name = "Platform Administration", description = "Platform metadata, modules, build, and health")
@SecurityRequirement(name = "bearerAuth")
public class PlatformController {

    private final PlatformApplicationService platformApplicationService;

    public PlatformController(PlatformApplicationService platformApplicationService) {
        this.platformApplicationService = platformApplicationService;
    }

    @GetMapping("/info")
    @Operation(summary = "Platform information", description = "Runtime, build, database, and Flyway metadata.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Platform information",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "data": {
                                        "applicationName": "govos-api",
                                        "version": "0.1.0-SNAPSHOT",
                                        "environment": "local",
                                        "javaVersion": "21.0.11",
                                        "springBootVersion": "3.5.16",
                                        "database": "PostgreSQL",
                                        "flywayVersion": "1.7.0",
                                        "buildTime": "2026-07-17T18:00:00Z",
                                        "gitCommit": "c5af16c"
                                      }
                                    }
                                    """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ApiResponse<PlatformInfoResponse> info(HttpServletRequest request) {
        return ApiResponse.ok(
                platformApplicationService.getPlatformInfo(),
                null,
                RequestContextUtils.resolveRequestId(request));
    }

    @GetMapping("/version")
    @Operation(summary = "Platform version", description = "Semantic version and release metadata.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Platform version",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "data": {
                                        "version": "0.1.0-SNAPSHOT",
                                        "release": "0.1.0",
                                        "releaseDate": "2026-07-17"
                                      }
                                    }
                                    """)))
    })
    public ApiResponse<PlatformVersionResponse> version(HttpServletRequest request) {
        return ApiResponse.ok(
                platformApplicationService.getPlatformVersion(),
                null,
                RequestContextUtils.resolveRequestId(request));
    }

    @GetMapping("/modules")
    @Operation(summary = "Platform modules", description = "Registered GovOS modular monolith modules.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Module catalog",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "data": [
                                        {
                                          "moduleName": "govos-api",
                                          "version": "0.1.0-SNAPSHOT",
                                          "status": "ACTIVE"
                                        }
                                      ]
                                    }
                                    """)))
    })
    public ApiResponse<List<ModuleResponse>> modules(HttpServletRequest request) {
        return ApiResponse.ok(
                platformApplicationService.getModules(),
                null,
                RequestContextUtils.resolveRequestId(request));
    }

    @GetMapping("/build")
    @Operation(summary = "Build metadata", description = "Artifact, build number, and Git metadata from BuildProperties.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Build metadata",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "data": {
                                        "artifact": "govos-api",
                                        "buildNumber": "local",
                                        "gitBranch": "feature/platform-foundation",
                                        "gitCommit": "c5af16c",
                                        "buildTimestamp": "2026-07-17T18:00:00Z"
                                      }
                                    }
                                    """)))
    })
    public ApiResponse<BuildResponse> build(HttpServletRequest request) {
        return ApiResponse.ok(
                platformApplicationService.getBuild(),
                null,
                RequestContextUtils.resolveRequestId(request));
    }

    @GetMapping("/health")
    @Operation(
            summary = "Platform health summary",
            description = "Aggregated component health derived from Spring Boot Actuator HealthEndpoint.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Health summary",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "data": {
                                        "database": "UP",
                                        "redis": "NOT_CONFIGURED",
                                        "minio": "NOT_CONFIGURED",
                                        "opensearch": "NOT_CONFIGURED",
                                        "disk": "UP",
                                        "memory": "UP",
                                        "uptime": "2h 15m 30s"
                                      }
                                    }
                                    """)))
    })
    public ApiResponse<HealthResponse> health(HttpServletRequest request) {
        return ApiResponse.ok(
                platformApplicationService.getHealth(),
                null,
                RequestContextUtils.resolveRequestId(request));
    }
}
