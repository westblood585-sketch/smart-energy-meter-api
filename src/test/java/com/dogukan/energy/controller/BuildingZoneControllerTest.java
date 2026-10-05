package com.dogukan.energy.controller;

import com.dogukan.energy.dto.request.BuildingZoneRequest;
import com.dogukan.energy.dto.response.BuildingZoneResponse;
import com.dogukan.energy.exception.DuplicateResourceException;
import com.dogukan.energy.exception.ResourceNotFoundException;
import com.dogukan.energy.service.BuildingZoneService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BuildingZoneController.class)
class BuildingZoneControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private BuildingZoneService zoneService;

    @Test
    void create_returns201_whenRequestIsValid() throws Exception {
        BuildingZoneRequest request = new BuildingZoneRequest("Production Hall", "desc");
        when(zoneService.create(any())).thenReturn(new BuildingZoneResponse(1L, "Production Hall", "desc"));

        mockMvc.perform(post("/api/v1/zones")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void create_returns400_whenNameIsBlank() throws Exception {
        BuildingZoneRequest request = new BuildingZoneRequest(" ", "desc");

        mockMvc.perform(post("/api/v1/zones")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    @Test
    void create_returns409_whenNameAlreadyExists() throws Exception {
        BuildingZoneRequest request = new BuildingZoneRequest("Production Hall", "desc");
        when(zoneService.create(any())).thenThrow(new DuplicateResourceException("BuildingZone", "name", "Production Hall"));

        mockMvc.perform(post("/api/v1/zones")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_RESOURCE"));
    }

    @Test
    void getById_returns404_whenZoneMissing() throws Exception {
        when(zoneService.getById(eq(99L))).thenThrow(new ResourceNotFoundException("BuildingZone", 99L));

        mockMvc.perform(get("/api/v1/zones/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void delete_returns204_whenZoneExists() throws Exception {
        mockMvc.perform(delete("/api/v1/zones/1"))
                .andExpect(status().isNoContent());
    }
}
