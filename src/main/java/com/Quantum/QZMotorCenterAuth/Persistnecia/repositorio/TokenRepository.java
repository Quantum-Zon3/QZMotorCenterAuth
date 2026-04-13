package com.Quantum.QZMotorCenterAuth.persistnecia.repositorio;

import com.Quantum.QZMotorCenterAuth.persistnecia.entidad.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.Quantum.QZMotorCenterAuth.persistnecia.entidad.Token;

import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {

    Optional<Token> findByToken(String token);

    List<Token> findAllByEmailAndExpiredFalseAndRevokedFalse(String email);
}