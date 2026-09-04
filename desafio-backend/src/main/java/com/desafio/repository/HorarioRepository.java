package com.desafio.repository;

import com.desafio.entity.Horario;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class HorarioRepository implements PanacheRepository<Horario> {
}