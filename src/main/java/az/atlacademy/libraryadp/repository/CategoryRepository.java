package az.atlacademy.libraryadp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import az.atlacademy.libraryadp.model.entity.CategoryEntity;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long>
{
    
}
