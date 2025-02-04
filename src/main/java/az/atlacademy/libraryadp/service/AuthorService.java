package az.atlacademy.libraryadp.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import az.atlacademy.libraryadp.exception.AuthorNotFoundException;
import az.atlacademy.libraryadp.mapper.AuthorMapper;
import az.atlacademy.libraryadp.model.dto.request.AuthorRequest;
import az.atlacademy.libraryadp.model.dto.response.AuthorResponse;
import az.atlacademy.libraryadp.model.dto.response.BaseResponse;
import az.atlacademy.libraryadp.model.entity.AuthorEntity;
import az.atlacademy.libraryadp.repository.AuthorRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthorService 
{
    private final AuthorRepository authorRepository; 
    private final AuthorMapper authorMapper; 

    @Transactional
    public BaseResponse<Void> createAuthor(AuthorRequest authorRequest)
    {
        AuthorEntity authorEntity = authorMapper.requestToEntity(authorRequest); 

        authorRepository.save(authorEntity);

        log.info("Created new author: {}", authorEntity.toString());
        
        return BaseResponse.<Void>builder()
                .success(true)
                .status(HttpStatus.CREATED.value())
                .message("Author created successfully.")
                .build();
    }

    public BaseResponse<AuthorResponse> getAuthorById(Long id)
    {
        AuthorEntity authorEntity = authorRepository.findById(id)
            .orElseThrow(() -> new AuthorNotFoundException("Author not found with id : " + id));
    
        AuthorResponse authorResponse = authorMapper.entityToResponse(authorEntity); 

        log.info("Retrieved author with id: {}", id);

        return BaseResponse.<AuthorResponse>builder()
                .success(true)
                .data(authorResponse)
                .message("Author retrieved successfully.")
                .status(HttpStatus.OK.value())
                .build();
    }

    public BaseResponse<List<AuthorResponse>> getAuthors(int pageNumber, int pageSize)
    {
        Pageable pageable = PageRequest.of(pageNumber, pageSize); 
        Page<AuthorEntity> authorPage = authorRepository.findAll(pageable); 

        List<AuthorEntity> authorEntities = authorPage.getContent(); 

        List<AuthorResponse> authorResponses = authorEntities.stream()
            .map(authorMapper::entityToResponse).collect(Collectors.toList());

        log.info("Retrieved all authors (page: {}, size: {})", pageNumber, pageSize);

        return BaseResponse.<List<AuthorResponse>>builder()
                .success(true)
                .data(authorResponses)
                .message("Authors retrieved successfully.")
                .status(HttpStatus.OK.value())
                .build();
    }

    public BaseResponse<List<AuthorResponse>> searchAuthorsByFullName(
        String fullName, int pageNumber, int pageSize
    ){
        Pageable pageable = PageRequest.of(pageNumber, pageSize); 
        Page<AuthorEntity> authorPage = authorRepository.searchByFullName(fullName, pageable);

        List<AuthorEntity> authorEntities = authorPage.getContent();

        List<AuthorResponse> authorResponses = authorEntities.stream()
            .map(authorMapper::entityToResponse).collect(Collectors.toList());

        log.info("Retrieved authors by full name (page: {}, size: {})", pageNumber, pageSize);

        return BaseResponse.<List<AuthorResponse>>builder()
                .success(true)
                .data(authorResponses)
                .message("Authors retrieved successfully.")
                .status(HttpStatus.OK.value())
                .build();
    }

    @Transactional
    public BaseResponse<Void> updateAuthor(Long id, AuthorRequest authorRequest)
    {
        AuthorEntity authorEntity = authorRepository.findById(id)
            .orElseThrow(() -> new AuthorNotFoundException("Author not found with id : " + id));

        authorMapper.convertRequestToEntity(authorRequest, authorEntity);
        authorRepository.save(authorEntity);

        log.info("Updated author with id: {}", id);

        return BaseResponse.<Void>builder()
                .success(true)
                .message("Author updated successfully.")
                .status(HttpStatus.OK.value())
                .build();
    }

    @Transactional
    public BaseResponse<Void> deleteAuthor(Long id)
    {
        authorRepository.deleteById(id);
        
        log.info("Deleted author with id: {}", id);

        return BaseResponse.<Void>builder()
                .success(true)
                .message("Author deleted successfully.")
                .status(HttpStatus.OK.value())
                .build();
    }

    protected AuthorEntity getAuthorEntityById(long authorId)
    {
        return authorRepository.findById(authorId)
           .orElseThrow(() -> new AuthorNotFoundException("Author not found with id : " + authorId));
    }
}
