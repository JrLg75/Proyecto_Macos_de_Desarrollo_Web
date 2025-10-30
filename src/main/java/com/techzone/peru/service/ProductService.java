package com.techzone.peru.service;

import com.techzone.peru.model.dto.ProductDTO;
import com.techzone.peru.model.dto.VariantDTO;
import com.techzone.peru.model.entity.Categoria;
import com.techzone.peru.model.entity.Producto;
import com.techzone.peru.model.entity.ProductoVariante;
import com.techzone.peru.repository.CategoriaRepository;
import com.techzone.peru.repository.ProductoRepository;
import com.techzone.peru.repository.ProductoVarianteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.util.Comparator;
import java.text.Normalizer;

@Service
public class ProductService {

    @Autowired
    private ProductoRepository productoRepository;
    @Autowired
    private ProductoVarianteRepository productoVarianteRepository;
    @Autowired
    private CategoriaRepository categoriaRepository;
    @Autowired
    private FileStorageService fileStorageService;

    // --- Métodos de Lectura ---
    public List<Producto> findAllProducts() {
        return productoRepository.findAll();
    }

    public Optional<Producto> findProductById(Long id) {
        return productoRepository.findById(id);
    }

    public List<Producto> searchTopByName(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        return productoRepository.findTop5ByNombreContainingIgnoreCase(query.trim());
    }

    public List<Producto> searchByKeywordsSmart(String rawQuery) {
        if (rawQuery == null || rawQuery.isBlank()) return List.of();

        String cleaned = rawQuery.trim();
        String[] tokens = cleaned.toLowerCase().replaceAll("[¿?.,]", " ").split("\\s+");
        List<String> keywords = new ArrayList<>();
        for (String t : tokens) {
            if (t.length() >= 3) {
                keywords.add(t);
            }
        }
        if (keywords.isEmpty()) return searchTopByName(cleaned);

        Map<Long, Integer> productIdToScore = new HashMap<>();
        Map<Long, Producto> idToProduct = new HashMap<>();

        // 1) Intento en BD por cada keyword (union con ranking por cantidad de matches)
        for (String k : keywords) {
            List<Producto> partial = productoRepository.findTop5ByNombreContainingIgnoreCase(k);
            for (Producto p : partial) {
                idToProduct.putIfAbsent(p.getId(), p);
                productIdToScore.put(p.getId(), productIdToScore.getOrDefault(p.getId(), 0) + 1);
            }
        }

        List<Producto> ranked = productIdToScore.entrySet().stream()
                .sorted(Comparator.<Map.Entry<Long, Integer>>comparingInt(Map.Entry::getValue).reversed())
                .map(e -> idToProduct.get(e.getKey()))
                .limit(5)
                .toList();

        if (!ranked.isEmpty()) return ranked;

        // 2) Fallback acento-insensible en memoria
        String normalizedQuery = removeDiacritics(String.join(" ", keywords));
        List<Producto> all = findAllProducts();
        List<Producto> filtered = new ArrayList<>();
        for (Producto p : all) {
            if (p.getNombre() == null) continue;
            String normName = removeDiacritics(p.getNombre().toLowerCase());
            boolean allMatch = true;
            for (String k : keywords) {
                if (!normName.contains(removeDiacritics(k))) {
                    allMatch = false;
                    break;
                }
            }
            if (allMatch) filtered.add(p);
            if (filtered.size() >= 5) break;
        }
        return filtered;
    }

    private String removeDiacritics(String s) {
        String norm = Normalizer.normalize(s, Normalizer.Form.NFD);
        return norm.replaceAll("\\p{M}", "");
    }

    // --- Métodos de Escritura ---

    @Transactional
    public Producto saveBaseProduct(ProductDTO dto) {
        Categoria categoria = categoriaRepository.findById(dto.categoriaId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        Producto producto = new Producto();
        if (dto.id() != null) {
            producto = findProductById(dto.id()).orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        }

        producto.setNombre(dto.nombre());
        producto.setDescripcion(dto.descripcion());
        producto.setCategoria(categoria);
        producto.setActivo(true);
        // Inicializamos la lista de variantes si es un producto nuevo
        if (producto.getVariants() == null) {
            producto.setVariants(new ArrayList<>());
        }

        return productoRepository.save(producto);
    }

    @Transactional
    public ProductoVariante addVariantToProduct(Long productId, VariantDTO dto, MultipartFile imagen) {
        Producto producto = findProductById(productId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        ProductoVariante variante = new ProductoVariante();
        variante.setSku(dto.sku());
        variante.setStock(dto.stock());
        variante.setPrecio(dto.precio());
        variante.setColor(dto.color());
        variante.setTalla(dto.talla());
        if (imagen != null && !imagen.isEmpty()) {
            String nombreArchivo = fileStorageService.storeFile(imagen);
            variante.setImagenPrincipalUrl(nombreArchivo); // Guardamos el nombre del archivo en la entidad
        }

        variante.setProducto(producto);
        return productoVarianteRepository.save(variante);
    }

    public void deleteProductById(Long id) {
        productoRepository.deleteById(id);
    }

    public void deleteVariantById(Long variantId) {
        // Nos aseguramos de que la variante exista antes de borrarla
        if (!productoVarianteRepository.existsById(variantId)) {
            throw new RuntimeException("Variante no encontrada");
        }
        productoVarianteRepository.deleteById(variantId);
    }
}