package com.ferraz.forumbackend.unit.status;

import com.ferraz.forumbackend.infra.exception.DatabaseException;
import com.ferraz.forumbackend.infra.service.AuthorizationService;
import com.ferraz.forumbackend.status.StatusController;
import com.ferraz.forumbackend.status.StatusService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatusControllerTest {

    @Mock
    private StatusService statusService;

    @Mock
    private AuthorizationService authorizationService;

    @InjectMocks
    private StatusController statusController;

    @Test
    @DisplayName("Deve lançar um DatabaseException quando buscar status do banco com ele inascessível")
    void shouldThrowDatabaseExceptionWhenDatabaseIsInaccessibleOnController() {
        when(statusService.getStatus()).thenThrow(new DatabaseException(new RuntimeException()));
        Assertions.assertThrows(DatabaseException.class, () -> {
            statusController.getStatus();
        });
    }

}