package com.mocicarazvan.templatemodule.services;

import com.mocicarazvan.templatemodule.models.IdGeneratedImpl;
import com.mocicarazvan.templatemodule.services.impl.RabbitMqUpdateDeleteServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RabbitMqUpdateDeleteServiceTest {

    @Mock
    private RabbitMqSender updateSender;

    @Mock
    private RabbitMqSender deleteSender;

    private RabbitMqUpdateDeleteService<IdGeneratedImpl> service;

    @BeforeEach
    void setUp() {
        service = new RabbitMqUpdateDeleteServiceImpl<>(updateSender, deleteSender);
    }

    @Test
    void sendUpdateMessage() {
        IdGeneratedImpl model = IdGeneratedImpl.builder().id(1L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        doNothing().when(updateSender).sendMessage(model);
        service.sendUpdateMessage(model);
        verify(updateSender, times(1)).sendMessage(eq(model));
        verify(deleteSender, never()).sendMessage(any());
    }

    @Test
    void sendUpdateMessage_propagatesError() {
        IdGeneratedImpl model = IdGeneratedImpl.builder().id(1L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        doThrow(new RuntimeException("Test exception")).when(updateSender).sendMessage(model);
        assertThrows(RuntimeException.class, () -> service.sendUpdateMessage(model));
        verify(updateSender, times(1)).sendMessage(eq(model));
        verify(deleteSender, never()).sendMessage(any());
    }

    @Test
    void sendDeleteMessage() {
        IdGeneratedImpl model = IdGeneratedImpl.builder().id(1L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        doNothing().when(deleteSender).sendMessage(model);
        service.sendDeleteMessage(model);
        verify(deleteSender, times(1)).sendMessage(eq(model));
        verify(updateSender, never()).sendMessage(any());
    }

    @Test
    void sendDeleteMessage_propagatesError() {
        IdGeneratedImpl model = IdGeneratedImpl.builder().id(1L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        doThrow(new RuntimeException("Test exception")).when(deleteSender).sendMessage(model);
        assertThrows(RuntimeException.class, () -> service.sendDeleteMessage(model));
        verify(deleteSender, times(1)).sendMessage(eq(model));
        verify(updateSender, never()).sendMessage(any());
    }

    @Test
    void sendBatchUpdateMessages() {
        IdGeneratedImpl model1 = IdGeneratedImpl.builder().id(1L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        IdGeneratedImpl model2 = IdGeneratedImpl.builder().id(2L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        doNothing().when(updateSender).sendBatchMessage(List.of(model1, model2));
        service.sendBatchUpdateMessage(List.of(model1, model2));
        verify(updateSender, times(1)).sendBatchMessage(eq(List.of(model1, model2)));
        verify(deleteSender, never()).sendBatchMessage(any());
    }

    @Test
    void sendBatchUpdateMessages_emptyList() {
        service.sendBatchUpdateMessage(List.of());
        verify(updateSender, never()).sendBatchMessage(any());
        verify(deleteSender, never()).sendBatchMessage(any());
    }

    @Test
    void sendBatchUpdateMessages_propagatesError() {
        IdGeneratedImpl model1 = IdGeneratedImpl.builder().id(1L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        IdGeneratedImpl model2 = IdGeneratedImpl.builder().id(2L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        doThrow(new RuntimeException("Test exception")).when(updateSender).sendBatchMessage(List.of(model1, model2));
        assertThrows(RuntimeException.class, () -> service.sendBatchUpdateMessage(List.of(model1, model2)));
        verify(updateSender, times(1)).sendBatchMessage(eq(List.of(model1, model2)));
        verify(deleteSender, never()).sendBatchMessage(any());
    }

    @Test
    void sendBatchDeleteMessages() {
        IdGeneratedImpl model1 = IdGeneratedImpl.builder().id(1L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        IdGeneratedImpl model2 = IdGeneratedImpl.builder().id(2L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        doNothing().when(deleteSender).sendBatchMessage(List.of(model1, model2));
        service.sendBatchDeleteMessage(List.of(model1, model2));
        verify(deleteSender, times(1)).sendBatchMessage(eq(List.of(model1, model2)));
        verify(updateSender, never()).sendBatchMessage(any());
    }

    @Test
    void sendBatchDeleteMessages_emptyList() {
        service.sendBatchDeleteMessage(List.of());
        verify(deleteSender, never()).sendBatchMessage(any());
        verify(updateSender, never()).sendBatchMessage(any());
    }

    @Test
    void sendBatchDeleteMessages_propagatesError() {
        IdGeneratedImpl model1 = IdGeneratedImpl.builder().id(1L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        IdGeneratedImpl model2 = IdGeneratedImpl.builder().id(2L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        doThrow(new RuntimeException("Test exception")).when(deleteSender).sendBatchMessage(List.of(model1, model2));
        assertThrows(RuntimeException.class, () -> service.sendBatchDeleteMessage(List.of(model1, model2)));
        verify(deleteSender, times(1)).sendBatchMessage(eq(List.of(model1, model2)));
        verify(updateSender, never()).sendBatchMessage(any());
    }

}