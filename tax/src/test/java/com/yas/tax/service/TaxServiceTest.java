package com.yas.tax.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.tax.model.TaxClass;
import com.yas.tax.model.TaxRate;
import com.yas.tax.repository.TaxClassRepository;
import com.yas.tax.repository.TaxRateRepository;
import com.yas.tax.viewmodel.taxrate.TaxRatePostVm;
import com.yas.tax.viewmodel.taxrate.TaxRateVm;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(classes = TaxRateService.class)
public class TaxServiceTest {
    @MockitoBean
    TaxRateRepository taxRateRepository;
    @MockitoBean
    LocationService locationService;
    @MockitoBean
    TaxClassRepository taxClassRepository;

    @Autowired
    TaxRateService taxRateService;

    TaxRate taxRate;
    TaxClass taxClass;

    @BeforeEach
    void setUp() {
        taxClass = TaxClass.builder().id(1L).name("Standard Tax").build();
        taxRate = TaxRate.builder()
            .id(1L)
            .rate(10.0)
            .zipCode("12345")
            .taxClass(taxClass)
            .stateOrProvinceId(1L)
            .countryId(1L)
            .build();
        lenient().when(taxRateRepository.findAll()).thenReturn(List.of(taxRate));
    }

    @Test
    void testFindAll_shouldReturnAllTaxRate() {
        List<TaxRateVm> result = taxRateService.findAll();
        assertThat(result).hasSize(1).contains(TaxRateVm.fromModel(taxRate));
    }

    @Test
    void testFindById_WhenTaxRateExists_ShouldReturnTaxRate() {
        when(taxRateRepository.findById(1L)).thenReturn(Optional.of(taxRate));

        TaxRateVm result = taxRateService.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.rate()).isEqualTo(10.0);
    }

    @Test
    void testFindById_WhenTaxRateNotFound_ShouldThrowNotFoundException() {
        when(taxRateRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taxRateService.findById(999L))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    void testCreateTaxRate_WhenValidData_ShouldCreateTaxRate() {
        TaxRatePostVm postVm = new TaxRatePostVm(10.0, "12345", 1L, 1L, 1L);
        when(taxClassRepository.existsById(1L)).thenReturn(true);
        when(taxClassRepository.getReferenceById(1L)).thenReturn(taxClass);
        when(taxRateRepository.save(any(TaxRate.class))).thenReturn(taxRate);

        TaxRate result = taxRateService.createTaxRate(postVm);

        assertThat(result).isNotNull();
        verify(taxRateRepository, times(1)).save(any(TaxRate.class));
    }

    @Test
    void testCreateTaxRate_WhenTaxClassNotFound_ShouldThrowNotFoundException() {
        TaxRatePostVm postVm = new TaxRatePostVm(10.0, "12345", 999L, 1L, 1L);
        when(taxClassRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> taxRateService.createTaxRate(postVm))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    void testUpdateTaxRate_WhenValidData_ShouldUpdateTaxRate() {
        TaxRatePostVm postVm = new TaxRatePostVm(15.0, "54321", 1L, 2L, 2L);
        when(taxRateRepository.findById(1L)).thenReturn(Optional.of(taxRate));
        when(taxClassRepository.existsById(1L)).thenReturn(true);
        when(taxClassRepository.getReferenceById(1L)).thenReturn(taxClass);
        when(taxRateRepository.save(any(TaxRate.class))).thenReturn(taxRate);

        taxRateService.updateTaxRate(postVm, 1L);

        verify(taxRateRepository, times(1)).save(any(TaxRate.class));
    }

    @Test
    void testUpdateTaxRate_WhenTaxRateNotFound_ShouldThrowNotFoundException() {
        TaxRatePostVm postVm = new TaxRatePostVm(15.0, "54321", 1L, 2L, 2L);
        when(taxRateRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taxRateService.updateTaxRate(postVm, 999L))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    void testUpdateTaxRate_WhenTaxClassNotFound_ShouldThrowNotFoundException() {
        TaxRatePostVm postVm = new TaxRatePostVm(15.0, "54321", 999L, 2L, 2L);
        when(taxRateRepository.findById(1L)).thenReturn(Optional.of(taxRate));
        when(taxClassRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> taxRateService.updateTaxRate(postVm, 1L))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    void testDelete_WhenTaxRateExists_ShouldDeleteTaxRate() {
        when(taxRateRepository.existsById(1L)).thenReturn(true);

        taxRateService.delete(1L);

        verify(taxRateRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDelete_WhenTaxRateNotFound_ShouldThrowNotFoundException() {
        when(taxRateRepository.existsById(anyLong())).thenReturn(false);

        assertThatThrownBy(() -> taxRateService.delete(999L))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    void testGetTaxPercent_WhenTaxPercentExists_ShouldReturnTaxPercent() {
        when(taxRateRepository.getTaxPercent(1L, 1L, "12345", 1L)).thenReturn(10.0);

        double result = taxRateService.getTaxPercent(1L, 1L, 1L, "12345");

        assertThat(result).isEqualTo(10.0);
    }

    @Test
    void testGetTaxPercent_WhenTaxPercentNotFound_ShouldReturnZero() {
        when(taxRateRepository.getTaxPercent(anyLong(), anyLong(), anyString(), anyLong())).thenReturn(null);

        double result = taxRateService.getTaxPercent(1L, 1L, 1L, "12345");

        assertThat(result).isEqualTo(0);
    }

    @Test
    void testGetBulkTaxRate_WhenTaxRatesExist_ShouldReturnTaxRates() {
        when(taxRateRepository.getBatchTaxRates(anyLong(), anyLong(), anyString(), anySet()))
            .thenReturn(List.of(taxRate));

        List<TaxRateVm> result = taxRateService.getBulkTaxRate(List.of(1L), 1L, 1L, "12345");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(1L);
    }

    @Test
    void testGetBulkTaxRate_WhenNoTaxRatesFound_ShouldReturnEmptyList() {
        when(taxRateRepository.getBatchTaxRates(anyLong(), anyLong(), anyString(), anySet()))
            .thenReturn(List.of());

        List<TaxRateVm> result = taxRateService.getBulkTaxRate(List.of(999L), 1L, 1L, "12345");

        assertThat(result).isEmpty();
    }
}
