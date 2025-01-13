package az.atlacademy.libraryadp.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import az.atlacademy.libraryadp.model.entity.BookEntity;
import az.atlacademy.libraryadp.model.entity.OrderEntity;
import az.atlacademy.libraryadp.model.entity.StudentEntity;


public interface OrderRepository extends JpaRepository<OrderEntity, Long>
{
    Page<OrderEntity> findByBook(BookEntity book, Pageable pageable);
    Page<OrderEntity> findByStudent(StudentEntity student, Pageable pageable);
}
