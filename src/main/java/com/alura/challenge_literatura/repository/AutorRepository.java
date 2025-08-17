package com.alura.challenge_literatura.repository;

import com.alura.challenge_literatura.model.Autor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AutorRepository extends JpaRepository<Autor,Long> {

    Autor findByNombreAndFechaNacimientoAndFechaFallecimiento(String nombre, Integer fechaNacimiento,
                                                                Integer fechaFallecimiento);
}
