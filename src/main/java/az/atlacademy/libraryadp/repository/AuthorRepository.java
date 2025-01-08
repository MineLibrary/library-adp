package az.atlacademy.libraryadp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import az.atlacademy.libraryadp.model.entity.AuthorEntity;

public interface AuthorRepository extends JpaRepository<AuthorEntity, Long>
{
    
}
