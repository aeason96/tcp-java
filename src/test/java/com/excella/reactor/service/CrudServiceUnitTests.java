package com.excella.reactor.service;

import com.excella.reactor.common.exceptions.ResourceNotFoundException;
import com.excella.reactor.shared.SampleEntity;
import java.util.Arrays;
import java.util.Optional;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import reactor.test.StepVerifier;

public class CrudServiceUnitTests {

  private JpaRepository<SampleEntity, Long> mockRepository;

  private class SampleCrudService implements CrudService<SampleEntity> {

    @Override
    public JpaRepository<SampleEntity, Long> getRepository() {
      return mockRepository;
    }
  }

  private Pageable pageable = null;
  private Page page = null;
  private SampleCrudService sampleService;
  private SampleEntity sampleEntity1 = new SampleEntity();
  private SampleEntity sampleEntity2 = new SampleEntity();
  private SampleEntity sampleEntity3 = new SampleEntity();
  private Page<SampleEntity> sampleEntityList;

  @BeforeMethod
  private void beforeEach() {
    sampleService = new SampleCrudService();
    mockRepository = Mockito.mock(JpaRepository.class);
    sampleEntityList = new PageImpl<>(Arrays.asList(sampleEntity1, sampleEntity2, sampleEntity3));
  }

  @AfterMethod
  private void afterEach() {
    Mockito.reset(mockRepository);
  }

  // all
  @Test
  private void all_method_can_return_empty_flux() {
    Mockito.when(mockRepository.findAll(pageable)).thenReturn(page);
    StepVerifier.create(sampleService.all(null)).verifyComplete();
  }

  @Test
  private void all_method_can_return_flux_with_multiple_entities() {
    Mockito.when(mockRepository.findAll(pageable)).thenReturn(sampleEntityList);
    StepVerifier.create(sampleService.all(null))
        .expectNextSequence(sampleEntityList)
        .verifyComplete();
  }

  // byId

  @Test
  private void byId_throws_ResourceNotFoundException_if_nothing_found() {
    Mockito.when(mockRepository.findById(Mockito.anyLong())).thenReturn(Optional.empty());
    StepVerifier.create(sampleService.byId(1234L))
        .expectError(ResourceNotFoundException.class)
        .verify();
  }

  @Test
  private void byId_can_return_instance_when_one_found() {
    Mockito.when(mockRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(sampleEntity1));
    StepVerifier.create(sampleService.byId(1234L)).expectNext(sampleEntity1).verifyComplete();
  }
  // save
  @Test
  private void save_returns_mono_of_saved_entity() {
    Mockito.when(mockRepository.save(Mockito.any(SampleEntity.class))).thenReturn(sampleEntity1);
    StepVerifier.create(sampleService.save(sampleEntity1))
        .expectNext(sampleEntity1)
        .verifyComplete();
  }

  // update
  @Test
  private void
      update_throws_ResourceNotFoundException_and_does_not_save_new_entity_when_no_matching_id_found() {
    Mockito.when(mockRepository.save(Mockito.any())).thenReturn(sampleEntity1);
    Mockito.when(mockRepository.findById(Mockito.any())).thenReturn(Optional.empty());

    StepVerifier.create(sampleService.update(1234L, sampleEntity1))
        .expectError(ResourceNotFoundException.class)
        .verify();

    Mockito.verify(mockRepository, Mockito.never()).save(ArgumentMatchers.any());
  }

  @Test
  private void update_returns_mono_with_updated_entity_and_saves_when_matching_id_found() {

    Mockito.when(mockRepository.save(sampleEntity1)).thenReturn(sampleEntity1);
    Mockito.when(mockRepository.findById(1234L)).thenReturn(Optional.of(sampleEntity1));

    StepVerifier.create(sampleService.update(1234L, sampleEntity1))
        .expectNext(sampleEntity1)
        .verifyComplete();

    Mockito.verify(mockRepository, Mockito.times(1)).save(ArgumentMatchers.eq(sampleEntity1));
  }
  // delete

  @Test
  private void delete_throws_ResourceNotFoundException_when_no_matching_entity_is_found() {
    Mockito.when(mockRepository.findById(1234L)).thenReturn(Optional.empty());

    StepVerifier.create(sampleService.delete(1234L))
        .expectError(ResourceNotFoundException.class)
        .verify();

    Mockito.verify(mockRepository, Mockito.never()).delete(ArgumentMatchers.any());
  }

  @Test
  private void delete_deletes_a_matching_entry_when_found() {
    Mockito.when(mockRepository.findById(1234L)).thenReturn(Optional.of(sampleEntity1));

    StepVerifier.create(sampleService.delete(1234L)).expectNext(sampleEntity1).verifyComplete();

    Mockito.verify(mockRepository, Mockito.times(1)).delete(ArgumentMatchers.eq(sampleEntity1));
  }
}
