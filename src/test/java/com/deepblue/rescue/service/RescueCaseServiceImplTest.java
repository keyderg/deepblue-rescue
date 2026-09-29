package com.deepblue.rescue.service;

import com.deepblue.rescue.domain.*;
import com.deepblue.rescue.dto.request.ChangeRescueStatusRequest;
import com.deepblue.rescue.exception.BusinessRuleException;
import com.deepblue.rescue.exception.ResourceNotFoundException;
import com.deepblue.rescue.mapper.RescueCaseMapper; // <-- Importante: El mapper correcto
import com.deepblue.rescue.repository.RescueCaseRepository;
import com.deepblue.rescue.service.impl.RescueCaseServiceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RescueCaseServiceImplTest {

    @Mock
    private RescueCaseRepository repository;

    @Mock
    private RescueCaseMapper mapper; // <-- Corregido: Ahora es RescueCaseMapper

    @InjectMocks
    private RescueCaseServiceImpl service;

    @Test
    void shouldThrowExceptionWhenRescueCaseNotFound() {
        when(
                repository.findByCaseCode("RES-999")
        ).thenReturn(
                Optional.empty()
        );

        assertThatThrownBy(() -> service.findByCode("RES-999"))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(repository)
                .findByCaseCode("RES-999");
    }

    @Test
    void shouldThrowExceptionWhenInvalidStatusTransition() {
        RescueCase rescueCase = new RescueCase(
                "RES-001",
                LocalDate.now(),
                "Calle 123",
                RescueStatus.ADMITTED
        );

        when(
                repository.findByCaseCode("RES-001")
        ).thenReturn(
                Optional.of(rescueCase)
        );

        ChangeRescueStatusRequest request = new ChangeRescueStatusRequest(RescueStatus.READY_FOR_RELEASE);

        assertThatThrownBy(() ->
                service.changeStatus("RES-001", request)
        ).isInstanceOf(BusinessRuleException.class);

        verify(repository)
                .findByCaseCode("RES-001");

        verify(repository, never())
                .save(any(RescueCase.class));
    }
}