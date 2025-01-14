package az.atlacademy.libraryadp.service;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import az.atlacademy.libraryadp.exception.BookNotFoundException;
import az.atlacademy.libraryadp.mapper.BookMapper;
import az.atlacademy.libraryadp.model.dto.request.BookRequest;
import az.atlacademy.libraryadp.model.dto.response.BaseResponse;
import az.atlacademy.libraryadp.model.dto.response.BookResponse;
import az.atlacademy.libraryadp.model.entity.BookEntity;
import az.atlacademy.libraryadp.repository.BookRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookService 
{
    private final BookRepository bookRepository;
    private final BookMapper bookMapper; 
    private final CategoryService categoryService; 
    private final AuthorService authorService; 
    private final AmazonS3Service amazonS3Service; 

    @Value("${appliation.image-storage-default-image-file-name.books}")
    private String defaultImageFileName; 

    @Value("${appliation.image-storage-folders.books}")
    private String imageFolder; 

    @Transactional
    public BaseResponse<Void> createBook(BookRequest bookRequest)
    {
        BookEntity bookEntity = bookMapper.requestToEntity(bookRequest);
        
        bookEntity.setCategory(categoryService.getCategoryEntityById(bookRequest.getCategoryId()));
        bookEntity.setAuthors(
            bookRequest.getAuthorIds().stream().map(authorService::getAuthorEntityById).collect(Collectors.toList())
        );

        bookRepository.save(bookEntity);

        log.info("Created new book : {}", bookEntity.getTitle());

        return BaseResponse.<Void>builder()
               .success(true)
               .status(HttpStatus.CREATED.value())
               .message("Book created successfully.")
               .build();
    }

    public BaseResponse<BookResponse> getBookById(long bookId)
    {
        BookEntity bookEntity = bookRepository.findById(bookId)
            .orElseThrow(() -> new BookNotFoundException("Not found book with id : " + bookId)); 
        
        BookResponse bookResponse = bookMapper.entityToResponse(bookEntity);

        log.info("Retrieved book with id: {}", bookId);

        return BaseResponse.<BookResponse>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .data(bookResponse)
                .message("Book retrieved successfully.")
                .build();
    }

    public BaseResponse<List<BookResponse>> getBooks(int pageNumber, int pageSize)
    {
        Pageable pageable = PageRequest.of(pageNumber, pageSize); 
        Page<BookEntity> bookPage = bookRepository.findAll(pageable);

        List<BookEntity> bookEntities = bookPage.getContent(); 
        
        List<BookResponse> bookResponses = bookEntities.stream()
            .map(bookMapper::entityToResponse).collect(Collectors.toList());

        log.info("Retrieved books (page: {}, size: {})", pageNumber, pageSize);

        return BaseResponse.<List<BookResponse>>builder()
                .status(HttpStatus.OK.value())
                .success(true)
                .data(bookResponses)
                .message("Books retrieved successfully.")
                .build();
    }

    public BaseResponse<List<BookResponse>> getBooksByCategoryId(
        long categoryId, int pageNumber, int pageSize
    ){

        Pageable pageable = PageRequest.of(pageNumber, pageSize); 
        Page<BookEntity> bookPage = bookRepository
            .findByCategory(categoryService.getCategoryEntityById(categoryId), pageable); 

        List<BookEntity> bookEntities = bookPage.getContent(); 
        
        List<BookResponse> bookResponses = bookEntities.stream()
            .map(bookMapper::entityToResponse).collect(Collectors.toList());

        log.info("Retrieved books by category id (page: {}, size: {})", pageNumber, pageSize);

        return BaseResponse.<List<BookResponse>>builder()
                .status(HttpStatus.OK.value())
                .success(true)
                .data(bookResponses)
                .message("Books retrieved successfully.")
                .build();
    }

    public BaseResponse<List<BookResponse>> getBooksByAuthorId(
        long authorId, int pageNumber, int pageSize
    ){
        Pageable pageable = PageRequest.of(pageNumber, pageSize); 
        Page<BookEntity> bookPage = bookRepository
            .findByAuthor(authorService.getAuthorEntityById(authorId), pageable);

        List<BookEntity> bookEntities = bookPage.getContent(); 
        
        List<BookResponse> bookResponses = bookEntities.stream()
            .map(bookMapper::entityToResponse).collect(Collectors.toList());

        log.info("Retrieved books by author id (page: {}, size: {})", pageNumber, pageSize);

        return BaseResponse.<List<BookResponse>>builder()
                .status(HttpStatus.OK.value())
                .success(true)
                .data(bookResponses)
                .message("Books retrieved successfully.")
                .build();
    }

    public BaseResponse<List<BookResponse>> searchBooksByTitle(
        String bookTitle, int pageNumber, int pageSize
    ){
        Pageable pageable = PageRequest.of(pageNumber, pageSize); 
        Page<BookEntity> bookPage = bookRepository.searchByTitle(bookTitle, pageable);

        List<BookEntity> bookEntities = bookPage.getContent(); 
        
        List<BookResponse> bookResponses = bookEntities.stream()
            .map(bookMapper::entityToResponse).collect(Collectors.toList());

        log.info("Retrieved books by title (page: {}, size: {})", pageNumber, pageSize);

        return BaseResponse.<List<BookResponse>>builder()
                .status(HttpStatus.OK.value())
                .success(true)
                .data(bookResponses)
                .message("Books retrieved successfully.")
                .build();
    }
    
    @Transactional
    public BaseResponse<Void> updateBook(long bookId, BookRequest bookRequest)
    {
        BookEntity bookEntity = bookRepository.findById(bookId)
            .orElseThrow(() -> new BookNotFoundException("Book not found with id : " + bookId));

        bookMapper.convertRequestToEntity(bookRequest, bookEntity);

        bookEntity.setCategory(categoryService.getCategoryEntityById(bookRequest.getCategoryId()));
        bookEntity.setAuthors(
            bookRequest.getAuthorIds().stream().map(authorService::getAuthorEntityById).collect(Collectors.toList())
        );

        bookRepository.save(bookEntity);

        log.info("Updated book with id: {}", bookId);

        return BaseResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .success(true)
                .message("Book updated successfully.")
                .build();
    }

    @Transactional
    public BaseResponse<Void> deleteBook(long bookId)
    {
        bookRepository.deleteById(bookId);

        log.info("Deleted book with id: {}", bookId);

        return BaseResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .success(true)
                .message("Book deleted successfully.")
                .build();
    }

    @Transactional
    public BaseResponse<Void> updateBookStock(long bookId, int stock)
    {
        BookEntity bookEntity = bookRepository.findById(bookId)
            .orElseThrow(() -> new BookNotFoundException("Not found book with id : " + bookId));

        bookEntity.setStock(stock);
        bookRepository.save(bookEntity); 

        log.info("Updated stock for book with id : {}", bookId);

        return BaseResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .success(true)
                .message("Book stock updated successfully.")
                .build();
    }
    
    @Transactional
    public BaseResponse<Void> uploadBookImage(long bookId, File bookImage)
    {
        BookEntity bookEntity = getBookEntityById(bookId);
        
        String imageName = bookImage.getName();
        String fileKey = imageFolder + "/" + bookEntity.getId() + imageName.substring(imageName.lastIndexOf("."));

        amazonS3Service.uploadFile(fileKey, bookImage);

        bookEntity.setS3FileKey(fileKey);
        bookRepository.save(bookEntity);

        log.info("Uploaded book image for book with id : {}", bookId);

        return BaseResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .success(true)
                .message("Book image uploaded successfully.")
                .build();
    }

    public BaseResponse<byte[]> getBookImage(long bookId)
    {
        BookEntity bookEntity = getBookEntityById(bookId);

        String fileKey; 

        if ((fileKey = bookEntity.getS3FileKey()) == null) 
        {
            fileKey = imageFolder + "/" + defaultImageFileName;    
        }

        BaseResponse<byte[]> response = amazonS3Service.getFile(fileKey);
        response.setMessage("Book image retrieved successfully.");

        log.info("Image retrieved successfully for book with id : {}", bookEntity.getId());
    
        return response;
    }

    protected BookEntity getBookEntityById(long bookId)
    {
        return bookRepository.findById(bookId)
            .orElseThrow(() -> new BookNotFoundException("Not found book with id : " + bookId));
    }
}
