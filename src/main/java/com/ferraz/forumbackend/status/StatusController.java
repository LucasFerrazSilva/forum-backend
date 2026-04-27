package com.ferraz.forumbackend.status;

import com.ferraz.forumbackend.infra.service.AuthorizationService;
import com.ferraz.forumbackend.status.entity.DatabaseInfo;
import com.ferraz.forumbackend.status.entity.StatusDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/status")
public class StatusController {

    private final StatusService service;
    private final AuthorizationService authorizationService;

    @GetMapping
    public ResponseEntity<Object> getStatus() {
        StatusDTO status = this.service.getStatus();

        if (!authorizationService.loggedUserCan("read:status:all")) {
            DatabaseInfo dbInfo = status.database();
            status = new StatusDTO(
                    status.appVersion(),
                    status.updatedAt(),
                    new DatabaseInfo(null, dbInfo.maxConnections(), dbInfo.openedConnections())
            );
        }

        return ResponseEntity.ok(status);
    }

}
