package com.dogukan.energy.exception;

import com.dogukan.energy.dto.response.ErrorResponse;
import com.dogukan.energy.entity.MeterStatus;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleBusiness_mapsInactiveMeterException_to422WithErrorCode() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/v1/readings");
        InactiveMeterException exception = new InactiveMeterException("SM-0001", MeterStatus.FAULTY);

        ResponseEntity<ErrorResponse> response = handler.handleBusiness(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("INACTIVE_METER");
        assertThat(response.getBody().path()).isEqualTo("/api/v1/readings");
        assertThat(response.getBody().fieldErrors()).isEmpty();
    }

    @Test
    void handleBusiness_mapsResourceNotFoundException_to404() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/v1/meters/42");
        ResourceNotFoundException exception = new ResourceNotFoundException("Meter", 42L);

        ResponseEntity<ErrorResponse> response = handler.handleBusiness(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().message()).contains("42");
    }
}
