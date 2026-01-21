package com.ferraz.forumbackend.teste;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("teste")
@RequiredArgsConstructor
public class TesteController {

    private final TesteService service;

    @GetMapping
    public ResponseEntity<TesteEntity> getAndIncrement() {
        TesteEntity teste = service.getAndIncrement();
        return ResponseEntity.ok(teste);
    }

}
