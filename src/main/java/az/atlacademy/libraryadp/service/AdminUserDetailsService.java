package az.atlacademy.libraryadp.service;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import az.atlacademy.libraryadp.exception.AdminUserNotFoundException;
import az.atlacademy.libraryadp.model.entity.AdminUserEntity;
import az.atlacademy.libraryadp.repository.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserDetailsService implements UserDetailsService
{
    private final AdminUserRepository adminUserRepository; 

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException 
    {
        AdminUserEntity adminUserEntity = adminUserRepository.findByUsername(username)
            .orElseThrow(() -> new AdminUserNotFoundException("Admin user not found with username : " + username)); 
        
        return User
                .withUsername(adminUserEntity.getUsername())
                .password(adminUserEntity.getPassword())
                .roles("ADMIN")
                .build();    
    }
}
