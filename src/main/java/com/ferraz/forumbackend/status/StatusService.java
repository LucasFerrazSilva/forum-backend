package com.ferraz.forumbackend.status;

import com.ferraz.forumbackend.status.entity.DatabaseInfo;
import com.ferraz.forumbackend.status.entity.StatusDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatusService {

    private final StatusDAO statusDAO;

    public StatusDTO getStatus() {
        DatabaseInfo databaseInfo = this.statusDAO.getDatabaseInfo();
        return new StatusDTO(LocalDateTime.now(), databaseInfo);
    }

}
