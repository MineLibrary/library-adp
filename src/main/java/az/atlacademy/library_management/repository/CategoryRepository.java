package az.atlacademy.library_management.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import az.atlacademy.library_management.model.entity.CategoryEntity;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long>
{
    
}
