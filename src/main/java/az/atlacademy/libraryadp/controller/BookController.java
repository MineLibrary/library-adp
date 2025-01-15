package az.atlacademy.libraryadp.controller;

import java.io.File;
import java.io.IOException;
import java.net.URLConnection;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import az.atlacademy.libraryadp.model.dto.request.BookRequest;
import az.atlacademy.libraryadp.model.dto.response.BaseResponse;
import az.atlacademy.libraryadp.model.dto.response.BookImageResponse;
import az.atlacademy.libraryadp.model.dto.response.BookResponse;
import az.atlacademy.libraryadp.service.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/book")
public class BookController 
{
    private final BookService bookService;

    private static final String LOG_TEMPLATE = "{} request to /api/v1/book{}";

    @PostMapping
    @ResponseStatus(value = HttpStatus.CREATED)
    public BaseResponse<Void> createBook(@RequestBody BookRequest bookRequest)
    {
        log.info(LOG_TEMPLATE, "POST", "");
        return bookService.createBook(bookRequest); 
    }

    @GetMapping(value = "/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    public BaseResponse<BookResponse> getBookById(@PathVariable(value = "id") long bookId)
    {
        log.info(LOG_TEMPLATE, "GET", "/" + bookId);
        return bookService.getBookById(bookId);
    }

    @GetMapping
    @ResponseStatus(value = HttpStatus.OK)
    public BaseResponse<List<BookResponse>> getBooks(
        @RequestParam(value = "pageNumber", required = false, defaultValue = "0") int pageNumber,
        @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize
    ){
        log.info(LOG_TEMPLATE, "GET", "");
        return bookService.getBooks(pageNumber, pageSize);
    }

    @GetMapping(value = "/get-by-category-id")
    @ResponseStatus(value = HttpStatus.OK)
    public BaseResponse<List<BookResponse>> getBooksByCategoryId(
        @RequestParam(value = "categoryId", required = true) long categoryId,
        @RequestParam(value = "pageNumber", required = false, defaultValue = "0") int pageNumber,
        @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize
    ){
        log.info(LOG_TEMPLATE, "GET", "/get-by-category-id");
        return bookService.getBooksByCategoryId(categoryId, pageNumber, pageSize);
    }

    @GetMapping(value = "/get-by-author-id")
    @ResponseStatus(value = HttpStatus.OK)
    public BaseResponse<List<BookResponse>> getBooksByAuthorId(
        @RequestParam(value = "authorId", required = true) long authorId,
        @RequestParam(value = "pageNumber", required = false, defaultValue = "0") int pageNumber,
        @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize
    ){
        log.info(LOG_TEMPLATE, "GET", "/get-by-author-id");
        return bookService.getBooksByAuthorId(authorId, pageNumber, pageSize);
    }

    @GetMapping(value = "/search-by-title")
    public BaseResponse<List<BookResponse>> searchBooksByTitle(
        @RequestParam(value = "title", required = true) String title,
        @RequestParam(value = "pageNumber", required = false, defaultValue = "0") int pageNumber,
        @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize
    ){
        log.info(LOG_TEMPLATE, "GET", "/search-by-title");
        return bookService.searchBooksByTitle(title, pageNumber, pageSize);
    }

    @PutMapping(value = "/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    public BaseResponse<Void> updateBook(
        @PathVariable(value = "id") long bookId, 
        @RequestBody BookRequest bookRequest
    ){
        log.info(LOG_TEMPLATE, "PUT", "/" + bookId);
        return bookService.updateBook(bookId, bookRequest);
    }

    @DeleteMapping(value = "/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    public BaseResponse<Void> deleteBook(@PathVariable(value = "id") long bookId)
    {
        log.info(LOG_TEMPLATE, "DELETE", "/" + bookId);
        return bookService.deleteBook(bookId);
    }

    @PatchMapping(value = "/{id}/update-stock")
    @ResponseStatus(value = HttpStatus.OK)
    public BaseResponse<Void> updateBookStock(
        @PathVariable(value = "id") long bookId,
        @RequestParam(value = "stock", required = true) int stock
    ){
        log.info(LOG_TEMPLATE, "PATCH", "/" + bookId + "/update-stock");
        return bookService.updateBookStock(bookId, stock);
    }

    @PostMapping(value = "/{id}/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseResponse<Void>> uploadBookImage(
        @PathVariable(value = "id") long bookId,
        @RequestParam(value = "file", required = true) MultipartFile file
    ){
        log.info(LOG_TEMPLATE, "POST", "/" + bookId + "/upload-image");

        try
        {
            File tempFile = File.createTempFile("upload-", file.getOriginalFilename()); 
            file.transferTo(tempFile);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(bookService.uploadBookImage(bookId, tempFile));
        }
        catch (IOException e) 
        {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                        BaseResponse.<Void>builder()
                            .message("Failed to retrieve image: " + e.getMessage().getBytes())
                            .success(false)
                            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build()); 
        }
    }

    @GetMapping(value = "/{id}/image")
    public ResponseEntity<byte[]> getBookImage(@PathVariable(value = "id") long bookId)
    {
        log.info(LOG_TEMPLATE, "GET", "/" + bookId + "/image");

        BaseResponse<BookImageResponse> imageResponse = bookService.getBookImage(bookId);

        String contentType = URLConnection.guessContentTypeFromName(imageResponse.getData().getFileKey());
        if (contentType == null) 
        {
            contentType = "application/octet-stream";    
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .header("Content-Type", contentType)
                .body(imageResponse.getData().getImageData()); 
    }
}
