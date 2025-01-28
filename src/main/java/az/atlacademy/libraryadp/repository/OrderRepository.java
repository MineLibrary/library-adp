package az.atlacademy.libraryadp.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import az.atlacademy.libraryadp.model.entity.BookEntity;
import az.atlacademy.libraryadp.model.entity.OrderEntity;
import az.atlacademy.libraryadp.model.entity.StudentEntity;


public interface OrderRepository extends JpaRepository<OrderEntity, Long>
{
    @EntityGraph(attributePaths = {"book", "student"})
    Page<OrderEntity> findByBook(BookEntity book, Pageable pageable);

    @EntityGraph(attributePaths = {"book", "student"})
    Page<OrderEntity> findByStudent(StudentEntity student, Pageable pageable);

    @EntityGraph(attributePaths = {"book", "student"})
    Page<OrderEntity> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"book", "student"})
    Optional<OrderEntity> findById(Long id);
}
