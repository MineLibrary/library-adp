package az.atlacademy.libraryadp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import az.atlacademy.libraryadp.model.entity.OrderEntity;

public interface OrderRepository extends JpaRepository<OrderEntity, Long>
{
    
}
