package com.inventory.system.controller;

import com.inventory.system.domain.Category;
import com.inventory.system.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*")
public class CategoryController {

    @Autowired
    private CategoryRepository categoryRepository;

    
    @GetMapping
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    
    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        return categoryRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    
    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<?> createCategory(@RequestBody Category category) {
        if (categoryRepository.existsByName(category.getName())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Category with name '" + category.getName() + "' already exists"));
        }
        category.setIsSystemDefault(false); 
        Category saved = categoryRepository.save(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<?> updateCategory(@PathVariable Long id, @RequestBody Category categoryDetails) {
        return categoryRepository.findById(id)
                .map(category -> {
                    
                    if (!category.getName().equals(categoryDetails.getName()) 
                            && categoryRepository.existsByName(categoryDetails.getName())) {
                        return ResponseEntity.badRequest()
                                .body(Map.of("error", "Category with name '" + categoryDetails.getName() + "' already exists"));
                    }
                    
                    category.setName(categoryDetails.getName());
                    category.setDescription(categoryDetails.getDescription());
                    
                    Category updated = categoryRepository.save(category);
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        return categoryRepository.findById(id)
                .map(category -> {
                    
                    if (category.getIsSystemDefault()) {
                        return ResponseEntity.badRequest()
                                .body(Map.of("error", "Cannot delete system default category"));
                    }
                    
                    categoryRepository.delete(category);
                    return ResponseEntity.ok(Map.of("message", "Category deleted successfully"));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
