package com.yas.tax.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
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
import com.yas.tax.service.TaxClassService;
import com.yas.tax.viewmodel.taxclass.TaxClassListGetVm;
import com.yas.tax.viewmodel.taxclass.TaxClassPostVm;
import com.yas.tax.viewmodel.taxclass.TaxClassVm;
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

@WebMvcTest(controllers = TaxClassController.class,
    excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class TaxClassControllerTest {

    @MockitoBean
    private TaxClassService taxClassService;

    @Autowired
    private MockMvc mockMvc;

    private ObjectWriter objectWriter;

    @BeforeEach
    void setUp() {
        objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
    }

    @Test
    void listTaxClasses_WhenTaxClassesExist_ShouldReturnOk() throws Exception {
        TaxClassVm taxClassVm = new TaxClassVm(1L, "Standard Tax");
        given(taxClassService.findAllTaxClasses()).willReturn(List.of(taxClassVm));

        mockMvc.perform(get(ApiConstant.TAX_CLASS_URL))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].name").value("Standard Tax"));
    }

    @Test
    void getTaxClass_WhenTaxClassExists_ShouldReturnOk() throws Exception {
        TaxClassVm taxClassVm = new TaxClassVm(1L, "Standard Tax");
        given(taxClassService.findById(1L)).willReturn(taxClassVm);

        mockMvc.perform(get(ApiConstant.TAX_CLASS_URL + "/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Standard Tax"));
    }

    @Test
    void getTaxClass_WhenTaxClassNotFound_ShouldReturnNotFound() throws Exception {
        given(taxClassService.findById(anyLong())).willThrow(new NotFoundException("TAX_CLASS_NOT_FOUND", 999L));

        mockMvc.perform(get(ApiConstant.TAX_CLASS_URL + "/999"))
            .andExpect(status().isNotFound());
    }

    @Test
    void createTaxClass_WhenRequestIsValid_ShouldReturnCreated() throws Exception {
        TaxClassPostVm postVm = new TaxClassPostVm("1", "New Tax Class");
        TaxClass taxClass = TaxClass.builder().id(1L).name("New Tax Class").build();
        
        given(taxClassService.create(any(TaxClassPostVm.class))).willReturn(taxClass);

        String request = objectWriter.writeValueAsString(postVm);

        mockMvc.perform(post(ApiConstant.TAX_CLASS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("New Tax Class"));
    }

    @Test
    void createTaxClass_WhenIdIsBlank_ShouldReturnBadRequest() throws Exception {
        TaxClassPostVm postVm = new TaxClassPostVm("", "New Tax Class");

        String request = objectWriter.writeValueAsString(postVm);

        mockMvc.perform(post(ApiConstant.TAX_CLASS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isBadRequest());
    }

    @Test
    void updateTaxClass_WhenRequestIsValid_ShouldReturnNoContent() throws Exception {
        TaxClassPostVm postVm = new TaxClassPostVm("1", "Updated Tax Class");
        doNothing().when(taxClassService).update(any(TaxClassPostVm.class), anyLong());

        String request = objectWriter.writeValueAsString(postVm);

        mockMvc.perform(put(ApiConstant.TAX_CLASS_URL + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isNoContent());
    }

    @Test
    void updateTaxClass_WhenTaxClassNotFound_ShouldReturnNotFound() throws Exception {
        TaxClassPostVm postVm = new TaxClassPostVm("1", "Updated Tax Class");
        doThrow(new NotFoundException("TAX_CLASS_NOT_FOUND", 999L))
            .when(taxClassService).update(any(TaxClassPostVm.class), anyLong());

        String request = objectWriter.writeValueAsString(postVm);

        mockMvc.perform(put(ApiConstant.TAX_CLASS_URL + "/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isNotFound());
    }

    @Test
    void deleteTaxClass_WhenTaxClassExists_ShouldReturnNoContent() throws Exception {
        doNothing().when(taxClassService).delete(anyLong());

        mockMvc.perform(delete(ApiConstant.TAX_CLASS_URL + "/1"))
            .andExpect(status().isNoContent());
    }

    @Test
    void deleteTaxClass_WhenTaxClassNotFound_ShouldReturnNotFound() throws Exception {
        doThrow(new NotFoundException("TAX_CLASS_NOT_FOUND", 999L))
            .when(taxClassService).delete(anyLong());

        mockMvc.perform(delete(ApiConstant.TAX_CLASS_URL + "/999"))
            .andExpect(status().isNotFound());
    }

    @Test
    void getPageableTaxClasses_WhenTaxClassesExist_ShouldReturnOk() throws Exception {
        TaxClassVm taxClassVm = new TaxClassVm(1L, "Standard Tax");
        TaxClassListGetVm listVm = new TaxClassListGetVm(List.of(taxClassVm), 0, 10, 1, 1, true);
        given(taxClassService.getPageableTaxClasses(anyInt(), anyInt())).willReturn(listVm);

        mockMvc.perform(get(ApiConstant.TAX_CLASS_URL + "/paging")
                .param("pageNo", "0")
                .param("pageSize", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.taxClassContent[0].id").value(1))
            .andExpect(jsonPath("$.totalElements").value(1));
    }
}
