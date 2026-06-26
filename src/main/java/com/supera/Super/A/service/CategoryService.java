package com.supera.Super.A.service;

import com.supera.Super.A.model.Category;
import com.supera.Super.A.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    public Optional<Category> findById(String id) {
        return categoryRepository.findById(id);
    }

    public Optional<Category> createCategory(Category category) {
        if (category.getCategoryName() == null || category.getCategoryName().isBlank()) {
            throw new IllegalArgumentException("categoryName is required");
        }
        if (categoryRepository.findByCategoryNameIgnoreCase(category.getCategoryName()).isPresent()) {
            return Optional.empty();
        }
        return Optional.of(categoryRepository.save(category));
    }

    public boolean deleteById(String id) {
        return categoryRepository.findById(id)
                .map(category -> {
                    categoryRepository.deleteById(category.getId());
                    return true;
                })
                .orElse(false);
    }
}
