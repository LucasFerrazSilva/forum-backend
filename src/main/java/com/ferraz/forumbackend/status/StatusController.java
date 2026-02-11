package com.ferraz.forumbackend.status;

import com.ferraz.forumbackend.status.entity.StatusDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("status")
public class StatusController {

    private final StatusService service;

    @GetMapping
    public ResponseEntity<StatusDTO> getStatus() {
        StatusDTO status = this.service.getStatus();
        return ResponseEntity.ok(status);
    }

}
