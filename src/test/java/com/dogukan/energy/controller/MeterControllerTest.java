package com.dogukan.energy.controller;

import com.dogukan.energy.dto.request.MeterRequest;
import com.dogukan.energy.dto.response.MeterResponse;
import com.dogukan.energy.entity.MeterStatus;
import com.dogukan.energy.exception.DuplicateResourceException;
import com.dogukan.energy.exception.ResourceNotFoundException;
import com.dogukan.energy.service.MeterService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MeterController.class)
class MeterControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private MeterService meterService;

    @Test
    void create_returns201_whenRequestIsValid() throws Exception {
        MeterRequest request = new MeterRequest("SM-0001", MeterStatus.ACTIVE, 1L);
        when(meterService.create(any())).thenReturn(new MeterResponse(1L, "SM-0001", MeterStatus.ACTIVE, 1L, "Production Hall"));

        mockMvc.perform(post("/api/v1/meters")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.serialNumber").value("SM-0001"));
    }

    @Test
    void create_returns404_whenZoneDoesNotExist() throws Exception {
        MeterRequest request = new MeterRequest("SM-0002", MeterStatus.ACTIVE, 99L);
        when(meterService.create(any())).thenThrow(new ResourceNotFoundException("BuildingZone", 99L));

        mockMvc.perform(post("/api/v1/meters")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_returns409_whenSerialNumberAlreadyExists() throws Exception {
        MeterRequest request = new MeterRequest("SM-0001", MeterStatus.ACTIVE, 1L);
        when(meterService.create(any())).thenThrow(new DuplicateResourceException("Meter", "serialNumber", "SM-0001"));

        mockMvc.perform(post("/api/v1/meters")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }
}
