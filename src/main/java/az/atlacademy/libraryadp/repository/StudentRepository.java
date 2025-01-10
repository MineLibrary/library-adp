package az.atlacademy.libraryadp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import az.atlacademy.libraryadp.model.entity.StudentEntity;
import java.util.Optional;


public interface StudentRepository extends JpaRepository<StudentEntity, Long>
{
    Optional<StudentEntity> findByFinCode(String finCode);
}
