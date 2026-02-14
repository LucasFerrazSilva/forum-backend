package com.ferraz.forumbackend.integration.status;

import com.ferraz.forumbackend.infra.exception.DatabaseException;
import com.ferraz.forumbackend.integration.status.entity.DatabaseInfo;
import com.ferraz.forumbackend.integration.status.entity.StatusDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class StatusService {

    private final StatusDAO statusDAO;

    private final BuildProperties buildProperties;

    public StatusDTO getStatus() {
        try {
            DatabaseInfo databaseInfo = this.statusDAO.getDatabaseInfo();
            return new StatusDTO(buildProperties.getVersion(), LocalDateTime.now(), databaseInfo);
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

}
