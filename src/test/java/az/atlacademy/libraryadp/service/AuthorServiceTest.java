package az.atlacademy.libraryadp.service;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    @Test
    @DisplayName(value = "Testing getAuthors() method")
    public void givenGetAuthorsThenReturnBaseResponseOfListOfAuthors()
    {
        int pageNumber = 0, pageSize = 2;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        List<AuthorEntity> foundAuthorEntities = List
            .of(
                AuthorEntity.builder()
                    .id(1L)
                    .firstName("Nizami")
                    .lastName("Ganjavi")
                    .build(), 
                AuthorEntity.builder()
                    .id(2L)
                    .firstName("Jafar")
                    .lastName("Jabbarli")
                    .build()
            );

        List<AuthorResponse> foundAuthorResponses = List
            .of(
                AuthorResponse.builder()
                    .id(1L)
                    .firstName("Nizami")
                    .lastName("Ganjavi")
                    .build(), 
                AuthorResponse.builder()
                    .id(2L)
                    .firstName("Jafar")
                    .lastName("Jabbarli")
                    .build()
            );

        Page<AuthorEntity> foundAuthorPage = new PageImpl<>(foundAuthorEntities);

        Mockito.when(authorRepository.findAll(pageable)).thenReturn(foundAuthorPage);
        for(int i = 0; i < pageSize; i++) 
        {
            Mockito.when(authorMapper.entityToResponse(foundAuthorEntities.get(i)))
                .thenReturn(foundAuthorResponses.get(i));
        }

        BaseResponse<List<AuthorResponse>> serviceResponse = authorService.getAuthors(pageNumber, pageSize);

        Mockito.verify(authorRepository, Mockito.times(1)).findAll(pageable);
        for(int i = 0; i < pageSize; i++)
        {
            Mockito.verify(authorMapper, Mockito.times(1))
                .entityToResponse(foundAuthorEntities.get(i));
        }
        Mockito.verifyNoMoreInteractions(authorRepository, authorMapper);

        Assertions.assertEquals(true, serviceResponse.isSuccess());
        Assertions.assertEquals("Authors retrieved successfully.", serviceResponse.getMessage());
        Assertions.assertEquals(HttpStatus.OK.value(), serviceResponse.getStatus());
        Assertions.assertNotNull(serviceResponse.getData());
        Assertions.assertEquals(2, serviceResponse.getData().size());
        for(int i = 0; i < pageSize; i++) 
        {
            Assertions.assertEquals(foundAuthorResponses.get(i).getId(), serviceResponse.getData().get(i).getId());
            Assertions.assertEquals(foundAuthorResponses.get(i).getFirstName(), serviceResponse.getData().get(i).getFirstName());
            Assertions.assertEquals(foundAuthorResponses.get(i).getLastName(), serviceResponse.getData().get(i).getLastName());
        }
    }

    @Test
    @DisplayName(value = "Testing searchAuthorsByFullName() method")
    public void givenSearchAuthorsByFullNameThenReturnBaseResponseOfListOfAuthors()
    {
        int pageNumber = 0, pageSize = 2;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        List<AuthorEntity> foundAuthorEntities = List
            .of(
                AuthorEntity.builder()
                    .id(1L)
                    .firstName("Nizami")
                    .lastName("Ganjavi")
                    .build(), 
                AuthorEntity.builder()
                    .id(2L)
                    .firstName("Nizami")
                    .lastName("Saracli")
                    .build()
            );

        List<AuthorResponse> foundAuthorResponses = List
            .of(
                AuthorResponse.builder()
                    .id(1L)
                    .firstName("Nizami")
                    .lastName("Ganjavi")
                    .build(), 
                AuthorResponse.builder()
                    .id(2L)
                    .firstName("Nizami")
                    .lastName("Saracli")
                    .build()
            );

        Page<AuthorEntity> foundAuthorPage = new PageImpl<>(foundAuthorEntities);

        Mockito.when(authorRepository.searchByFullName("Nizami", pageable)).thenReturn(foundAuthorPage);
        for(int i = 0; i < pageSize; i++) 
        {
            Mockito.when(authorMapper.entityToResponse(foundAuthorEntities.get(i)))
                .thenReturn(foundAuthorResponses.get(i));
        }

        BaseResponse<List<AuthorResponse>> serviceResponse = authorService
            .searchAuthorsByFullName("Nizami", pageNumber, pageSize);

        Mockito.verify(authorRepository, Mockito.times(1)).searchByFullName("Nizami", pageable);
        for(int i = 0; i < pageSize; i++)
        {
            Mockito.verify(authorMapper, Mockito.times(1))
                .entityToResponse(foundAuthorEntities.get(i));
        }
        Mockito.verifyNoMoreInteractions(authorRepository, authorMapper);

        Assertions.assertEquals(true, serviceResponse.isSuccess());
        Assertions.assertEquals("Authors retrieved successfully.", serviceResponse.getMessage());
        Assertions.assertEquals(HttpStatus.OK.value(), serviceResponse.getStatus());
        Assertions.assertNotNull(serviceResponse.getData());
        Assertions.assertEquals(2, serviceResponse.getData().size());
        for(int i = 0; i < pageSize; i++) 
        {
            Assertions.assertEquals(foundAuthorResponses.get(i).getId(), serviceResponse.getData().get(i).getId());
            Assertions.assertEquals(foundAuthorResponses.get(i).getFirstName(), serviceResponse.getData().get(i).getFirstName());
            Assertions.assertEquals(foundAuthorResponses.get(i).getLastName(), serviceResponse.getData().get(i).getLastName());
        }
    } 

    @Test
    @DisplayName(value = "Testing updateAuthor() method when author exists")
    public void givenUpdateAuthorWhenAuthorExistsThenReturnSuccessResponse()
    {
        AuthorEntity foundAuthorEntity = AuthorEntity
            .builder()
            .id(1L)
            .firstName("Nizami")
            .lastName("Ganjavi")
            .build();

        AuthorRequest updatedAuthorRequest = AuthorRequest
            .builder()
            .firstName("Jafar")
            .lastName("Jabbarli")
            .build(); 

        Mockito.when(authorRepository.findById(1L)).thenReturn(Optional.of(foundAuthorEntity));

        Mockito
            .doAnswer(invocation -> {
                AuthorRequest mapperAuthorRequest = invocation.getArgument(0);
                AuthorEntity mapperAuthorEntity = invocation.getArgument(1);
                mapperAuthorEntity.setFirstName(mapperAuthorRequest.getFirstName());
                mapperAuthorEntity.setLastName(mapperAuthorRequest.getLastName());
                return null; 
            })
            .when(authorMapper)
            .convertRequestToEntity(
                Mockito.any(AuthorRequest.class), 
                Mockito.any(AuthorEntity.class)
            );

        BaseResponse<Void> serviceResponse = authorService.updateAuthor(1L, updatedAuthorRequest);

        Mockito.verify(authorRepository, Mockito.times(1)).findById(1L);
        Mockito.verify(authorMapper, Mockito.times(1))
            .convertRequestToEntity(updatedAuthorRequest, foundAuthorEntity);
        Mockito.verify(authorRepository, Mockito.times(1)).save(foundAuthorEntity);
        Mockito.verifyNoMoreInteractions(authorRepository, authorMapper);

        Assertions.assertEquals(true, serviceResponse.isSuccess());
        Assertions.assertEquals("Author updated successfully.", serviceResponse.getMessage());
        Assertions.assertEquals(HttpStatus.OK.value(), serviceResponse.getStatus());
        Assertions.assertNull(serviceResponse.getData());
    }

    @Test
    @DisplayName(value = "Testing updateAuthor() method when author does not exist")
    public void givenUpdateAuthorWhenAuthorDoesNotExistThenThrowAuthorNotFoundException()
    {
        AuthorRequest updatedAuthorRequest = AuthorRequest
            .builder()
            .firstName("Jafar")
            .lastName("Jabbarli")
            .build();

        Mockito.when(authorRepository.findById(1L)).thenReturn(Optional.empty());

        AuthorNotFoundException exception = Assertions
            .assertThrows(AuthorNotFoundException.class, () -> authorService.updateAuthor(1L, updatedAuthorRequest));
        Assertions.assertEquals("Author not found with id : 1", exception.getMessage());

        Mockito.verify(authorRepository, Mockito.times(1)).findById(1L);
        Mockito.verifyNoMoreInteractions(authorRepository);
    }

    @Test
    @DisplayName(value = "Testing deleteAuthor() method")
    public void givenDeleteAuthorThenReturnSuccessResponse()
    {
        BaseResponse<Void> serviceResponse = authorService.deleteAuthor(1L);
        
        Mockito.verify(authorRepository, Mockito.times(1)).deleteById(1L);
        Mockito.verifyNoMoreInteractions(authorRepository);

        Assertions.assertEquals(true, serviceResponse.isSuccess());
        Assertions.assertEquals("Author deleted successfully.", serviceResponse.getMessage());
        Assertions.assertEquals(HttpStatus.OK.value(), serviceResponse.getStatus());
        Assertions.assertNull(serviceResponse.getData());
    }

    @Test
    @DisplayName(value = "Testing getAuthorEntityById() method when author exists")
    public void givenGetAuthorEntityByIdWhenAuthorExistsThenReturnAuthorEntity()
    {
        AuthorEntity foundAuthorEntity = AuthorEntity
            .builder()
            .id(1L)
            .firstName("Nizami")
            .lastName("Ganjavi")
            .build();

        Mockito.when(authorRepository.findById(1L)).thenReturn(Optional.of(foundAuthorEntity));

        AuthorEntity serviceResponse = authorService.getAuthorEntityById(1L);
        
        Mockito.verify(authorRepository, Mockito.times(1)).findById(1L);
        Mockito.verifyNoMoreInteractions(authorRepository, authorMapper);

        Assertions.assertEquals(foundAuthorEntity.getId(), serviceResponse.getId());
        Assertions.assertEquals(foundAuthorEntity.getFirstName(), serviceResponse.getFirstName());
        Assertions.assertEquals(foundAuthorEntity.getLastName(), serviceResponse.getLastName());
    }

    @Test
    @DisplayName(value = "Testing getAuthorEntityById() method when author does not exist")
    public void givenGetAuthorEntityByIdWhenAuthorDoesNotExistThenThrowAuthorNotFoundException()
    {
        Mockito.when(authorRepository.findById(1L)).thenReturn(Optional.empty());
        
        AuthorNotFoundException exception = Assertions
            .assertThrows(AuthorNotFoundException.class, () -> authorService.getAuthorEntityById(1L));
            
        Assertions.assertEquals("Author not found with id : 1", exception.getMessage());
        
        Mockito.verify(authorRepository, Mockito.times(1)).findById(1L);
        Mockito.verifyNoMoreInteractions(authorRepository);
    }
}
