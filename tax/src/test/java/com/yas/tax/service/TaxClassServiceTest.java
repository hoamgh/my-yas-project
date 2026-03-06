package com.yas.tax.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yas.commonlibrary.exception.DuplicatedException;
import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.tax.model.TaxClass;
import com.yas.tax.repository.TaxClassRepository;
import com.yas.tax.viewmodel.taxclass.TaxClassListGetVm;
import com.yas.tax.viewmodel.taxclass.TaxClassPostVm;
import com.yas.tax.viewmodel.taxclass.TaxClassVm;
import java.util.List;
import java.util.Optional;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class TaxClassServiceTest {

    @Mock
    private TaxClassRepository taxClassRepository;

    private TaxClassService taxClassService;

    private TaxClass taxClass;

    @BeforeEach
    void setUp() {
        taxClassService = new TaxClassService(taxClassRepository);
        taxClass = TaxClass.builder()
            .id(1L)
            .name("Standard Tax")
            .build();
    }

    @Test
    void findAllTaxClasses_WhenTaxClassesExist_ShouldReturnAllTaxClasses() {
        TaxClass taxClass2 = TaxClass.builder().id(2L).name("Reduced Tax").build();
        when(taxClassRepository.findAll(any(Sort.class))).thenReturn(List.of(taxClass, taxClass2));

        List<TaxClassVm> result = taxClassService.findAllTaxClasses();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo("Standard Tax");
        assertThat(result.get(1).name()).isEqualTo("Reduced Tax");
    }

    @Test
    void findAllTaxClasses_WhenNoTaxClassesExist_ShouldReturnEmptyList() {
        when(taxClassRepository.findAll(any(Sort.class))).thenReturn(List.of());

        List<TaxClassVm> result = taxClassService.findAllTaxClasses();

        assertThat(result).isEmpty();
    }

    @Test
    void findById_WhenTaxClassExists_ShouldReturnTaxClass() {
        when(taxClassRepository.findById(1L)).thenReturn(Optional.of(taxClass));

        TaxClassVm result = taxClassService.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Standard Tax");
    }

    @Test
    void findById_WhenTaxClassNotFound_ShouldThrowNotFoundException() {
        when(taxClassRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taxClassService.findById(999L))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    void create_WhenValidData_ShouldCreateTaxClass() {
        TaxClassPostVm postVm = new TaxClassPostVm("1", "New Tax Class");
        when(taxClassRepository.existsByName(anyString())).thenReturn(false);
        when(taxClassRepository.save(any(TaxClass.class))).thenReturn(taxClass);

        TaxClass result = taxClassService.create(postVm);

        assertThat(result).isNotNull();
        verify(taxClassRepository, times(1)).save(any(TaxClass.class));
    }

    @Test
    void create_WhenNameAlreadyExists_ShouldThrowDuplicatedException() {
        TaxClassPostVm postVm = new TaxClassPostVm("1", "Standard Tax");
        when(taxClassRepository.existsByName("Standard Tax")).thenReturn(true);

        assertThatThrownBy(() -> taxClassService.create(postVm))
            .isInstanceOf(DuplicatedException.class);
    }

    @Test
    void update_WhenValidData_ShouldUpdateTaxClass() {
        TaxClassPostVm postVm = new TaxClassPostVm("1", "Updated Tax Class");
        when(taxClassRepository.findById(1L)).thenReturn(Optional.of(taxClass));
        when(taxClassRepository.existsByNameNotUpdatingTaxClass(anyString(), anyLong())).thenReturn(false);
        when(taxClassRepository.save(any(TaxClass.class))).thenReturn(taxClass);

        taxClassService.update(postVm, 1L);

        verify(taxClassRepository, times(1)).save(any(TaxClass.class));
    }

    @Test
    void update_WhenTaxClassNotFound_ShouldThrowNotFoundException() {
        TaxClassPostVm postVm = new TaxClassPostVm("1", "Updated Tax Class");
        when(taxClassRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taxClassService.update(postVm, 999L))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    void update_WhenNameAlreadyExistsForOtherTaxClass_ShouldThrowDuplicatedException() {
        TaxClassPostVm postVm = new TaxClassPostVm("1", "Existing Name");
        when(taxClassRepository.findById(1L)).thenReturn(Optional.of(taxClass));
        when(taxClassRepository.existsByNameNotUpdatingTaxClass("Existing Name", 1L)).thenReturn(true);

        assertThatThrownBy(() -> taxClassService.update(postVm, 1L))
            .isInstanceOf(DuplicatedException.class);
    }

    @Test
    void delete_WhenTaxClassExists_ShouldDeleteTaxClass() {
        when(taxClassRepository.existsById(1L)).thenReturn(true);

        taxClassService.delete(1L);

        verify(taxClassRepository, times(1)).deleteById(1L);
    }

    @Test
    void delete_WhenTaxClassNotFound_ShouldThrowNotFoundException() {
        when(taxClassRepository.existsById(anyLong())).thenReturn(false);

        assertThatThrownBy(() -> taxClassService.delete(999L))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getPageableTaxClasses_WhenTaxClassesExist_ShouldReturnPagedResult() {
        TaxClass taxClass2 = TaxClass.builder().id(2L).name("Reduced Tax").build();
        Page<TaxClass> page = new PageImpl<>(List.of(taxClass, taxClass2));
        when(taxClassRepository.findAll(any(Pageable.class))).thenReturn(page);

        TaxClassListGetVm result = taxClassService.getPageableTaxClasses(0, 10);

        assertThat(result.taxClassContent()).hasSize(2);
        assertThat(result.pageNo()).isEqualTo(0);
        assertThat(result.totalElements()).isEqualTo(2);
    }

    @Test
    void getPageableTaxClasses_WhenNoTaxClasses_ShouldReturnEmptyPage() {
        Page<TaxClass> emptyPage = new PageImpl<>(List.of());
        when(taxClassRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

        TaxClassListGetVm result = taxClassService.getPageableTaxClasses(0, 10);

        assertThat(result.taxClassContent()).isEmpty();
        assertThat(result.totalElements()).isEqualTo(0);
    }
}
