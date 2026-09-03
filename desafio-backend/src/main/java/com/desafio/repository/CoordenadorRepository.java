package com.desafio.repository;

import com.desafio.entity.Coordenador;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CoordenadorRepository implements PanacheRepository<Coordenador> {
}