package com.dogukan.energy.controller;

import com.dogukan.energy.dto.request.TariffRequest;
import com.dogukan.energy.dto.response.TariffResponse;
import com.dogukan.energy.service.TariffService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TariffController.class)
class TariffControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private TariffService tariffService;

    @Test
    void create_returns201_whenRequestIsValid() throws Exception {
        TariffRequest request = new TariffRequest(
                "Night Tariff", new BigDecimal("1.8500"),
                LocalDate.of(2026, 1, 1), null,
                LocalTime.of(22, 0), LocalTime.of(6, 0), true);
        when(tariffService.create(any())).thenReturn(new TariffResponse(
                1L, "Night Tariff", new BigDecimal("1.8500"),
                LocalDate.of(2026, 1, 1), null, LocalTime.of(22, 0), LocalTime.of(6, 0), true));

        mockMvc.perform(post("/api/v1/tariffs")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Night Tariff"));
    }

    @Test
    void create_returns400_whenValidToIsBeforeValidFrom() throws Exception {
        TariffRequest request = new TariffRequest(
                "Broken Tariff", new BigDecimal("1.0000"),
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 1, 1),
                LocalTime.of(0, 0), LocalTime.of(0, 0), true);

        mockMvc.perform(post("/api/v1/tariffs")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }
}
