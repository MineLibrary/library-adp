package az.atlacademy.libraryadp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import az.atlacademy.libraryadp.model.entity.BookEntity;

public interface BookRepository extends JpaRepository<BookEntity, Long>
{
    
}
