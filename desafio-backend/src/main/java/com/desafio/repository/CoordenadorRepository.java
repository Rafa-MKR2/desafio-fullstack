package com.desafio.repository;

import com.desafio.entity.Coordenador;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class CoordenadorRepository implements PanacheRepository<Coordenador> {

    public Optional<Coordenador> findByEmail(String email) {
        return find("email", email).singleResultOptional();
    }
}