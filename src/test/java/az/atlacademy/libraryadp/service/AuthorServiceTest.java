package az.atlacademy.libraryadp.service;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import az.atlacademy.libraryadp.exception.AuthorNotFoundException;
import az.atlacademy.libraryadp.mapper.AuthorMapper;
import az.atlacademy.libraryadp.model.dto.request.AuthorRequest;
import az.atlacademy.libraryadp.model.dto.response.AuthorResponse;
import az.atlacademy.libraryadp.model.dto.response.BaseResponse;
import az.atlacademy.libraryadp.model.entity.AuthorEntity;
import az.atlacademy.libraryadp.repository.AuthorRepository;

@ExtendWith(value = MockitoExtension.class)
public class AuthorServiceTest 
{
    @InjectMocks
    private AuthorService authorService; 

    @Mock
    private AuthorRepository authorRepository; 

    @Mock
    private AuthorMapper authorMapper; 

    @Test
    @DisplayName(value = "Testing createAuthor() method")
    public void givenCreateAuthorThenReturnSuccessResponse()
    {
        AuthorRequest createAuthorRequest = AuthorRequest
            .builder()
            .firstName("Nizami")
            .lastName("Ganjavi")
            .build(); 

        AuthorEntity createAuthorEntity = AuthorEntity
            .builder()
            .firstName("Nizami")
            .lastName("Ganjavi")
            .build(); 

        AuthorEntity createdAuthorEntity = AuthorEntity
            .builder()
            .id(1L)
            .firstName("Nizami")
            .lastName("Ganjavi")
            .build(); 

        Mockito.when(authorMapper.requestToEntity(createAuthorRequest)).thenReturn(createAuthorEntity);
        Mockito.when(authorRepository.save(createAuthorEntity)).thenReturn(createdAuthorEntity);
        
        BaseResponse<Void> serviceResponse = authorService.createAuthor(createAuthorRequest);

        Mockito.verify(authorMapper, Mockito.times(1)).requestToEntity(createAuthorRequest);
        Mockito.verify(authorRepository, Mockito.times(1)).save(createAuthorEntity);
        Mockito.verifyNoMoreInteractions(authorRepository, authorMapper);
        
        Assertions.assertEquals(true, serviceResponse.isSuccess());
        Assertions.assertEquals("Author created successfully.", serviceResponse.getMessage());
        Assertions.assertEquals(HttpStatus.CREATED.value(), serviceResponse.getStatus());
        Assertions.assertNull(serviceResponse.getData());
    }
    
    @Test
    @DisplayName(value = "Testing getAuthorById() method when author exists")
    public void givenGetAuthorByIdWhenAuthorExistsThenReturnBaseResponseOfAuthorResponse()
    {
        AuthorEntity foundAuthorEntity = AuthorEntity
            .builder()
            .id(1L)
            .firstName("Nizami")
            .lastName("Ganjavi")
            .build();

        AuthorResponse foundAuthorResponse = AuthorResponse
            .builder()
            .id(1L)
            .firstName("Nizami")
            .lastName("Ganjavi")
            .build();

        Mockito.when(authorRepository.findById(1L)).thenReturn(Optional.of(foundAuthorEntity));
        Mockito.when(authorMapper.entityToResponse(foundAuthorEntity)).thenReturn(foundAuthorResponse);
        
        BaseResponse<AuthorResponse> serviceResponse = authorService.getAuthorById(1L);

        Mockito.verify(authorRepository, Mockito.times(1)).findById(1L);
        Mockito.verify(authorMapper, Mockito.times(1)).entityToResponse(foundAuthorEntity);
        Mockito.verifyNoMoreInteractions(authorRepository, authorMapper);

        Assertions.assertEquals(true, serviceResponse.isSuccess());
        Assertions.assertEquals("Author retrieved successfully.", serviceResponse.getMessage());
        Assertions.assertEquals(HttpStatus.OK.value(), serviceResponse.getStatus());
        Assertions.assertNotNull(serviceResponse.getData());
        Assertions.assertEquals(1L, serviceResponse.getData().getId());
        Assertions.assertEquals("Nizami", serviceResponse.getData().getFirstName());
        Assertions.assertEquals("Ganjavi", serviceResponse.getData().getLastName());
    }

    @Test
    @DisplayName(value = "Testing getAuthorById() method when author does not exist")
    public void givenGetAuthorByIdWhenAuthorDoesNotExistThenThrowAuthorNotFoundException()
    {
        Mockito.when(authorRepository.findById(1L)).thenReturn(Optional.empty());
        
        AuthorNotFoundException exception = Assertions
            .assertThrows(AuthorNotFoundException.class, () -> authorService.getAuthorById(1L));

        Assertions.assertEquals("Author not found with id : 1", exception.getMessage());

        Mockito.verify(authorRepository, Mockito.times(1)).findById(1L);
        Mockito.verifyNoMoreInteractions(authorRepository);
    }
}
