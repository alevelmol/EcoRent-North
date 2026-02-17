package com.ecorent.gestionalquileres.repository;

import com.ecorent.gestionalquileres.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {

    Optional<Client> findByDni(String dni);

    // RF-14 → clientes con más alquileres
    @Query("""
        SELECT c FROM Client c
        LEFT JOIN Rental r ON r.client = c
        GROUP BY c
        ORDER BY COUNT(r.id) DESC
    """)
    List<Client> findTopClients();
    
    boolean existsByDni(String dni);

}
