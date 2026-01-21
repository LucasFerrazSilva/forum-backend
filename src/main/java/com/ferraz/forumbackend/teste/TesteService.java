package com.ferraz.forumbackend.teste;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TesteService {

    private static final Logger logger = LoggerFactory.getLogger(TesteService.class);

    private final TesteRepository repository;

    public TesteEntity getAndIncrement() {
        List<TesteEntity> list = this.repository.findAll();

        if (list.isEmpty()) {
            logger.warn("Não foram encontrados registros de teste");
            return null;
        }

        TesteEntity teste = list.getFirst();
        teste.increment();
        repository.save(teste);
        return teste;
    }

}
