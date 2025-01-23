package az.atlacademy.libraryadp.service;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import az.atlacademy.libraryadp.exception.AdminUserNotFoundException;
import az.atlacademy.libraryadp.model.dto.request.LoginRequest;
import az.atlacademy.libraryadp.model.dto.response.BaseResponse;
import az.atlacademy.libraryadp.model.dto.response.LoginResponse;
import az.atlacademy.libraryadp.model.entity.AdminUserEntity;
import az.atlacademy.libraryadp.repository.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService 
{
    private final JwtEncoder jwtEncoder; 
    private final AuthenticationManager authenticationManager; 
    private final AdminUserRepository adminUserRepository; 

    @Value("${application.security.jwt.issuer}")
    private String jwtIssuer; 

    private String generateJwtToken(AdminUserEntity adminUserEntity)
    {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer(jwtIssuer)
            .issuedAt(now)
            .expiresAt(now.plusSeconds(3600 * 24))
            .subject(adminUserEntity.getUsername())
            .claim("roles", List.of("ROLE_ADMIN"))
            .build();

        JwtEncoderParameters params = JwtEncoderParameters
            .from(JwsHeader.with(MacAlgorithm.HS256).build(), claims);

        return jwtEncoder.encode(params).getTokenValue(); 
    }

    public BaseResponse<LoginResponse> authenticateAdminUser(LoginRequest loginRequest)
    {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(), loginRequest.getPassword()
            )
        );

        AdminUserEntity adminUserEntity = adminUserRepository
            .findByUsername(loginRequest.getUsername())
            .orElseThrow(
                () -> new AdminUserNotFoundException("Admin user not found with username " + loginRequest.getUsername())
            );
        
        String jwtToken = generateJwtToken(adminUserEntity);

        log.info("Admin user {} authenticated successfully", loginRequest.getUsername());

        return BaseResponse.<LoginResponse>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Admin user authenticated successfully")
                .data(
                    LoginResponse.builder()
                        .token(jwtToken)
                        .build()
                ).build();  
    }

    public BaseResponse<Boolean> isAuthenticated()
    {
        log.info("User is authenticated");
        return BaseResponse.<Boolean>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("User is authenticated")
                .data(true)
                .build();
    }
}
