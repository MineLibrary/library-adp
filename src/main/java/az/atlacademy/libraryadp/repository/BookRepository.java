package az.atlacademy.libraryadp.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import az.atlacademy.libraryadp.model.entity.AuthorEntity;
import az.atlacademy.libraryadp.model.entity.BookEntity;
import az.atlacademy.libraryadp.model.entity.CategoryEntity;

public interface BookRepository extends JpaRepository<BookEntity, Long>
{
    Page<BookEntity> findByCategory(CategoryEntity category, Pageable pageable);

    @Query("SELECT b FROM BookEntity b JOIN b.authors a WHERE a = :author")
    Page<BookEntity> findByAuthor(@Param("author") AuthorEntity author, Pageable pageable);

    @Query("SELECT b FROM BookEntity b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    Page<BookEntity> searchByTitle(@Param("title") String title, Pageable pageable);
}
