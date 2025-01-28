package az.atlacademy.libraryadp.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import az.atlacademy.libraryadp.model.entity.StudentEntity;
import java.util.Optional;


public interface StudentRepository extends JpaRepository<StudentEntity, Long>
{
    @EntityGraph(attributePaths = {"orders"})
    Optional<StudentEntity> findByFinCode(String finCode);

    @EntityGraph(attributePaths = {"orders"})
    Page<StudentEntity> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"orders"})
    Optional<StudentEntity> findById(Long id);
}
