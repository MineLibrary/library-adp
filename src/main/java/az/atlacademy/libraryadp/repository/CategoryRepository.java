package az.atlacademy.libraryadp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import az.atlacademy.libraryadp.model.entity.CategoryEntity;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long>
{
    @EntityGraph(attributePaths = {"books"})
    List<CategoryEntity> findAll(); 

    @EntityGraph(attributePaths = {"books"})
    Optional<CategoryEntity> findById(Long id);
}
