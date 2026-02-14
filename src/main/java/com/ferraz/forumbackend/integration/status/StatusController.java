package com.ferraz.forumbackend.integration.status;

import com.ferraz.forumbackend.integration.status.entity.StatusDTO;
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

    @GetMapping
    public ResponseEntity<Object> getStatus() {
        StatusDTO status = this.service.getStatus();
        return ResponseEntity.ok(status);
    }

}
