package az.atlacademy.libraryadp.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import az.atlacademy.libraryadp.model.entity.AuthorEntity;
import java.util.Optional;


public interface AuthorRepository extends JpaRepository<AuthorEntity, Long>
{
    @Query(value = """
        SELECT a FROM AuthorEntity a 
            WHERE LOWER(CONCAT(a.firstName, ' ', a.lastName)) LIKE LOWER(CONCAT('%', :fullName, '%'))
    """)
    @EntityGraph(attributePaths = {"books"})
    Page<AuthorEntity> searchByFullName(@Param(value = "fullName") String fullName, Pageable pageable);

    @EntityGraph(attributePaths = {"books"})
    Optional<AuthorEntity> findById(Long id);

    @EntityGraph(attributePaths = {"books"})
    Page<AuthorEntity> findAll(Pageable pageable);
}
