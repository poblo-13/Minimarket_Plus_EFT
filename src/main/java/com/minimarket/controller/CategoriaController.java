package com.minimarket.controller;

import com.minimarket.api.dto.CategoriaRequest;
import com.minimarket.api.dto.CategoriaResponse;
import com.minimarket.api.mapper.ResourceMapper;
import com.minimarket.entity.Categoria;
import com.minimarket.service.CategoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
@Tag(name = "Categorias")
@SecurityRequirement(name = "basicAuth")
public class CategoriaController {
    private final CategoriaService categoriaService;

    @GetMapping
    @Operation(summary = "Listar categorías")
    @ApiResponses(@ApiResponse(responseCode = "401", description = "Autenticación Basic requerida"))
    public CollectionModel<EntityModel<CategoriaResponse>> listarCategorias() {
        List<EntityModel<CategoriaResponse>> resources = categoriaService.findAll().stream().map(this::resource).toList();
        return CollectionModel.of(resources, linkTo(methodOn(CategoriaController.class).listarCategorias()).withSelfRel());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una categoría")
    @ApiResponses({@ApiResponse(responseCode = "401", description = "Autenticación Basic requerida"),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada")})
    public ResponseEntity<EntityModel<CategoriaResponse>> obtenerCategoriaPorId(@PathVariable Long id) {
        Categoria categoria = categoriaService.findById(id);
        return categoria == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(resource(categoria));
    }

    @PostMapping
    @Operation(summary = "Crear una categoría")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Categoría creada"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "401", description = "Autenticación Basic requerida")})
    public ResponseEntity<EntityModel<CategoriaResponse>> guardarCategoria(@Valid @RequestBody CategoriaRequest request) {
        Categoria categoria = new Categoria();
        categoria.setNombre(request.nombre());
        Categoria saved = categoriaService.save(categoria);
        EntityModel<CategoriaResponse> resource = resource(saved);
        return ResponseEntity.created(URI.create(resource.getRequiredLink("self").getHref())).body(resource);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar una categoría")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Categoría actualizada"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "401", description = "Autenticación Basic requerida"),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada")})
    public ResponseEntity<EntityModel<CategoriaResponse>> actualizarCategoria(@PathVariable Long id, @Valid @RequestBody CategoriaRequest request) {
        Categoria categoria = categoriaService.findById(id);
        if (categoria == null) return ResponseEntity.notFound().build();
        categoria.setNombre(request.nombre());
        return ResponseEntity.ok(resource(categoriaService.save(categoria)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una categoría")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Categoría eliminada"),
            @ApiResponse(responseCode = "401", description = "Autenticación Basic requerida"),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada")})
    public ResponseEntity<Void> eliminarCategoria(@PathVariable Long id) {
        if (categoriaService.findById(id) == null) return ResponseEntity.notFound().build();
        categoriaService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private EntityModel<CategoriaResponse> resource(Categoria categoria) {
        return EntityModel.of(ResourceMapper.toResponse(categoria),
                linkTo(methodOn(CategoriaController.class).obtenerCategoriaPorId(categoria.getId())).withSelfRel(),
                linkTo(methodOn(CategoriaController.class).listarCategorias()).withRel("categorias"),
                linkTo(methodOn(ProductoController.class).listarProductos()).withRel("productos"));
    }
}
