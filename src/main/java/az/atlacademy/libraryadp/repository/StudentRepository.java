package az.atlacademy.libraryadp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import az.atlacademy.libraryadp.model.entity.StudentEntity;

public interface StudentRepository extends JpaRepository<StudentEntity, Long>
{

}
