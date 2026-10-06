package com.deepblue.rescue.service;

import com.deepblue.rescue.domain.Animal;
import com.deepblue.rescue.domain.AnimalSex;
import com.deepblue.rescue.domain.RescueCase;
import com.deepblue.rescue.domain.RescueStatus;
import com.deepblue.rescue.exception.ResourceNotFoundException;
import com.deepblue.rescue.mapper.AnimalMapper;
import com.deepblue.rescue.repository.AnimalRepository;
import com.deepblue.rescue.service.impl.AnimalServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnimalServiceImplTest {

    @Mock
    private AnimalRepository repository;

    @Mock
    private AnimalMapper mapper;

    @InjectMocks
    private AnimalServiceImpl service;

    private Animal newAnimal(String animalCode, RescueStatus status) {
        RescueCase rescueCase = new RescueCase("RES-001", LocalDate.of(2026, 8, 20), "Santa Marta", status);
        Animal animal = new Animal(animalCode, "Green Sea Turtle", "Chelonia mydas", AnimalSex.MALE);
        rescueCase.assignAnimal(animal);
        return animal;
    }

    @Test
    void shouldReturnTrueWhenAnimalIsInRehabilitation() {
        Animal animal = newAnimal("AN-001", RescueStatus.IN_REHABILITATION);
        when(repository.findByAnimalCode("AN-001")).thenReturn(Optional.of(animal));

        boolean canReceive = service.canReceiveTreatment("AN-001");

        assertThat(canReceive).isTrue();
    }

    @Test
    void shouldReturnFalseWhenAnimalIsReleased() {
        Animal animal = newAnimal("AN-002", RescueStatus.RELEASED);
        when(repository.findByAnimalCode("AN-002")).thenReturn(Optional.of(animal));

        boolean canReceive = service.canReceiveTreatment("AN-002");

        assertThat(canReceive).isFalse();
    }

    @Test
    void shouldThrowExceptionWhenAnimalDoesNotExist() {
        when(repository.findByAnimalCode("AN-999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.canReceiveTreatment("AN-999"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Animal no encontrado: AN-999");
    }
}