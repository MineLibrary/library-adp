package az.atlacademy.libraryadp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import az.atlacademy.libraryadp.model.entity.AdminUserEntity;
import java.util.Optional;

public interface AdminUserRepository extends JpaRepository<AdminUserEntity, Long>
{
    Optional<AdminUserEntity> findByUsername(String username);    
}
