package com.minimarket.controller;

import com.minimarket.api.dto.UsuarioCreateRequest;
import com.minimarket.api.dto.UsuarioResponse;
import com.minimarket.api.dto.UsuarioUpdateRequest;
import com.minimarket.api.problem.ApiProblem;
import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.RolRepository;
import com.minimarket.security.SecurityRoles;
import com.minimarket.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@Validated
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios", description = "User administration endpoints")
@SecurityRequirement(name = "bearerAuth")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioController(UsuarioService usuarioService, RolRepository rolRepository,
                             PasswordEncoder passwordEncoder) {
        this.usuarioService = usuarioService;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    @Operation(summary = "List users", description = "Requires HTTP Basic authentication.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "HAL user collection"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid Basic credentials",
                    content = @Content(schema = @Schema(implementation = ApiProblem.class)))
    })
    public CollectionModel<EntityModel<UsuarioResponse>> listarUsuarios() {
        Collection<EntityModel<UsuarioResponse>> usuarios = usuarioService.findAll().stream()
                .map(this::toModel)
                .toList();
        return CollectionModel.of(usuarios,
                linkTo(methodOn(UsuarioController.class).listarUsuarios()).withSelfRel());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a user", description = "Requires HTTP Basic authentication.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "HAL user resource"),
            @ApiResponse(responseCode = "400", description = "The identifier must be positive",
                    content = @Content(schema = @Schema(implementation = ApiProblem.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid Basic credentials",
                    content = @Content(schema = @Schema(implementation = ApiProblem.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ApiProblem.class)))
    })
    public ResponseEntity<EntityModel<UsuarioResponse>> obtenerUsuarioPorId(
            @PathVariable @Positive Long id) {
        return usuarioService.findById(id).map(usuario -> ResponseEntity.ok(toModel(usuario)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('" + SecurityRoles.ADMIN + "')")
    @Operation(summary = "Create a user", description = "Requires HTTP Basic authentication. Password is stored as a hash.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User created; Location identifies the new resource"),
            @ApiResponse(responseCode = "400", description = "Invalid request or an unknown role identifier",
                    content = @Content(schema = @Schema(implementation = ApiProblem.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid Basic credentials",
                    content = @Content(schema = @Schema(implementation = ApiProblem.class))),
            @ApiResponse(responseCode = "403", description = "ADMIN role is required; RFC 9457 error",
                    content = @Content(mediaType = org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Username conflicts with an existing user",
                    content = @Content(schema = @Schema(implementation = ApiProblem.class)))
    })
    public ResponseEntity<EntityModel<UsuarioResponse>> guardarUsuario(
            @Valid @RequestBody UsuarioCreateRequest request) {
        Usuario usuario = new Usuario();
        usuario.setUsername(request.username());
        usuario.setPassword(passwordEncoder.encode(request.password()));
        usuario.setRoles(resolveRoles(request.rolIds()));

        Usuario creado = usuarioService.save(usuario);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.getId()).toUri();
        return ResponseEntity.created(location).body(toModel(creado));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('" + SecurityRoles.ADMIN + "')")
    @Operation(summary = "Update a user", description = "Requires HTTP Basic authentication. Omitted password and role identifiers retain their current values.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Updated HAL user resource"),
            @ApiResponse(responseCode = "400", description = "Invalid request or an unknown role identifier",
                    content = @Content(schema = @Schema(implementation = ApiProblem.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid Basic credentials",
                    content = @Content(schema = @Schema(implementation = ApiProblem.class))),
            @ApiResponse(responseCode = "403", description = "ADMIN role is required; RFC 9457 error",
                    content = @Content(mediaType = org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ApiProblem.class))),
            @ApiResponse(responseCode = "409", description = "Username conflicts with an existing user",
                    content = @Content(schema = @Schema(implementation = ApiProblem.class)))
    })
    public ResponseEntity<EntityModel<UsuarioResponse>> actualizarUsuario(@PathVariable @Positive Long id,
                                                                            @Valid @RequestBody UsuarioUpdateRequest request) {
        Optional<Usuario> existente = usuarioService.findById(id);
        if (existente.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Usuario usuario = existente.get();
        usuario.setUsername(request.username());
        if (request.password() != null) {
            usuario.setPassword(passwordEncoder.encode(request.password()));
        }
        if (request.rolIds() != null) {
            usuario.setRoles(resolveRoles(request.rolIds()));
        }

        return ResponseEntity.ok(toModel(usuarioService.save(usuario)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('" + SecurityRoles.ADMIN + "')")
    @Operation(summary = "Delete a user", description = "Requires HTTP Basic authentication.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "User deleted"),
            @ApiResponse(responseCode = "400", description = "The identifier must be positive",
                    content = @Content(schema = @Schema(implementation = ApiProblem.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid Basic credentials",
                    content = @Content(schema = @Schema(implementation = ApiProblem.class))),
            @ApiResponse(responseCode = "403", description = "ADMIN role is required; RFC 9457 error",
                    content = @Content(mediaType = org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ApiProblem.class)))
    })
    public ResponseEntity<Void> eliminarUsuario(@PathVariable @Positive Long id) {
        if (usuarioService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        usuarioService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private Set<Rol> resolveRoles(Set<Long> rolIds) {
        Set<Rol> roles = new LinkedHashSet<>(rolRepository.findAllById(rolIds));
        if (roles.size() != rolIds.size()) {
            throw new IllegalArgumentException("One or more role identifiers do not exist.");
        }
        return roles;
    }

    private EntityModel<UsuarioResponse> toModel(Usuario usuario) {
        Set<Long> rolIds = usuario.getRoles() == null ? Set.of() : usuario.getRoles().stream()
                .map(Rol::getId)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
        UsuarioResponse response = new UsuarioResponse(usuario.getId(), usuario.getUsername(), rolIds);
        return EntityModel.of(response,
                linkTo(methodOn(UsuarioController.class).obtenerUsuarioPorId(usuario.getId())).withSelfRel());
    }
}
