package com.supera.Super.A.repository;

import com.supera.Super.A.model.Category;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends MongoRepository<Category, String> {
    Optional<Category> findByCategoryNameIgnoreCase(String categoryName);
}
