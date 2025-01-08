package az.atlacademy.library_management.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import az.atlacademy.library_management.model.entity.BookEntity;

public interface BookRepository extends JpaRepository<BookEntity, Long>
{
    
}
