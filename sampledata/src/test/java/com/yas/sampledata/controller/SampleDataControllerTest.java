package com.yas.sampledata.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.yas.sampledata.service.SampleDataService;
import com.yas.sampledata.viewmodel.SampleDataVm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ObjectWriter;

@WebMvcTest(controllers = SampleDataController.class,
    excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class SampleDataControllerTest {

    private static final String SAMPLE_DATA_URL = "/storefront/sampledata";

    @MockitoBean
    private SampleDataService sampleDataService;

    @Autowired
    private MockMvc mockMvc;

    private ObjectWriter objectWriter;

    @BeforeEach
    void setUp() {
        objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
    }

    @Test
    void createSampleData_WhenRequestIsValid_ShouldReturnSuccess() throws Exception {
        SampleDataVm requestVm = new SampleDataVm("test");
        SampleDataVm responseVm = new SampleDataVm("Insert Sample Data successfully!");

        given(sampleDataService.createSampleData()).willReturn(responseVm);

        String request = objectWriter.writeValueAsString(requestVm);

        mockMvc.perform(post(SAMPLE_DATA_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Insert Sample Data successfully!"));
    }

    @Test
    void createSampleData_WhenServiceReturnsMessage_ShouldReturnCorrectMessage() throws Exception {
        SampleDataVm requestVm = new SampleDataVm("request");
        SampleDataVm responseVm = new SampleDataVm("Custom success message");

        given(sampleDataService.createSampleData()).willReturn(responseVm);

        String request = objectWriter.writeValueAsString(requestVm);

        mockMvc.perform(post(SAMPLE_DATA_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Custom success message"));
    }
}
