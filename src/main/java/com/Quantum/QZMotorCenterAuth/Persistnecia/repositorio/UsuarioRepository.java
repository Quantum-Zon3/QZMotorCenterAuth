package com.Quantum.QZMotorCenterAuth.persistnecia.repositorio;

import java.util.List;
import java.util.Optional;
//imports de conexion base de datos
import com.Quantum.QZMotorCenterAuth.persistnecia.entidad.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
}
