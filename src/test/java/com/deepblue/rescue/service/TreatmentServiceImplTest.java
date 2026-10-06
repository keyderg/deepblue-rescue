package com.deepblue.rescue.service;

import com.deepblue.rescue.domain.*;
import com.deepblue.rescue.dto.request.CreateTreatmentRequest;
import com.deepblue.rescue.dto.response.TreatmentResponse;
import com.deepblue.rescue.exception.BusinessRuleException;
import com.deepblue.rescue.mapper.TreatmentMapper;
import com.deepblue.rescue.repository.AnimalRepository;
import com.deepblue.rescue.repository.RescueCaseRepository;
import com.deepblue.rescue.repository.SpecialistRepository;
import com.deepblue.rescue.repository.TreatmentRepository;

import com.deepblue.rescue.service.impl.TreatmentServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TreatmentServiceImplTest {

    @Mock
    private AnimalRepository animalRepository;

    @Mock
    private SpecialistRepository specialistRepository;

    @Mock
    private TreatmentRepository treatmentRepository;

    @Mock
    private RescueCaseRepository rescueCaseRepository;

    @Mock
    private TreatmentMapper mapper;

    @InjectMocks
    private TreatmentServiceImpl treatmentService;

    @Test
    void shouldRegisterTreatmentSuccessfully() {
        CreateTreatmentRequest request = new CreateTreatmentRequest(
                "AN-001",
                "SPEC-001",
                LocalDateTime.now(),
                TreatmentType.WOUND_CARE,
                "Curación"
        );

        Animal animal = new Animal("AN-001", "Perro", "Canis lupus", AnimalSex.MALE);
        Specialist specialist = new Specialist("SPEC-001", "Juan", "Pérez", "juan@mail.com", true);

        RescueCase rescueCase = new RescueCase(
                "RES-001",
                LocalDate.now(),
                "Calle Principal",
                RescueStatus.IN_REHABILITATION
        );

        Treatment treatment = new Treatment(
                animal,
                specialist,
                request.performedAt(),
                request.type(),
                request.description()
        );

        TreatmentResponse expectedResponse = new TreatmentResponse(
                1L,
                "AN-001",
                "SPEC-001",
                request.performedAt(),
                request.type(),
                request.description()
        );

        when(animalRepository.findByAnimalCode("AN-001"))
                .thenReturn(Optional.of(animal));

        when(specialistRepository.findByProfessionalCode("SPEC-001"))
                .thenReturn(Optional.of(specialist));

        when(rescueCaseRepository.findByAnimal(animal))
                .thenReturn(Optional.of(rescueCase));

        when(treatmentRepository.save(any(Treatment.class)))
                .thenReturn(treatment);

        when(mapper.toResponse(treatment))
                .thenReturn(expectedResponse);

        TreatmentResponse result = treatmentService.register(request);

        assertThat(result).isEqualTo(expectedResponse);

        verify(animalRepository).findByAnimalCode("AN-001");
        verify(specialistRepository).findByProfessionalCode("SPEC-001");
        verify(rescueCaseRepository).findByAnimal(animal);
        verify(treatmentRepository).save(any(Treatment.class));
        verify(mapper).toResponse(treatment);
    }

    @Test
    void shouldThrowExceptionWhenSpecialistIsInactive() {
        CreateTreatmentRequest request = new CreateTreatmentRequest(
                "AN-001",
                "SPEC-001",
                LocalDateTime.now(),
                TreatmentType.WOUND_CARE,
                "Curación"
        );

        Animal animal = new Animal("AN-001", "Perro", "Canis lupus", AnimalSex.MALE);
        Specialist specialist = new Specialist("SPEC-001", "Ana", "Gómez", "ana@mail.com", false);

        when(animalRepository.findByAnimalCode("AN-001"))
                .thenReturn(Optional.of(animal));

        when(specialistRepository.findByProfessionalCode("SPEC-001"))
                .thenReturn(Optional.of(specialist));

        assertThatThrownBy(() -> treatmentService.register(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Specialist is not active");

        verify(animalRepository).findByAnimalCode("AN-001");
        verify(specialistRepository).findByProfessionalCode("SPEC-001");
        verify(treatmentRepository, never()).save(any(Treatment.class));
    }

    @Test
    void shouldThrowExceptionWhenRescueCaseIsReleased() {
        CreateTreatmentRequest request = new CreateTreatmentRequest(
                "AN-001",
                "SPEC-001",
                LocalDateTime.now(),
                TreatmentType.OBSERVATION,
                "Observación general"
        );

        Animal animal = new Animal("AN-001", "Perro", "Canis lupus", AnimalSex.MALE);
        Specialist specialist = new Specialist("SPEC-001", "Juan", "Pérez", "juan@mail.com", true);

        RescueCase rescueCase = new RescueCase(
                "RES-001",
                LocalDate.now(),
                "Calle Principal",
                RescueStatus.RELEASED
        );

        when(animalRepository.findByAnimalCode("AN-001"))
                .thenReturn(Optional.of(animal));

        when(specialistRepository.findByProfessionalCode("SPEC-001"))
                .thenReturn(Optional.of(specialist));

        when(rescueCaseRepository.findByAnimal(animal))
                .thenReturn(Optional.of(rescueCase));

        assertThatThrownBy(() -> treatmentService.register(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Cannot add treatment to a released or closed case");

        verify(animalRepository).findByAnimalCode("AN-001");
        verify(specialistRepository).findByProfessionalCode("SPEC-001");
        verify(rescueCaseRepository).findByAnimal(animal);
        verify(treatmentRepository, never()).save(any(Treatment.class));
    }

    @Test
    void shouldRegisterIntegratorChallengeTreatmentSuccessfully() {
        LocalDateTime treatmentDate = LocalDateTime.of(2026, 8, 21, 9, 0);

        CreateTreatmentRequest request = new CreateTreatmentRequest(
                "AN-2026-100",
                "SPEC-001",
                treatmentDate,
                TreatmentType.WOUND_CARE,
                "Cleaning of left front flipper injury."
        );

        Animal animal = new Animal("AN-2026-100", "Green Sea Turtle", "Chelonia mydas", AnimalSex.FEMALE);
        Specialist specialist = new Specialist("SPEC-001", "Elena", "Vargas", "elena@mail.com", true);

        RescueCase rescueCase = new RescueCase(
                "RES-2026-100",
                LocalDate.of(2026, 8, 20),
                "Beach area",
                RescueStatus.IN_REHABILITATION
        );

        Treatment treatment = new Treatment(
                animal,
                specialist,
                request.performedAt(),
                request.type(),
                request.description()
        );

        TreatmentResponse expectedResponse = new TreatmentResponse(
                1L,
                "AN-2026-100",
                "SPEC-001",
                request.performedAt(),
                request.type(),
                request.description()
        );

        when(animalRepository.findByAnimalCode("AN-2026-100"))
                .thenReturn(Optional.of(animal));

        when(specialistRepository.findByProfessionalCode("SPEC-001"))
                .thenReturn(Optional.of(specialist));

        when(rescueCaseRepository.findByAnimal(animal))
                .thenReturn(Optional.of(rescueCase));

        when(treatmentRepository.save(any(Treatment.class)))
                .thenReturn(treatment);

        when(mapper.toResponse(treatment))
                .thenReturn(expectedResponse);

        TreatmentResponse result = treatmentService.register(request);

        assertThat(result).isEqualTo(expectedResponse);

        verify(animalRepository).findByAnimalCode("AN-2026-100");
        verify(specialistRepository).findByProfessionalCode("SPEC-001");
        verify(rescueCaseRepository).findByAnimal(animal);
        verify(treatmentRepository).save(any(Treatment.class));
        verify(mapper).toResponse(treatment);
    }

    @Test
    void shouldThrowExceptionWhenTreatmentDateBeforeRescueDate() {
        LocalDateTime invalidTreatmentDate = LocalDateTime.of(2026, 8, 15, 9, 0);

        CreateTreatmentRequest request = new CreateTreatmentRequest(
                "AN-2026-100",
                "SPEC-001",
                invalidTreatmentDate,
                TreatmentType.WOUND_CARE,
                "Intento de tratamiento previo al rescate"
        );

        Animal animal = new Animal("AN-2026-100", "Green Sea Turtle", "Chelonia mydas", AnimalSex.FEMALE);
        Specialist specialist = new Specialist("SPEC-001", "Elena", "Vargas", "elena@mail.com", true);

        RescueCase rescueCase = new RescueCase(
                "RES-2026-100",
                LocalDate.of(2026, 8, 20),
                "Beach area",
                RescueStatus.IN_REHABILITATION
        );

        when(animalRepository.findByAnimalCode("AN-2026-100"))
                .thenReturn(Optional.of(animal));

        when(specialistRepository.findByProfessionalCode("SPEC-001"))
                .thenReturn(Optional.of(specialist));

        when(rescueCaseRepository.findByAnimal(animal))
                .thenReturn(Optional.of(rescueCase));

        assertThatThrownBy(() -> treatmentService.register(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Treatment date cannot be before rescue date");

        verify(animalRepository).findByAnimalCode("AN-2026-100");
        verify(specialistRepository).findByProfessionalCode("SPEC-001");
        verify(rescueCaseRepository).findByAnimal(animal);
        verify(treatmentRepository, never()).save(any(Treatment.class));
    }

    @Test
    void shouldThrowExceptionWhenCaseIsReleasedForIntegratorChallenge() {
        LocalDateTime treatmentDate = LocalDateTime.of(2026, 8, 21, 9, 0);

        CreateTreatmentRequest request = new CreateTreatmentRequest(
                "AN-2026-100",
                "SPEC-001",
                treatmentDate,
                TreatmentType.OBSERVATION,
                "Intent of observation after release"
        );

        Animal animal = new Animal("AN-2026-100", "Green Sea Turtle", "Chelonia mydas", AnimalSex.FEMALE);
        Specialist specialist = new Specialist("SPEC-001", "Elena", "Vargas", "elena@mail.com", true);

        RescueCase rescueCase = new RescueCase(
                "RES-2026-100",
                LocalDate.of(2026, 8, 20),
                "Beach area",
                RescueStatus.RELEASED
        );

        when(animalRepository.findByAnimalCode("AN-2026-100"))
                .thenReturn(Optional.of(animal));

        when(specialistRepository.findByProfessionalCode("SPEC-001"))
                .thenReturn(Optional.of(specialist));

        when(rescueCaseRepository.findByAnimal(animal))
                .thenReturn(Optional.of(rescueCase));

        assertThatThrownBy(() -> treatmentService.register(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Cannot add treatment to a released or closed case");

        verify(animalRepository).findByAnimalCode("AN-2026-100");
        verify(specialistRepository).findByProfessionalCode("SPEC-001");
        verify(rescueCaseRepository).findByAnimal(animal);
        verify(treatmentRepository, never()).save(any(Treatment.class));
    }

    @Test
    void shouldThrowExceptionWhenSpecialistIsInactiveForIntegratorChallenge() {
        LocalDateTime treatmentDate = LocalDateTime.of(2026, 8, 21, 9, 0);

        CreateTreatmentRequest request = new CreateTreatmentRequest(
                "AN-2026-100",
                "SPEC-001",
                treatmentDate,
                TreatmentType.WOUND_CARE,
                "Treatment attempt with inactive specialist"
        );

        Animal animal = new Animal("AN-2026-100", "Green Sea Turtle", "Chelonia mydas", AnimalSex.FEMALE);
        Specialist specialist = new Specialist("SPEC-001", "Elena", "Vargas", "elena@mail.com", false);

        RescueCase rescueCase = new RescueCase(
                "RES-2026-100",
                LocalDate.of(2026, 8, 20),
                "Beach area",
                RescueStatus.IN_REHABILITATION
        );

        when(animalRepository.findByAnimalCode("AN-2026-100"))
                .thenReturn(Optional.of(animal));

        when(specialistRepository.findByProfessionalCode("SPEC-001"))
                .thenReturn(Optional.of(specialist));

        assertThatThrownBy(() -> treatmentService.register(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Specialist is not active");

        verify(animalRepository).findByAnimalCode("AN-2026-100");
        verify(specialistRepository).findByProfessionalCode("SPEC-001");
        verify(treatmentRepository, never()).save(any(Treatment.class));
    }
}