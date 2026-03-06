package com.yas.tax.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.tax.constants.ApiConstant;
import com.yas.tax.model.TaxClass;
import com.yas.tax.model.TaxRate;
import com.yas.tax.service.TaxRateService;
import com.yas.tax.viewmodel.taxrate.TaxRatePostVm;
import com.yas.tax.viewmodel.taxrate.TaxRateVm;
import java.util.List;
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

@WebMvcTest(controllers = TaxRateController.class,
    excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class TaxRateControllerTest {

    @MockitoBean
    private TaxRateService taxRateService;

    @Autowired
    private MockMvc mockMvc;

    private ObjectWriter objectWriter;
    private TaxClass taxClass;
    private TaxRate taxRate;

    @BeforeEach
    void setUp() {
        objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
        taxClass = TaxClass.builder().id(1L).name("Standard Tax").build();
        taxRate = TaxRate.builder()
            .id(1L)
            .rate(10.0)
            .zipCode("12345")
            .taxClass(taxClass)
            .stateOrProvinceId(1L)
            .countryId(1L)
            .build();
    }

    @Test
    void getTaxRate_WhenTaxRateExists_ShouldReturnOk() throws Exception {
        TaxRateVm taxRateVm = TaxRateVm.fromModel(taxRate);
        given(taxRateService.findById(1L)).willReturn(taxRateVm);

        mockMvc.perform(get(ApiConstant.TAX_RATE_URL + "/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.rate").value(10.0));
    }

    @Test
    void getTaxRate_WhenTaxRateNotFound_ShouldReturnNotFound() throws Exception {
        given(taxRateService.findById(anyLong())).willThrow(new NotFoundException("TAX_RATE_NOT_FOUND", 999L));

        mockMvc.perform(get(ApiConstant.TAX_RATE_URL + "/999"))
            .andExpect(status().isNotFound());
    }

    @Test
    void createTaxRate_WhenRequestIsValid_ShouldReturnCreated() throws Exception {
        TaxRatePostVm postVm = new TaxRatePostVm(10.0, "12345", 1L, 1L, 1L);
        given(taxRateService.createTaxRate(any(TaxRatePostVm.class))).willReturn(taxRate);

        String request = objectWriter.writeValueAsString(postVm);

        mockMvc.perform(post(ApiConstant.TAX_RATE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.rate").value(10.0));
    }

    @Test
    void createTaxRate_WhenRateIsNull_ShouldReturnBadRequest() throws Exception {
        TaxRatePostVm postVm = new TaxRatePostVm(null, "12345", 1L, 1L, 1L);

        String request = objectWriter.writeValueAsString(postVm);

        mockMvc.perform(post(ApiConstant.TAX_RATE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isBadRequest());
    }

    @Test
    void createTaxRate_WhenTaxClassIdIsNull_ShouldReturnBadRequest() throws Exception {
        TaxRatePostVm postVm = new TaxRatePostVm(10.0, "12345", null, 1L, 1L);

        String request = objectWriter.writeValueAsString(postVm);

        mockMvc.perform(post(ApiConstant.TAX_RATE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isBadRequest());
    }

    @Test
    void createTaxRate_WhenCountryIdIsNull_ShouldReturnBadRequest() throws Exception {
        TaxRatePostVm postVm = new TaxRatePostVm(10.0, "12345", 1L, 1L, null);

        String request = objectWriter.writeValueAsString(postVm);

        mockMvc.perform(post(ApiConstant.TAX_RATE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isBadRequest());
    }

    @Test
    void updateTaxRate_WhenRequestIsValid_ShouldReturnNoContent() throws Exception {
        TaxRatePostVm postVm = new TaxRatePostVm(15.0, "54321", 1L, 2L, 2L);
        doNothing().when(taxRateService).updateTaxRate(any(TaxRatePostVm.class), anyLong());

        String request = objectWriter.writeValueAsString(postVm);

        mockMvc.perform(put(ApiConstant.TAX_RATE_URL + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isNoContent());
    }

    @Test
    void updateTaxRate_WhenTaxRateNotFound_ShouldReturnNotFound() throws Exception {
        TaxRatePostVm postVm = new TaxRatePostVm(15.0, "54321", 1L, 2L, 2L);
        doThrow(new NotFoundException("TAX_RATE_NOT_FOUND", 999L))
            .when(taxRateService).updateTaxRate(any(TaxRatePostVm.class), anyLong());

        String request = objectWriter.writeValueAsString(postVm);

        mockMvc.perform(put(ApiConstant.TAX_RATE_URL + "/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isNotFound());
    }

    @Test
    void deleteTaxRate_WhenTaxRateExists_ShouldReturnNoContent() throws Exception {
        doNothing().when(taxRateService).delete(anyLong());

        mockMvc.perform(delete(ApiConstant.TAX_RATE_URL + "/1"))
            .andExpect(status().isNoContent());
    }

    @Test
    void deleteTaxRate_WhenTaxRateNotFound_ShouldReturnNotFound() throws Exception {
        doThrow(new NotFoundException("TAX_RATE_NOT_FOUND", 999L))
            .when(taxRateService).delete(anyLong());

        mockMvc.perform(delete(ApiConstant.TAX_RATE_URL + "/999"))
            .andExpect(status().isNotFound());
    }

    @Test
    void getTaxPercentByAddress_WhenValidRequest_ShouldReturnOk() throws Exception {
        given(taxRateService.getTaxPercent(anyLong(), anyLong(), anyLong(), anyString())).willReturn(10.0);

        mockMvc.perform(get(ApiConstant.TAX_RATE_URL + "/tax-percent")
                .param("taxClassId", "1")
                .param("countryId", "1")
                .param("stateOrProvinceId", "1")
                .param("zipCode", "12345"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").value(10.0));
    }

    @Test
    void getBatchTaxPercentsByAddress_WhenValidRequest_ShouldReturnOk() throws Exception {
        TaxRateVm taxRateVm = TaxRateVm.fromModel(taxRate);
        given(taxRateService.getBulkTaxRate(anyList(), anyLong(), anyLong(), anyString()))
            .willReturn(List.of(taxRateVm));

        mockMvc.perform(get(ApiConstant.TAX_RATE_URL + "/location-based-batch")
                .param("taxClassIds", "1", "2")
                .param("countryId", "1")
                .param("stateOrProvinceId", "1")
                .param("zipCode", "12345"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].rate").value(10.0));
    }
}
