package com.Quantum.QZMotorCenterAuth.web.controller;
//imports de anotacion springboot
import com.Quantum.QZMotorCenterAuth.config.security.JwtService;
import com.Quantum.QZMotorCenterAuth.dominio.servicio.UsuarioService;
import com.Quantum.QZMotorCenterAuth.persistnecia.dto.LoginRequest;
import com.Quantum.QZMotorCenterAuth.persistnecia.dto.RefreshTokenRequest;
import com.Quantum.QZMotorCenterAuth.persistnecia.dto.AuthResponse;
import com.Quantum.QZMotorCenterAuth.persistnecia.dto.UsuarioDto;
import com.Quantum.QZMotorCenterAuth.persistnecia.entidad.Token;
import com.Quantum.QZMotorCenterAuth.persistnecia.entidad.TokenType;
import com.Quantum.QZMotorCenterAuth.persistnecia.repositorio.TokenRepository;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.token.TokenService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
//imports para documentar swagen
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@RestController
@RequestMapping("/qzwork_hub/auth")
@Tag(name = "Usuario", description = "Controlador de usuarios")
@Slf4j
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final JwtService  jwtService;
    private final TokenRepository tokenRepository;
    private final Executor asyncExecutor;

    @Autowired
    public UsuarioController(UsuarioService usuarioService, JwtService jwtService ,TokenRepository tokenRepository,
                             @Qualifier("asyncExecutor") Executor asyncExecutor) {
        this.usuarioService = usuarioService;
        this.jwtService = jwtService;
        this.tokenRepository = tokenRepository;
        this.asyncExecutor = asyncExecutor;
    }


    /**
     * Crear un nuevo Usuario (Asíncrono)
     */
    @PostMapping
    @Operation(
            summary = "Crear nuevo usuario",
            description = "Crea un nuevo usaurio en el sistema con validación de email único"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Usuario creado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UsuarioDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos o email duplicado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = String.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor"
            )
    })
    public CompletableFuture<ResponseEntity<UsuarioDto>> save(
            @Parameter(description = "Datos del usaurio a crear", required = true)
            @RequestBody UsuarioDto createDTO
    ) {
        log.info("POST /qzwork_hub/usuarios - Creando Usuario: {}", createDTO.getEmail());

        return CompletableFuture.supplyAsync(() -> {
            try {
                UsuarioDto usuarioCreado = usuarioService.save(createDTO);
                log.info("usuario creado exitosamente con ID: {}", usuarioCreado.getCedula());
                return ResponseEntity.status(HttpStatus.CREATED).body(usuarioCreado);
            } catch (IllegalArgumentException e) {
                log.warn("Error de validación al crear usario: {}", e.getMessage());
                return ResponseEntity.<UsuarioDto>badRequest().build();
            }
        }, asyncExecutor);
    }

    /**
     * Obtener todos los Usuario (Asíncrono)
     */
    @GetMapping
    @Operation(
            summary = "Listar todos los usuarios",
            description = "Obtiene la lista completa de usuario registrados en el sistema"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de usuarios obtenida exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UsuarioDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuarios no encontrado"
            )
    })
    public CompletableFuture<ResponseEntity<List<UsuarioDto>>> finAll() {
        log.debug("GET /qzwork_hub/usuarios - Obteniendo todos los usaurio");

        return CompletableFuture.supplyAsync(() -> {
            try {
                List<UsuarioDto> usaurios = usuarioService.findAll();
                log.debug("Se encontraron {} usaurios", usaurios.size());
                return ResponseEntity.ok(usaurios);
            } catch (EmptyResultDataAccessException e) {
                log.error("No se encontro el usaurio exitosamente");
                return ResponseEntity.<List<UsuarioDto>>notFound().build();
            }
        }, asyncExecutor);
    }


    /**
     * Obtener Usuairo por ID (Asíncrono)
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar Usuario por ID",
            description = "Obtiene la información completa de un usuario específico"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuario encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UsuarioDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado"
            )
    })
    public CompletableFuture<ResponseEntity<UsuarioDto>> findById(
            @Parameter(description = "ID del usuario", required = true, example = "1")
            @PathVariable Long id
    ) {
        log.debug("GET /qzwork_hub/usuarios/{} - Buscando usuario", id);

        return CompletableFuture.supplyAsync(() -> {
            try {
                UsuarioDto usaurio = usuarioService.findById(id);
                return ResponseEntity.ok(usaurio);
            } catch (RuntimeException e) {
                log.warn("Usuario no encontrado con ID: {}", id);
                return ResponseEntity.<UsuarioDto>notFound().build();
            }
        }, asyncExecutor);
    }



    /**
     * Actualizar usuario existente (Asíncrono)
     */
    @PutMapping("/{id}")
    @Operation(
            summary = "Actualizar usaurio",
            description = "Actualiza la información de un usuario existente. El email no se puede modificar."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuario actualizado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UsuarioDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado"
            )
    })
    public CompletableFuture<ResponseEntity<UsuarioDto>> update(
            @Parameter(description = "ID del usuario a actualizar", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Datos a actualizar del usuario", required = true)
            @RequestBody UsuarioDto updateDTO
    ) {
        log.info("PUT /qzwork_hub/usuarios/{} - Actualizando usuario", id);

        return CompletableFuture.supplyAsync(() -> {
            try {
                UsuarioDto usuarioActualizado = usuarioService.update(id, updateDTO);
                log.info("Usuario actualizado exitosamente ID: {}", id);
                return ResponseEntity.ok(usuarioActualizado);
            } catch (RuntimeException e) {
                if (e.getMessage().contains("no encontrado")) {
                    log.warn("Usuario no encontrado para actualizar ID: {}", id);
                    return ResponseEntity.<UsuarioDto>notFound().build();
                }
                log.warn("Error al actualizar usaurio ID {}: {}", id, e.getMessage());
                return ResponseEntity.<UsuarioDto>badRequest().build();
            }
        }, asyncExecutor);
    }

    /**
     * Eliminar usuario (Asíncrono)
     */
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Eliminar usuario",
            description = "Elimina un usuario del sistema. No se puede eliminar si tiene productos asociados."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Usuario eliminado exitosamente"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "No se puede eliminar: usuario tiene reservas asociados"
            )
    })
    public CompletableFuture<ResponseEntity<Void>> delete(
            @Parameter(description = "ID del usuario a eliminar", required = true, example = "1")
            @PathVariable Long id
    ) {
        log.info("DELETE /qzwork_hub/usuarios/{} - Eliminando usuario", id);

        return CompletableFuture.supplyAsync(() -> {
            try {
                usuarioService.deleteUsuario(id);
                log.info("Usuario eliminado exitosamente ID: {}", id);
                return ResponseEntity.<Void>noContent().build();
            } catch (RuntimeException e) {
                if (e.getMessage().contains("no encontrado")) {
                    log.warn("Usuairo no encontrado para eliminar ID: {}", id);
                    return ResponseEntity.<Void>notFound().build();
                } else if (e.getMessage().contains("reserva")) {
                    log.warn("Intento de usaurio con reservas ID: {}", id);
                    return ResponseEntity.<Void>status(HttpStatus.CONFLICT).build();
                }
                log.error("Error al eliminar usuario ID {}: {}", id, e.getMessage());
                return ResponseEntity.<Void>status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }, asyncExecutor);
    }

    @PostMapping("/login")
    public CompletableFuture<ResponseEntity<AuthResponse>> login(@RequestBody LoginRequest request) {

        return CompletableFuture.supplyAsync(() -> {
            UsuarioDto usuario = usuarioService.loguear(request.getEmail(), request.getPassword());

            // Revocar tokens anteriores del usuario
            List<Token> validTokens = tokenRepository.findAllByEmailAndExpiredFalseAndRevokedFalse(usuario.getEmail());
            validTokens.forEach(t -> {
                t.setExpired(true);
                t.setRevoked(true);
            });
            tokenRepository.saveAll(validTokens);

            String token = jwtService.generateToken(usuario.getEmail());
            String refreshToken = jwtService.generateRefreshToken(usuario.getEmail());

            Token tokenEntity = new Token();
            tokenEntity.setToken(token);
            tokenEntity.setEmail(usuario.getEmail());
            tokenEntity.setTokenType(TokenType.BEARER);
            tokenEntity.setExpired(false);
            tokenEntity.setRevoked(false);

            Token refreshTokenEntity = new Token();
            refreshTokenEntity.setToken(refreshToken);
            refreshTokenEntity.setEmail(usuario.getEmail());
            refreshTokenEntity.setTokenType(TokenType.REFRESH);
            refreshTokenEntity.setExpired(false);
            refreshTokenEntity.setRevoked(false);

            tokenRepository.save(tokenEntity);
            tokenRepository.save(refreshTokenEntity);

            return ResponseEntity.ok(
                    AuthResponse.builder()
                            .accessToken(token)
                            .refreshToken(refreshToken)
                            .usuario(usuario)
                            .build()
            );
        }, asyncExecutor);
    }

    @PostMapping("/logout")
    public CompletableFuture<ResponseEntity<?>> logout(HttpServletRequest request) {

        return CompletableFuture.supplyAsync(() -> {
            String authHeader = request.getHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {

                String token = authHeader.substring(7);

                Token storedToken = tokenRepository.findByToken(token).orElse(null);

                if (storedToken != null) {
                    storedToken.setRevoked(true);
                    storedToken.setExpired(true);
                    tokenRepository.save(storedToken);
                    
                    // Opcionalmente, revocar todos los tokens asociados al email
                    List<Token> validTokens = tokenRepository.findAllByEmailAndExpiredFalseAndRevokedFalse(storedToken.getEmail());
                    validTokens.forEach(t -> {
                        t.setExpired(true);
                        t.setRevoked(true);
                    });
                    tokenRepository.saveAll(validTokens);
                }
            }

            return ResponseEntity.ok("Logout exitoso");
        }, asyncExecutor);
    }

    @PostMapping("/refresh-token")
    public CompletableFuture<ResponseEntity<?>> refreshToken(@RequestBody RefreshTokenRequest request) {

        return CompletableFuture.supplyAsync(() -> {
            String refreshToken = request.getRefreshToken();
            if (refreshToken == null) {
                return ResponseEntity.badRequest().body("Token de refresco es requerido");
            }

            Token storedToken = tokenRepository.findByToken(refreshToken).orElse(null);

            if (storedToken == null || storedToken.isExpired() || storedToken.isRevoked() || storedToken.getTokenType() != TokenType.REFRESH) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token de refresco inválido o expirado");
            }

            if (!jwtService.isTokenValid(refreshToken)) {
                storedToken.setExpired(true);
                storedToken.setRevoked(true);
                tokenRepository.save(storedToken);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token de refresco inválido o expirado");
            }

            String email = jwtService.extractUsername(refreshToken);
            
            // Invalidar el refresh token usado para rotarlo (Refresh Token Rotation)
            storedToken.setExpired(true);
            storedToken.setRevoked(true);
            tokenRepository.save(storedToken);

            String newToken = jwtService.generateToken(email);
            String newRefreshToken = jwtService.generateRefreshToken(email);

            Token newTokenEntity = new Token();
            newTokenEntity.setToken(newToken);
            newTokenEntity.setEmail(email);
            newTokenEntity.setTokenType(TokenType.BEARER);
            newTokenEntity.setExpired(false);
            newTokenEntity.setRevoked(false);

            Token newRefreshTokenEntity = new Token();
            newRefreshTokenEntity.setToken(newRefreshToken);
            newRefreshTokenEntity.setEmail(email);
            newRefreshTokenEntity.setTokenType(TokenType.REFRESH);
            newRefreshTokenEntity.setExpired(false);
            newRefreshTokenEntity.setRevoked(false);

            tokenRepository.save(newTokenEntity);
            tokenRepository.save(newRefreshTokenEntity);

            // Fetch user info just to return it in AuthResponse (assuming loguear is ok, or just find by email if method existed.. since no findByEmail, returning only tokens)
            try {
                // Here we actually need user info, but UsuarioService doesn't have findByEmail openly.
                // We'll return just the tokens in the response or find user through findAll.
                List<UsuarioDto> allUsers = usuarioService.findAll();
                UsuarioDto theUser = allUsers.stream().filter(u -> u.getEmail().equals(email)).findFirst().orElse(null);

                return ResponseEntity.ok(
                        AuthResponse.builder()
                                .accessToken(newToken)
                                .refreshToken(newRefreshToken)
                                .usuario(theUser)
                                .build()
                );
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

        }, asyncExecutor);
    }
}
