package com.vms.common;

import com.vms.common.exception.BadRequestException;
import com.vms.common.exception.ConflictException;
import com.vms.common.exception.ForbiddenException;
import com.vms.common.exception.NotFoundException;
import com.vms.common.security.ApiAccessDeniedHandler;
import com.vms.common.security.ApiAuthenticationEntryPoint;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @RestController
    @RequestMapping("/test")
    static class TestController {

        @GetMapping("/not-found")
        public void notFound() {
            throw new NotFoundException("User not found");
        }

        @GetMapping("/forbidden")
        public void forbidden() {
            throw new ForbiddenException(
                    "You cannot access this resource"
            );
        }

        @GetMapping("/conflict")
        public void conflict() {
            throw new ConflictException("Email already exists");
        }

        @GetMapping("/bad-request")
        public void badRequest() {
            throw new BadRequestException(
                    "Invalid state transition"
            );
        }

        @PostMapping("/validation")
        public void validation(
                @Valid @RequestBody TestRequest request
        ) {
        }
    }

    record TestRequest(
            @NotBlank(message = "Name is required")
            String name
    ) {
    }

    @Test
    void shouldHandleNotFound() throws Exception {
        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("User not found"))
                .andExpect(jsonPath("$.path")
                        .value("/test/not-found"));
    }

    @Test
    void shouldHandleForbidden() throws Exception {
        mockMvc.perform(get("/test/forbidden"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message")
                        .value("You cannot access this resource"))
                .andExpect(jsonPath("$.path")
                        .value("/test/forbidden"));
    }

    @Test
    void shouldHandleConflict() throws Exception {
        mockMvc.perform(get("/test/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message")
                        .value("Email already exists"))
                .andExpect(jsonPath("$.path")
                        .value("/test/conflict"));
    }

    @Test
    void shouldHandleBadRequest() throws Exception {
        mockMvc.perform(get("/test/bad-request"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Invalid state transition"))
                .andExpect(jsonPath("$.path")
                        .value("/test/bad-request"));
    }

    @Test
    void shouldHandleValidationErrors() throws Exception {

        String invalidJson = """
                {
                    "name": ""
                }
                """;

        mockMvc.perform(
                        post("/test/validation")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(invalidJson)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Validation failed"))
                .andExpect(jsonPath("$.path")
                        .value("/test/validation"))
                .andExpect(jsonPath("$.fieldErrors").exists())
                .andExpect(jsonPath("$.fieldErrors.name")
                        .value("Name is required"));
    }

    @Test
    void shouldReturnUnauthorizedApiError() throws Exception {

        ApiAuthenticationEntryPoint entryPoint =
                new ApiAuthenticationEntryPoint(new ObjectMapper());

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setRequestURI("/api/users");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        entryPoint.commence(
                request,
                response,
                new BadCredentialsException("Bad credentials")
        );

        String responseBody = response.getContentAsString();

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(responseBody)
                .contains("\"status\":401");
        assertThat(responseBody)
                .contains("\"message\":\"Invalid credentials\"");
        assertThat(responseBody)
                .contains("\"path\":\"/api/users\"");
    }

    @Test
    void shouldReturnForbiddenApiError() throws Exception {

        ApiAccessDeniedHandler handler =
                new ApiAccessDeniedHandler(new ObjectMapper());

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setRequestURI("/api/admin");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        handler.handle(
                request,
                response,
                new AccessDeniedException("Forbidden")
        );

        String responseBody = response.getContentAsString();

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(responseBody)
                .contains("\"status\":403");
        assertThat(responseBody)
                .contains("\"message\":\"Access denied\"");
        assertThat(responseBody)
                .contains("\"path\":\"/api/admin\"");
    }
}