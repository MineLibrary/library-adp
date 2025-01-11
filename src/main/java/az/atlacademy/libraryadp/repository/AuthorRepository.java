package az.atlacademy.libraryadp.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import az.atlacademy.libraryadp.model.entity.AuthorEntity;

public interface AuthorRepository extends JpaRepository<AuthorEntity, Long>
{
    @Query(value = """
        SELECT a FROM AuthorEntity a 
            WHERE LOWER(CONCAT(a.firstName, ' ', a.lastName)) LIKE LOWER(CONCAT('%', :fullName, '%'))
    """)
    Page<AuthorEntity> searchByFullName(@Param(value = "fullName") String fullName, Pageable pageable);
}
