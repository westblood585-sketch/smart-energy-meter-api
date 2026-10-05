package com.dogukan.energy.controller;

import com.dogukan.energy.dto.request.ConsumptionReadingRequest;
import com.dogukan.energy.dto.response.ConsumptionReadingResponse;
import com.dogukan.energy.entity.MeterStatus;
import com.dogukan.energy.exception.AnomalousReadingException;
import com.dogukan.energy.exception.InactiveMeterException;
import com.dogukan.energy.service.ConsumptionReadingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ConsumptionReadingController.class)
class ConsumptionReadingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ConsumptionReadingService readingService;

    @Test
    void submit_returns400_whenConsumptionIsMissing() throws Exception {
        String body = """
                {"meterId": 1, "readingTime": "2026-09-28T10:00:00"}
                """;

        mockMvc.perform(post("/api/v1/readings")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("consumptionKwh"));
    }

    @Test
    void submit_returns422_whenMeterIsInactive() throws Exception {
        ConsumptionReadingRequest request = new ConsumptionReadingRequest(1L, LocalDateTime.of(2026, 9, 28, 10, 0), new BigDecimal("5"));
        when(readingService.submit(any())).thenThrow(new InactiveMeterException("SM-0001", MeterStatus.INACTIVE));

        mockMvc.perform(post("/api/v1/readings")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("INACTIVE_METER"));
    }

    @Test
    void submit_returns422_whenReadingIsAnomalous() throws Exception {
        ConsumptionReadingRequest request = new ConsumptionReadingRequest(1L, LocalDateTime.of(2026, 9, 28, 10, 0), new BigDecimal("300"));
        when(readingService.submit(any()))
                .thenThrow(new AnomalousReadingException("SM-0001", new BigDecimal("90"), new BigDecimal("300"), 3));

        mockMvc.perform(post("/api/v1/readings")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("ANOMALOUS_READING"));
    }

    @Test
    void submit_returns201_whenRequestIsValid() throws Exception {
        ConsumptionReadingRequest request = new ConsumptionReadingRequest(1L, LocalDateTime.of(2026, 9, 28, 10, 0), new BigDecimal("25"));
        ConsumptionReadingResponse response = new ConsumptionReadingResponse(
                1L, "SM-0001", "Day Tariff", new BigDecimal("2.0000"),
                request.readingTime(), request.consumptionKwh(), new BigDecimal("50.00"));
        when(readingService.submit(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/readings")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalCost").value(50.00));
    }
}
