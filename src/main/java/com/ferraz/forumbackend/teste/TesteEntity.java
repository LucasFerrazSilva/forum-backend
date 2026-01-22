package com.ferraz.forumbackend.teste;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name="TB_TESTE")
@Data
public class TesteEntity {
    @Id
    @Column(name="ID_TESTE")
    private Integer id;

    @Column(name="valor")
    private Integer valor;

    public void increment() {
        this.valor += 3;
    }
}
