package az.atlacademy.libraryadp.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import az.atlacademy.libraryadp.model.dto.request.LoginRequest;
import az.atlacademy.libraryadp.model.dto.response.BaseResponse;
import az.atlacademy.libraryadp.model.dto.response.LoginResponse;
import az.atlacademy.libraryadp.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/auth")
public class AuthController 
{
    private static final String LOG_TEMPLATE = "{} request to /api/v1/author{}";

    private final AuthService authService; 

    @PostMapping(value = "/authenticate")
    public BaseResponse<LoginResponse> authenticateAdminUser(
        @RequestBody LoginRequest loginRequest
    ){
        log.info(LOG_TEMPLATE, "POST", "/authenticate");
        return authService.authenticateAdminUser(loginRequest);  
    }
}
