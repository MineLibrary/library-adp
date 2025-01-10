package az.atlacademy.libraryadp.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import az.atlacademy.libraryadp.model.dto.request.AuthorRequest;
import az.atlacademy.libraryadp.model.dto.response.AuthorResponse;
import az.atlacademy.libraryadp.model.dto.response.BaseResponse;
import az.atlacademy.libraryadp.service.AuthorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/author")
public class AuthorController 
{
    private final AuthorService authorService;

    private static final String LOG_TEMPLATE = "{} request to /api/v1/author{}";

    @PostMapping
    public BaseResponse<Void> createAuthor(@RequestBody AuthorRequest authorRequest)
    {
        log.info(LOG_TEMPLATE, "POST", "");
        return authorService.createAuthor(authorRequest);  
    }

    @GetMapping
    public BaseResponse<List<AuthorResponse>> getAuthors(
        @RequestParam(value = "pageNumber", required = false, defaultValue = "0") int pageNumber,
        @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
        @RequestParam(value = "fullName", required = false) String fullName
    ){
        log.info(LOG_TEMPLATE, "GET", "");
        
        if (fullName == null) 
        {
            return authorService.getAuthors(pageNumber, pageSize);
        }
        else
        {
            return authorService.getAuthorsByFullName(fullName, pageNumber, pageSize);
        }
    }

    @GetMapping(value = "/{id}")
    public BaseResponse<AuthorResponse> getAuthorById(@PathVariable(value = "id") long authorId)
    {
        log.info(LOG_TEMPLATE, "GET", "/" + authorId);
        return authorService.getAuthorById(authorId);
    }

    @PutMapping(value = "/{id}")
    public BaseResponse<Void> updateAuthor(
        @PathVariable(value = "id") long authorId, 
        @RequestBody AuthorRequest authorRequest
    ){
        log.info(LOG_TEMPLATE, "PUT", "/" + authorId);
        return authorService.updateAuthor(authorId, authorRequest);
    }

    @DeleteMapping(value = "/{id}")
    public BaseResponse<Void> deleteAuthor(@PathVariable(value = "id") long authorId)
    {
        log.info(LOG_TEMPLATE, "DELETE", "/" + authorId);
        return authorService.deleteAuthor(authorId);
    }
}
