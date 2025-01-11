package az.atlacademy.libraryadp.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
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
    @ResponseStatus(value = HttpStatus.CREATED)
    public BaseResponse<Void> createAuthor(@RequestBody AuthorRequest authorRequest)
    {
        log.info(LOG_TEMPLATE, "POST", "");
        return authorService.createAuthor(authorRequest);  
    }

    @GetMapping
    @ResponseStatus(value = HttpStatus.OK)
    public BaseResponse<List<AuthorResponse>> getAuthors(
        @RequestParam(value = "pageNumber", required = false, defaultValue = "0") int pageNumber,
        @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize
    ){
        log.info(LOG_TEMPLATE, "GET", "");
        return authorService.getAuthors(pageNumber, pageSize);
    }

    @GetMapping(value = "/search-by-full-name")
    @ResponseStatus(value = HttpStatus.OK)
    public BaseResponse<List<AuthorResponse>> searchAuthorsByFullName(
        @RequestParam(value = "fullName") String fullName,
        @RequestParam(value = "pageNumber", required = false, defaultValue = "0") int pageNumber,
        @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize
    ){
        log.info(LOG_TEMPLATE, "GET", "/search");
        return authorService.searchAuthorsByFullName(fullName, pageNumber, pageSize);
    }

    @GetMapping(value = "/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    public BaseResponse<AuthorResponse> getAuthorById(@PathVariable(value = "id") long authorId)
    {
        log.info(LOG_TEMPLATE, "GET", "/" + authorId);
        return authorService.getAuthorById(authorId);
    }

    @PutMapping(value = "/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    public BaseResponse<Void> updateAuthor(
        @PathVariable(value = "id") long authorId, 
        @RequestBody AuthorRequest authorRequest
    ){
        log.info(LOG_TEMPLATE, "PUT", "/" + authorId);
        return authorService.updateAuthor(authorId, authorRequest);
    }

    @DeleteMapping(value = "/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    public BaseResponse<Void> deleteAuthor(@PathVariable(value = "id") long authorId)
    {
        log.info(LOG_TEMPLATE, "DELETE", "/" + authorId);
        return authorService.deleteAuthor(authorId);
    }
}
