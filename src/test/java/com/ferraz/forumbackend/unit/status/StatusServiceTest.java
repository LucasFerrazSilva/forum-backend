package com.ferraz.forumbackend.unit.status;

import com.ferraz.forumbackend.infra.exception.DatabaseException;
import com.ferraz.forumbackend.integration.status.StatusDAO;
import com.ferraz.forumbackend.integration.status.StatusService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatusServiceTest {

    @Mock
    private StatusDAO statusDAO;

    @InjectMocks
    private StatusService statusService;

    @Test
    @DisplayName("Deve lançar um DatabaseException quando buscar status do banco com ele inascessível")
    void shouldThrowDatabaseExceptionWhenDatabaseIsInaccessible() {
        when(statusDAO.getDatabaseInfo()).thenThrow(new RuntimeException("Banco fora do ar"));
        Assertions.assertThrows(DatabaseException.class, () -> {
            statusService.getStatus();
        });
    }

}