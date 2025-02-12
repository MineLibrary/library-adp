package az.atlacademy.libraryadp.service;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.test.util.ReflectionTestUtils;

import az.atlacademy.libraryadp.exception.AuthorNotFoundException;
import az.atlacademy.libraryadp.exception.BookNotFoundException;
import az.atlacademy.libraryadp.exception.BookOutOfStockException;
import az.atlacademy.libraryadp.exception.CategoryNotFoundException;
import az.atlacademy.libraryadp.mapper.BookMapper;
import az.atlacademy.libraryadp.model.dto.request.BookRequest;
import az.atlacademy.libraryadp.model.dto.response.AuthorResponse;
import az.atlacademy.libraryadp.model.dto.response.BaseResponse;
import az.atlacademy.libraryadp.model.dto.response.BookImageResponse;
import az.atlacademy.libraryadp.model.dto.response.BookResponse;
import az.atlacademy.libraryadp.model.dto.response.CategoryResponse;
import az.atlacademy.libraryadp.model.entity.AuthorEntity;
import az.atlacademy.libraryadp.model.entity.BookEntity;
import az.atlacademy.libraryadp.model.entity.CategoryEntity;
import az.atlacademy.libraryadp.repository.BookRepository;

@ExtendWith(value = MockitoExtension.class)
public class BookServiceTest 
{
    @InjectMocks
    private BookService bookService;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookMapper bookMapper; 

    @Mock 
    private CategoryService categoryService;

    @Mock
    private AuthorService authorService;

    @Mock
    private AmazonS3Service amazonS3Service;


    @BeforeEach
    public void setUp()
    {
        ReflectionTestUtils.setField(bookService, "defaultImageFileName", "default.webp");
        ReflectionTestUtils.setField(bookService, "imageFolder", "books");
    }

    @Test
    @DisplayName(value = "Testing createBook() method when authors and category exist")
    public void givenCreateBookWhenAuthorsAndCategoryExistThenReturnSuccessResponse()
    {
        List<Long> authorIds = List.of(1L, 2L);
        int authorsSize = authorIds.size();

        BookRequest createBookRequest = BookRequest.builder()
            .authorIds(authorIds)
            .categoryId(1L)
            .title("Tiny Pretty Things")
            .stock(1)
            .build(); 

        CategoryEntity foundCategoryEntity = CategoryEntity.builder().id(1L).name("Dram").build(); 

        Set<AuthorEntity> foundAuthorEntities = Set
            .of(
                AuthorEntity.builder()
                    .id(1L)
                    .firstName("Sona")
                    .lastName("Charaipotra")
                    .build(),
                AuthorEntity.builder()
                    .id(2L)
                    .firstName("Dhonielle")
                    .lastName("Clayton")
                    .build()
            ); 

        BookEntity createBookEntity = BookEntity.builder()
            .title("Tiny Pretty Things")
            .stock(1)
            .build(); 

        BookEntity createBookEntityWithRelations = BookEntity.builder()
            .title("Tiny Pretty Things")
            .stock(1)
            .authors(foundAuthorEntities)
            .category(foundCategoryEntity)
            .build(); 
        
        BookEntity createdBookEntity = BookEntity.builder()
            .id(1L)
            .title("Tiny Pretty Things")
            .stock(1)
            .authors(foundAuthorEntities)
            .category(foundCategoryEntity)
            .build(); 

        Mockito.when(bookMapper.requestToEntity(createBookRequest)).thenReturn(createBookEntity);
        Mockito.when(categoryService.getCategoryEntityById(1L)).thenReturn(foundCategoryEntity);

        Iterator<AuthorEntity> authorEntityIterator = foundAuthorEntities.iterator();
        for(int i = 0; i < authorsSize; i++) 
        {
            Mockito.when(authorService.getAuthorEntityById(authorIds.get(i))).thenReturn(authorEntityIterator.next());
        }

        Mockito.when(bookRepository.save(createBookEntityWithRelations)).thenReturn(createdBookEntity);

        BaseResponse<Void> serviceResponse = bookService.createBook(createBookRequest);

        Mockito.verify(bookMapper, Mockito.times(1)).requestToEntity(createBookRequest);
        Mockito.verify(categoryService, Mockito.times(1)).getCategoryEntityById(1L);
        for(int i = 0; i < authorsSize; i++)
        {
            Mockito.verify(authorService, Mockito.times(1)).getAuthorEntityById(authorIds.get(i));
        }
        Mockito.verify(bookRepository, Mockito.times(1)).save(createBookEntityWithRelations);
        Mockito.verifyNoMoreInteractions(bookMapper, categoryService, authorService, bookRepository, amazonS3Service);

        Assertions.assertEquals(true, serviceResponse.isSuccess());
        Assertions.assertEquals("Book created successfully.", serviceResponse.getMessage());
        Assertions.assertEquals(HttpStatus.CREATED.value(), serviceResponse.getStatus());
        Assertions.assertNull(serviceResponse.getData());
    }

    @Test
    @DisplayName(value = "Testing createBook() method when category does not exist")
    public void givenCreateBookWhenCategoryDoesNotExistThenThrowCategoryNotFoundException()
    {
        List<Long> authorIds = List.of(1L, 2L);

        BookRequest createBookRequest = BookRequest.builder()
            .authorIds(authorIds)
            .categoryId(1L)
            .title("Tiny Pretty Things")
            .stock(1)
            .build(); 

        BookEntity createBookEntity = BookEntity.builder()
            .title("Tiny Pretty Things")
            .stock(1)
            .build();

        Mockito.when(bookMapper.requestToEntity(createBookRequest)).thenReturn(createBookEntity);
        Mockito.when(categoryService.getCategoryEntityById(1L))
            .thenThrow(new CategoryNotFoundException("Category not found with id : 1"));

        Assertions.assertThrows(CategoryNotFoundException.class, () -> bookService.createBook(createBookRequest));

        Mockito.verify(bookMapper, Mockito.times(1)).requestToEntity(createBookRequest);
        Mockito.verify(categoryService, Mockito.times(1)).getCategoryEntityById(1L);
        Mockito.verifyNoMoreInteractions(bookMapper, categoryService, authorService, bookRepository, amazonS3Service);
    }

    @Test
    @DisplayName(value = "Testing createBook() method when authors do not exist")
    public void givenCreateBookWhenAuthorsDoNotExistThenThrowAuthorNotFoundException()
    {
        List<Long> authorIds = List.of(1L, 2L);

        BookRequest createBookRequest = BookRequest.builder()
            .authorIds(authorIds)
            .categoryId(1L)
            .title("Tiny Pretty Things")
            .stock(1)
            .build(); 

        BookEntity createBookEntity = BookEntity.builder()
            .title("Tiny Pretty Things")
            .stock(1)
            .build();

        CategoryEntity foundCategoryEntity = CategoryEntity.builder().id(1L).name("Dram").build(); 

        Mockito.when(bookMapper.requestToEntity(createBookRequest)).thenReturn(createBookEntity);
        Mockito.when(categoryService.getCategoryEntityById(1L)).thenReturn(foundCategoryEntity);
        Mockito.when(authorService.getAuthorEntityById(authorIds.get(0)))
            .thenThrow(new AuthorNotFoundException("Author not found with id : " + authorIds.get(0)));

        Assertions.assertThrows(AuthorNotFoundException.class, () -> bookService.createBook(createBookRequest));

        Mockito.verify(bookMapper, Mockito.times(1)).requestToEntity(createBookRequest);
        Mockito.verify(categoryService, Mockito.times(1)).getCategoryEntityById(1L);
        Mockito.verify(authorService, Mockito.times(1)).getAuthorEntityById(authorIds.get(0));
        Mockito.verifyNoMoreInteractions(bookMapper, categoryService, authorService, bookRepository, amazonS3Service);
    }

    @Test
    @DisplayName(value = "Testing getBooksByAuthorId() method when author exists")
    public void givenGetBooksByAuthorIdWhenAuthorExistsThenReturnBaseResponseOfListOfBooks()
    {
        long authorId = 1L; 
        int pageNumber = 0, pageSize = 2; 
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        CategoryEntity foundBooksCategoryEntity = CategoryEntity.builder().id(1L).name("Dram").build(); 
        CategoryResponse foundBooksCategoryResponse = CategoryResponse.builder().id(1L).name("Dram").build();

        AuthorEntity foundAuthorEntity = AuthorEntity.builder()
            .id(authorId)
            .firstName("Nizami")
            .lastName("Ganjavi")
            .build();
        
        AuthorResponse foundAuthorResponse = AuthorResponse.builder()
            .id(authorId)
            .firstName("Nizami")
            .lastName("Ganjavi")
            .build();

        Set<AuthorEntity> foundBooksAuthorEntities = Set.of(foundAuthorEntity); 
        List<AuthorResponse> foundBooksAuthorResponses = List.of(foundAuthorResponse);

        List<BookEntity> foundBookEntities = List.of(
            BookEntity.builder()
                .id(1L)
                .title("Xamsa")
                .stock(1)
                .category(foundBooksCategoryEntity)
                .authors(foundBooksAuthorEntities)
                .build(),
            BookEntity.builder()
                .id(2L)
                .title("Leyli and Majnun")
                .stock(1)
                .category(foundBooksCategoryEntity)
                .authors(foundBooksAuthorEntities)
                .build()
        );

        List<BookResponse> foundBookResponses = List.of(
            BookResponse.builder()
                .id(1L)
                .title("Xamsa")
                .stock(1)
                .category(foundBooksCategoryResponse)
                .authors(foundBooksAuthorResponses) 
                .build(),
            BookResponse.builder()
                .id(2L)
                .title("Leyli and Majnun")
                .stock(1)
                .category(foundBooksCategoryResponse)
                .authors(foundBooksAuthorResponses)
                .build()
        );

        Page<BookEntity> bookPage = new PageImpl<>(foundBookEntities);

        Mockito.when(authorService.getAuthorEntityById(authorId)).thenReturn(foundAuthorEntity);
        Mockito.when(bookRepository.findByAuthor(foundAuthorEntity, pageable)).thenReturn(bookPage);
        for(int i = 0; i < pageSize; i++) 
        {
            Mockito.when(bookMapper.entityToResponse(foundBookEntities.get(i)))
                .thenReturn(foundBookResponses.get(i));
        }

        BaseResponse<List<BookResponse>> serviceResponse = bookService.getBooksByAuthorId(authorId, pageNumber, pageSize);
        
        Mockito.verify(authorService, Mockito.times(1)).getAuthorEntityById(authorId);
        Mockito.verify(bookRepository, Mockito.times(1)).findByAuthor(foundAuthorEntity, pageable);
        for(int i = 0; i < pageSize; i++)
        {
            Mockito.verify(bookMapper, Mockito.times(1)).entityToResponse(foundBookEntities.get(i));
        }
        Mockito.verifyNoMoreInteractions(bookMapper, categoryService, authorService, bookRepository, amazonS3Service);

        Assertions.assertEquals(true, serviceResponse.isSuccess());
        Assertions.assertEquals("Books retrieved successfully.", serviceResponse.getMessage());
        Assertions.assertEquals(HttpStatus.OK.value(), serviceResponse.getStatus());
        Assertions.assertNotNull(serviceResponse.getData());
        for(int i = 0; i < pageSize; i++)
        {
            Assertions.assertEquals(foundBookResponses.get(i).getId(), serviceResponse.getData().get(i).getId());
            Assertions.assertEquals(foundBookResponses.get(i).getTitle(), serviceResponse.getData().get(i).getTitle());
            Assertions.assertEquals(foundBookResponses.get(i).getStock(), serviceResponse.getData().get(i).getStock());
            Assertions.assertEquals(foundBookResponses.get(i).getCategory(), serviceResponse.getData().get(i).getCategory());
            Assertions.assertEquals(foundBookResponses.get(i).getAuthors(), serviceResponse.getData().get(i).getAuthors());
        }
    }

    @Test
    @DisplayName(value = "Testing getBooksByAuthorId() method when author does not exist")
    public void givenGetBooksByAuthorIdWhenAuthorDoesNotExistThenThrowAuthorNotFoundException()
    {
        long authorId = 1L; 
        int pageNumber = 0, pageSize = 2; 

        Mockito.when(authorService.getAuthorEntityById(authorId))
            .thenThrow(new AuthorNotFoundException("Author not found with id : " + authorId));
            
        Assertions
            .assertThrows(
                AuthorNotFoundException.class, 
                () -> bookService.getBooksByAuthorId(authorId, pageNumber, pageSize)
            );

        Mockito.verify(authorService, Mockito.times(1)).getAuthorEntityById(authorId);
        Mockito.verifyNoMoreInteractions(bookMapper, categoryService, authorService, bookRepository, amazonS3Service);
    }

    @Test
    @DisplayName(value = "Testing getBookById() method when book exists")
    public void givenGetBookByIdWhenBookExistsThenReturnBaseResponseOfBookResponse()
    {
        CategoryEntity foundBookCategoryEntity = CategoryEntity.builder().id(1L).name("Dram").build(); 
        CategoryResponse foundBookCategoryResponse = CategoryResponse.builder().id(1L).name("Dram").build();

        AuthorEntity foundAuthorEntity = AuthorEntity.builder()
            .id(1L)
            .firstName("Nizami")
            .lastName("Ganjavi")
            .build();
        
        AuthorResponse foundAuthorResponse = AuthorResponse.builder()
            .id(1L)
            .firstName("Nizami")
            .lastName("Ganjavi")
            .build();

        Set<AuthorEntity> foundBookAuthorEntities = Set.of(foundAuthorEntity); 
        List<AuthorResponse> foundBookAuthorResponses = List.of(foundAuthorResponse);

        BookEntity foundBookEntity = BookEntity.builder()
            .id(1L)
            .title("Xamsa")
            .stock(1)
            .category(foundBookCategoryEntity)
            .authors(foundBookAuthorEntities)
            .build();

        BookResponse foundBookResponse = BookResponse.builder()
            .id(1L)
            .title("Xamsa")
            .stock(1)
            .category(foundBookCategoryResponse)
            .authors(foundBookAuthorResponses)
            .build();

        Mockito.when(bookRepository.findById(foundBookEntity.getId())).thenReturn(Optional.of(foundBookEntity));
        Mockito.when(bookMapper.entityToResponse(foundBookEntity)).thenReturn(foundBookResponse);

        BaseResponse<BookResponse> serviceResponse = bookService.getBookById(foundBookEntity.getId());

        Mockito.verify(bookRepository, Mockito.times(1)).findById(foundBookEntity.getId());
        Mockito.verify(bookMapper, Mockito.times(1)).entityToResponse(foundBookEntity);
        Mockito.verifyNoMoreInteractions(bookMapper, categoryService, authorService, bookRepository, amazonS3Service);

        Assertions.assertEquals(true, serviceResponse.isSuccess());
        Assertions.assertEquals("Book retrieved successfully.", serviceResponse.getMessage());
        Assertions.assertEquals(HttpStatus.OK.value(), serviceResponse.getStatus());
        Assertions.assertNotNull(serviceResponse.getData());
        Assertions.assertEquals(foundBookResponse.getId(), serviceResponse.getData().getId());
        Assertions.assertEquals(foundBookResponse.getTitle(), serviceResponse.getData().getTitle());
        Assertions.assertEquals(foundBookResponse.getStock(), serviceResponse.getData().getStock());
        Assertions.assertEquals(foundBookResponse.getCategory(), serviceResponse.getData().getCategory());
        Assertions.assertEquals(foundBookResponse.getAuthors(), serviceResponse.getData().getAuthors());
    }

    @Test
    @DisplayName(value = "Testing getBookById() method when book does not exist")
    public void givenGetBookByIdWhenBookDoesNotExistThenThrowBookNotFoundException()
    {
        Mockito.when(bookRepository.findById(1L)).thenReturn(Optional.empty());
        
        BookNotFoundException exception = Assertions
            .assertThrows(BookNotFoundException.class, () -> bookService.getBookById(1L));

        Assertions.assertEquals("Book not found with id : 1", exception.getMessage());

        Mockito.verify(bookRepository, Mockito.times(1)).findById(1L);
        Mockito.verifyNoMoreInteractions(bookMapper, categoryService, authorService, bookRepository, amazonS3Service);
    }

    @Test
    @DisplayName(value = "Testing getBooks() method")
    public void givenGetBooksThenReturnBaseResponseOfListOfBooks()
    {
        int pageNumber = 0, pageSize = 2; 
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        CategoryEntity foundBooksCategoryEntity = CategoryEntity.builder().id(1L).name("Dram").build(); 
        CategoryResponse foundBooksCategoryResponse = CategoryResponse.builder().id(1L).name("Dram").build();

        AuthorEntity foundAuthorEntity = AuthorEntity.builder()
            .id(1L)
            .firstName("Nizami")
            .lastName("Ganjavi")
            .build();
        
        AuthorResponse foundAuthorResponse = AuthorResponse.builder()
            .id(1L)
            .firstName("Nizami")
            .lastName("Ganjavi")
            .build();

        Set<AuthorEntity> foundBooksAuthorEntities = Set.of(foundAuthorEntity); 
        List<AuthorResponse> foundBooksAuthorResponses = List.of(foundAuthorResponse);

        List<BookEntity> foundBookEntities = List.of(
            BookEntity.builder()
                .id(1L)
                .title("Xamsa")
                .stock(1)
                .category(foundBooksCategoryEntity)
                .authors(foundBooksAuthorEntities)
                .build(),
            BookEntity.builder()
                .id(2L)
                .title("Leyli and Majnun")
                .stock(1)
                .category(foundBooksCategoryEntity)
                .authors(foundBooksAuthorEntities)
                .build()
        );

        List<BookResponse> foundBookResponses = List.of(
            BookResponse.builder()
                .id(1L)
                .title("Xamsa")
                .stock(1)
                .category(foundBooksCategoryResponse)
                .authors(foundBooksAuthorResponses) 
                .build(),
            BookResponse.builder()
                .id(2L)
                .title("Leyli and Majnun")
                .stock(1)
                .category(foundBooksCategoryResponse)
                .authors(foundBooksAuthorResponses)
                .build()
        );

        Page<BookEntity> bookPage = new PageImpl<>(foundBookEntities);

        Mockito.when(bookRepository.findAll(pageable)).thenReturn(bookPage);
        for(int i = 0; i < pageSize; i++) 
        {
            Mockito.when(bookMapper.entityToResponse(foundBookEntities.get(i)))
                .thenReturn(foundBookResponses.get(i));
        }
        
        BaseResponse<List<BookResponse>> serviceResponse = bookService.getBooks(pageNumber, pageSize);

        Mockito.verify(bookRepository, Mockito.times(1)).findAll(pageable);
        for(int i = 0; i < pageSize; i++)
        {
            Mockito.verify(bookMapper, Mockito.times(1)).entityToResponse(foundBookEntities.get(i));
        }
        Mockito.verifyNoMoreInteractions(bookMapper, categoryService, authorService, bookRepository, amazonS3Service);

        Assertions.assertEquals(true, serviceResponse.isSuccess());
        Assertions.assertEquals("Books retrieved successfully.", serviceResponse.getMessage());
        Assertions.assertEquals(HttpStatus.OK.value(), serviceResponse.getStatus());
        Assertions.assertNotNull(serviceResponse.getData());
        Assertions.assertEquals(foundBookResponses.size(), serviceResponse.getData().size());
        for(int i = 0; i < pageSize; i++)
        {
            Assertions.assertEquals(foundBookResponses.get(i).getId(), serviceResponse.getData().get(i).getId());
            Assertions.assertEquals(foundBookResponses.get(i).getTitle(), serviceResponse.getData().get(i).getTitle());
            Assertions.assertEquals(foundBookResponses.get(i).getStock(), serviceResponse.getData().get(i).getStock());
            Assertions.assertEquals(foundBookResponses.get(i).getCategory(), serviceResponse.getData().get(i).getCategory());
            Assertions.assertEquals(foundBookResponses.get(i).getAuthors(), serviceResponse.getData().get(i).getAuthors());
        }
    }

    @Test
    @DisplayName(value = "Testing getBooksByCategoryId() method when category exists")
    public void givenGetBooksByCategoryIdWhenCategoryExistsThenReturnBaseResponseOfListOfBooks()
    {
        long categoryId = 1L; 
        int pageNumber = 0, pageSize = 2; 
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        CategoryEntity foundBooksCategoryEntity = CategoryEntity.builder().id(categoryId).name("Dram").build(); 
        CategoryResponse foundBooksCategoryResponse = CategoryResponse.builder().id(categoryId).name("Dram").build();

        AuthorEntity foundAuthorEntity = AuthorEntity.builder()
            .id(1L)
            .firstName("Nizami")
            .lastName("Ganjavi")
            .build();
        
        AuthorResponse foundAuthorResponse = AuthorResponse.builder()
            .id(1L)
            .firstName("Nizami")
            .lastName("Ganjavi")
            .build();

        Set<AuthorEntity> foundBooksAuthorEntities = Set.of(foundAuthorEntity); 
        List<AuthorResponse> foundBooksAuthorResponses = List.of(foundAuthorResponse);

        List<BookEntity> foundBookEntities = List.of(
            BookEntity.builder()
                .id(1L)
                .title("Xamsa")
                .stock(1)
                .category(foundBooksCategoryEntity)
                .authors(foundBooksAuthorEntities)
                .build(),
            BookEntity.builder()
                .id(2L)
                .title("Leyli and Majnun")
                .stock(1)
                .category(foundBooksCategoryEntity)
                .authors(foundBooksAuthorEntities)
                .build()
        );

        List<BookResponse> foundBookResponses = List.of(
            BookResponse.builder()
                .id(1L)
                .title("Xamsa")
                .stock(1)
                .category(foundBooksCategoryResponse)
                .authors(foundBooksAuthorResponses) 
                .build(),
            BookResponse.builder()
                .id(2L)
                .title("Leyli and Majnun")
                .stock(1)
                .category(foundBooksCategoryResponse)
                .authors(foundBooksAuthorResponses)
                .build()
        );

        Page<BookEntity> bookPage = new PageImpl<>(foundBookEntities);

        Mockito.when(categoryService.getCategoryEntityById(categoryId)).thenReturn(foundBooksCategoryEntity);
        Mockito.when(bookRepository.findByCategory(foundBooksCategoryEntity, pageable)).thenReturn(bookPage);
        for(int i = 0; i < pageSize; i++) 
        {
            Mockito.when(bookMapper.entityToResponse(foundBookEntities.get(i)))
                .thenReturn(foundBookResponses.get(i));
        }

        BaseResponse<List<BookResponse>> serviceResponse = bookService.getBooksByCategoryId(categoryId, pageNumber, pageSize);
        
        Mockito.verify(categoryService, Mockito.times(1)).getCategoryEntityById(categoryId);
        Mockito.verify(bookRepository, Mockito.times(1)).findByCategory(foundBooksCategoryEntity, pageable);
        for(int i = 0; i < pageSize; i++)
        {
            Mockito.verify(bookMapper, Mockito.times(1)).entityToResponse(foundBookEntities.get(i));
        }
        Mockito.verifyNoMoreInteractions(bookMapper, categoryService, authorService, bookRepository, amazonS3Service);

        Assertions.assertEquals(true, serviceResponse.isSuccess());
        Assertions.assertEquals("Books retrieved successfully.", serviceResponse.getMessage());
        Assertions.assertEquals(HttpStatus.OK.value(), serviceResponse.getStatus());
        Assertions.assertNotNull(serviceResponse.getData());
        for(int i = 0; i < pageSize; i++)
        {
            Assertions.assertEquals(foundBookResponses.get(i).getId(), serviceResponse.getData().get(i).getId());
            Assertions.assertEquals(foundBookResponses.get(i).getTitle(), serviceResponse.getData().get(i).getTitle());
            Assertions.assertEquals(foundBookResponses.get(i).getStock(), serviceResponse.getData().get(i).getStock());
            Assertions.assertEquals(foundBookResponses.get(i).getCategory(), serviceResponse.getData().get(i).getCategory());
            Assertions.assertEquals(foundBookResponses.get(i).getAuthors(), serviceResponse.getData().get(i).getAuthors());
        }
    }

    @Test
    @DisplayName(value = "Testing getBooksByCategoryId() method when category does not exist")
    public void givenGetBooksByCategoryIdWhenCategoryDoesNotExistThenThrowCategoryNotFoundException() 
    {
        long categoryId = 1L; 
        int pageNumber = 0, pageSize = 2; 

        Mockito.when(categoryService.getCategoryEntityById(categoryId))
            .thenThrow(new CategoryNotFoundException("Category not found with id : " + categoryId));
            
        Assertions
            .assertThrows(
                CategoryNotFoundException.class, 
                () -> bookService.getBooksByCategoryId(categoryId, pageNumber, pageSize)
            );

        Mockito.verify(categoryService, Mockito.times(1)).getCategoryEntityById(categoryId);
        Mockito.verifyNoMoreInteractions(bookMapper, categoryService, authorService, bookRepository, amazonS3Service);
    }

    @Test
    @DisplayName(value = "Testing searchBooksByTitle() method")
    public void givenSearchBooksByTitleThenReturnBaseResponseOfListOfBooks()
    {
        int pageNumber = 0, pageSize = 2; 
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        CategoryEntity foundBooksCategoryEntity = CategoryEntity.builder().id(1L).name("Dram").build(); 
        CategoryResponse foundBooksCategoryResponse = CategoryResponse.builder().id(1L).name("Dram").build();

        AuthorEntity foundAuthorEntity = AuthorEntity.builder()
            .id(1L)
            .firstName("Nizami")
            .lastName("Ganjavi")
            .build();
        
        AuthorResponse foundAuthorResponse = AuthorResponse.builder()
            .id(1L)
            .firstName("Nizami")
            .lastName("Ganjavi")
            .build();

        Set<AuthorEntity> foundBooksAuthorEntities = Set.of(foundAuthorEntity); 
        List<AuthorResponse> foundBooksAuthorResponses = List.of(foundAuthorResponse);

        List<BookEntity> foundBookEntities = List.of(
            BookEntity.builder()
                .id(1L)
                .title("Xamsa")
                .stock(1)
                .category(foundBooksCategoryEntity)
                .authors(foundBooksAuthorEntities)
                .build(),
            BookEntity.builder()
                .id(2L)
                .title("Leyli and Majnun")
                .stock(1)
                .category(foundBooksCategoryEntity)
                .authors(foundBooksAuthorEntities)
                .build()
        );

        List<BookResponse> foundBookResponses = List.of(
            BookResponse.builder()
                .id(1L)
                .title("Xamsa")
                .stock(1)
                .category(foundBooksCategoryResponse)
                .authors(foundBooksAuthorResponses) 
                .build(),
            BookResponse.builder()
                .id(2L)
                .title("Leyli and Majnun")
                .stock(1)
                .category(foundBooksCategoryResponse)
                .authors(foundBooksAuthorResponses)
                .build()
        );

        Page<BookEntity> bookPage = new PageImpl<>(foundBookEntities);

        Mockito.when(bookRepository.searchByTitle("a", pageable)).thenReturn(bookPage);
        for(int i = 0; i < pageSize; i++) 
        {
            Mockito.when(bookMapper.entityToResponse(foundBookEntities.get(i)))
                .thenReturn(foundBookResponses.get(i));
        }

        BaseResponse<List<BookResponse>> serviceResponse = bookService.searchBooksByTitle("a", pageNumber, pageSize);

        Mockito.verify(bookRepository, Mockito.times(1)).searchByTitle("a", pageable);
        for(int i = 0; i < pageSize; i++)
        {
            Mockito.verify(bookMapper, Mockito.times(1))
                .entityToResponse(foundBookEntities.get(i));
        }
        Mockito.verifyNoMoreInteractions(bookMapper, categoryService, authorService, bookRepository, amazonS3Service);
        
        Assertions.assertEquals(true, serviceResponse.isSuccess());
        Assertions.assertEquals("Books retrieved successfully.", serviceResponse.getMessage());
        Assertions.assertEquals(HttpStatus.OK.value(), serviceResponse.getStatus());
        Assertions.assertNotNull(serviceResponse.getData());
        for(int i = 0; i < pageSize; i++)
        {
            Assertions.assertEquals(foundBookResponses.get(i).getId(), serviceResponse.getData().get(i).getId());
            Assertions.assertEquals(foundBookResponses.get(i).getTitle(), serviceResponse.getData().get(i).getTitle());
            Assertions.assertEquals(foundBookResponses.get(i).getStock(), serviceResponse.getData().get(i).getStock());
            Assertions.assertEquals(foundBookResponses.get(i).getCategory(), serviceResponse.getData().get(i).getCategory());
            Assertions.assertEquals(foundBookResponses.get(i).getAuthors(), serviceResponse.getData().get(i).getAuthors());
        }
    }

    @Test
    @DisplayName(value = "Testing updateBook() method when book, authors and category exist")
    public void givenUpdateBookWhenBookAndAuthorsAndCategoryExistThenReturnSuccessResponse()
    {
        List<Long> authorIds = List.of(1L, 2L);
        int authorsSize = authorIds.size();

        BookRequest updateBookRequest = BookRequest.builder()
            .authorIds(authorIds)
            .categoryId(1L)
            .title("Tiny Pretty Things")
            .stock(5)
            .build(); 

        CategoryEntity foundCategoryEntity = CategoryEntity.builder().name("Dram").id(1L).build(); 

        Set<AuthorEntity> foundAuthorEntities = Set
            .of(
                AuthorEntity.builder()
                    .id(1L)
                    .firstName("Sona")
                    .lastName("Charaipotra")
                    .build(),
                AuthorEntity.builder()
                    .id(2L)
                    .firstName("Dhonielle")
                    .lastName("Clayton")
                    .build()
            ); 

        BookEntity foundBookEntity = BookEntity.builder()
            .id(1L)
            .title("Tiny Pretty Things")
            .stock(1)
            .authors(foundAuthorEntities)
            .category(foundCategoryEntity)
            .build(); 

        BookEntity updatedBookEntity = BookEntity.builder()
            .id(1L)
            .title("Tiny Pretty Things")
            .stock(5)
            .authors(foundAuthorEntities)
            .category(foundCategoryEntity)
            .build(); 

        Mockito.when(bookRepository.findById(1L)).thenReturn(Optional.of(foundBookEntity));

        Mockito
            .doAnswer(invocation -> {
                BookRequest mapperBookRequest = invocation.getArgument(0); 
                BookEntity mapperBookEntity = invocation.getArgument(1);
                
                mapperBookEntity.setTitle(mapperBookRequest.getTitle());
                mapperBookEntity.setStock(mapperBookRequest.getStock());
                
                return null;
            })
            .when(bookMapper)
            .convertRequestToEntity(
                Mockito.any(BookRequest.class), 
                Mockito.any(BookEntity.class)
            );

        Mockito.when(bookRepository.save(updatedBookEntity)).thenReturn(updatedBookEntity);
        Mockito.when(categoryService.getCategoryEntityById(1L)).thenReturn(foundCategoryEntity);
        
        Iterator<AuthorEntity> authorEntityIterator = foundAuthorEntities.iterator();
        for(int i = 0; i < authorsSize; i++) 
        {
            Mockito.when(authorService.getAuthorEntityById(authorIds.get(i))).thenReturn(authorEntityIterator.next());
        }

        Mockito.when(bookRepository.save(updatedBookEntity)).thenReturn(updatedBookEntity);
        
        BaseResponse<Void> serviceResponse = bookService.updateBook(1L, updateBookRequest);

        Mockito.verify(bookRepository, Mockito.times(1)).findById(1L);
        Mockito.verify(bookMapper, Mockito.times(1))
            .convertRequestToEntity(updateBookRequest, foundBookEntity);
        Mockito.verify(categoryService, Mockito.times(1)).getCategoryEntityById(1L);
        for(int i = 0; i < authorsSize; i++)
        {
            Mockito.verify(authorService, Mockito.times(1)).getAuthorEntityById(authorIds.get(i));
        }
        Mockito.verify(bookRepository, Mockito.times(1)).save(updatedBookEntity);
        Mockito.verifyNoMoreInteractions(bookMapper, categoryService, authorService, bookRepository, amazonS3Service);

        Assertions.assertEquals(true, serviceResponse.isSuccess());
        Assertions.assertEquals("Book updated successfully.", serviceResponse.getMessage());
        Assertions.assertEquals(HttpStatus.OK.value(), serviceResponse.getStatus());
        Assertions.assertNull(serviceResponse.getData());
    }

    @Test
    @DisplayName(value = "Testing updateBook() method when book does not exist")
    public void givenUpdateBookWhenBookDoesNotExistThenThrowBookNotFoundException()
    {
        Mockito.when(bookRepository.findById(1L)).thenReturn(Optional.empty());
        
        BookNotFoundException exception = Assertions
            .assertThrows(BookNotFoundException.class, () -> bookService.getBookById(1L));

        Assertions.assertEquals("Book not found with id : 1", exception.getMessage());

        Mockito.verify(bookRepository, Mockito.times(1)).findById(1L);
        Mockito.verifyNoMoreInteractions(bookMapper, categoryService, authorService, bookRepository, amazonS3Service);
    }
    
    @Test
    @DisplayName(value = "Testing updateBook() method when category does not exist")
    public void givenUpdateBookWhenCategoryDoesNotExistThenThrowCategoryNotFoundException()
    {
        List<Long> authorIds = List.of(1L, 2L);

        BookRequest updateBookRequest = BookRequest.builder()
            .authorIds(authorIds)
            .categoryId(2L)
            .title("Tiny Pretty Things")
            .stock(5)
            .build(); 

        CategoryEntity foundBookCategoryEntity = CategoryEntity.builder().id(1L).name("Dram").build(); 

        Set<AuthorEntity> foundBookAuthorEntities = Set
            .of(
                AuthorEntity.builder()
                    .id(1L)
                    .firstName("Sona")
                    .lastName("Charaipotra")
                    .build(),
                AuthorEntity.builder()
                    .id(2L)
                    .firstName("Dhonielle")
                    .lastName("Clayton")
                    .build()
            ); 

        BookEntity foundBookEntity = BookEntity.builder()
            .id(1L)
            .title("Tiny Pretty Things")
            .stock(1)
            .authors(foundBookAuthorEntities)
            .category(foundBookCategoryEntity)
            .build(); 

        Mockito.when(bookRepository.findById(1L)).thenReturn(Optional.of(foundBookEntity));

        Mockito
            .doAnswer(invocation -> {
                BookRequest mapperBookRequest = invocation.getArgument(0); 
                BookEntity mapperBookEntity = invocation.getArgument(1);
                
                mapperBookEntity.setTitle(mapperBookRequest.getTitle());
                mapperBookEntity.setStock(mapperBookRequest.getStock());
                
                return null;
            })
            .when(bookMapper)
            .convertRequestToEntity(
                Mockito.any(BookRequest.class), 
                Mockito.any(BookEntity.class)
            );

        Mockito.when(categoryService.getCategoryEntityById(2L))
            .thenThrow(new CategoryNotFoundException("Category not found with id : 2"));

        Assertions
            .assertThrows(
                CategoryNotFoundException.class, 
                () -> bookService.updateBook(1L, updateBookRequest)
            );

        Mockito.verify(bookRepository, Mockito.times(1)).findById(1L);
        Mockito.verify(bookMapper, Mockito.times(1))
            .convertRequestToEntity(updateBookRequest, foundBookEntity);
        Mockito.verify(categoryService, Mockito.times(1)).getCategoryEntityById(2L);
        Mockito.verifyNoMoreInteractions(bookMapper, authorService, categoryService, bookRepository, amazonS3Service);
    }

    @Test
    @DisplayName(value = "Testing updateBook() method when authors do not exist")
    public void givenUpdateBookWhenAuthorsDoesNotExistThenThrowAuthorNotFoundException()
    {
        List<Long> authorIds = List.of(3L);

        BookRequest updateBookRequest = BookRequest.builder()
            .authorIds(authorIds)
            .categoryId(1L)
            .title("Tiny Pretty Things")
            .stock(5)
            .build();

        CategoryEntity foundBookCategoryEntity = CategoryEntity.builder().id(1L).name("Dram").build();
        
        Set<AuthorEntity> foundBookAuthorEntities = Set
            .of(
                AuthorEntity.builder()
                    .id(1L)
                    .firstName("Sona")
                    .lastName("Charaipotra")
                    .build(),
                AuthorEntity.builder()
                    .id(2L)
                    .firstName("Dhonielle")
                    .lastName("Clayton")
                    .build()
            ); 

        BookEntity foundBookEntity = BookEntity.builder()
            .id(1L)
            .title("Tiny Pretty Things")
            .stock(1)
            .authors(foundBookAuthorEntities)
            .category(foundBookCategoryEntity)
            .build();
            
        Mockito.when(bookRepository.findById(1L)).thenReturn(Optional.of(foundBookEntity));

        Mockito
            .doAnswer(invocation -> {
                BookRequest mapperBookRequest = invocation.getArgument(0); 
                BookEntity mapperBookEntity = invocation.getArgument(1);
                
                mapperBookEntity.setTitle(mapperBookRequest.getTitle());
                mapperBookEntity.setStock(mapperBookRequest.getStock());
                
                return null;
            })
            .when(bookMapper)
            .convertRequestToEntity(
                Mockito.any(BookRequest.class), 
                Mockito.any(BookEntity.class)
            );

        Mockito.when(categoryService.getCategoryEntityById(1L)).thenReturn(foundBookCategoryEntity);
        Mockito.when(authorService.getAuthorEntityById(authorIds.get(0)))
            .thenThrow(new AuthorNotFoundException("Author not found with id : " + authorIds.get(0)));

        Assertions
            .assertThrows(
                AuthorNotFoundException.class,
                () -> bookService.updateBook(1L, updateBookRequest)
            );

        Mockito.verify(bookRepository, Mockito.times(1)).findById(1L);
        Mockito.verify(bookMapper, Mockito.times(1))
            .convertRequestToEntity(updateBookRequest, foundBookEntity);
        Mockito.verify(categoryService, Mockito.times(1)).getCategoryEntityById(1L);
        Mockito.verify(authorService, Mockito.times(1)).getAuthorEntityById(authorIds.get(0));
        Mockito.verifyNoMoreInteractions(bookMapper, authorService, categoryService, bookRepository, amazonS3Service);
    }

    @Test
    @DisplayName(value = "Testing deleteBook() method when book and its image exists")
    public void givenDeleteBookWhenBookAndItsImageExistsThenReturnSuccessResponse()
    {
        String bookFileKey = "books/1.jpg";
        CategoryEntity foundBookCategoryEntity = CategoryEntity.builder().id(1L).name("Dram").build();
        
        Set<AuthorEntity> foundBookAuthorEntities = Set
            .of(
                AuthorEntity.builder()
                    .id(1L)
                    .firstName("Sona")
                    .lastName("Charaipotra")
                    .build(),
                AuthorEntity.builder()
                    .id(2L)
                    .firstName("Dhonielle")
                    .lastName("Clayton")
                    .build()
            ); 

        BookEntity foundBookEntity = BookEntity.builder()
            .id(1L)
            .title("Tiny Pretty Things")
            .stock(1)
            .authors(foundBookAuthorEntities)
            .category(foundBookCategoryEntity)
            .s3FileKey(bookFileKey)
            .build();

        Mockito.when(bookRepository.findById(1L)).thenReturn(Optional.of(foundBookEntity));
        Mockito.when(amazonS3Service.deleteFile(bookFileKey))
            .thenReturn(
                BaseResponse.<Void>builder()
                    .success(true)
                    .message("File deleted successfully.")
                    .status(HttpStatus.OK.value())
                    .build()
            );
        
        BaseResponse<Void> deleteBookResponse = bookService.deleteBook(1L);

        Mockito.verify(bookRepository, Mockito.times(1)).findById(1L);
        Mockito.verify(bookRepository, Mockito.times(1)).deleteById(1L);
        Mockito.verify(amazonS3Service, Mockito.times(1)).deleteFile(bookFileKey);
        Mockito.verifyNoMoreInteractions(bookMapper, authorService, categoryService, bookRepository, amazonS3Service);

        Assertions.assertEquals(HttpStatus.OK.value(), deleteBookResponse.getStatus());
        Assertions.assertEquals("Book deleted successfully.", deleteBookResponse.getMessage());
        Assertions.assertTrue(deleteBookResponse.isSuccess());
        Assertions.assertNull(deleteBookResponse.getData());
    }

    @Test
    @DisplayName(value = "Testing deleteBook() method when book exists, but its image doesn't")
    public void givenDeleteBookWhenBookExistsButItsImageDoesNotExistsThenReturnSuccessResponse()
    {
        CategoryEntity foundBookCategoryEntity = CategoryEntity.builder().id(1L).name("Dram").build();
        
        Set<AuthorEntity> foundBookAuthorEntities = Set
            .of(
                AuthorEntity.builder()
                    .id(1L)
                    .firstName("Sona")
                    .lastName("Charaipotra")
                    .build(),
                AuthorEntity.builder()
                    .id(2L)
                    .firstName("Dhonielle")
                    .lastName("Clayton")
                    .build()
            ); 

        BookEntity foundBookEntity = BookEntity.builder()
            .id(1L)
            .title("Tiny Pretty Things")
            .stock(1)
            .authors(foundBookAuthorEntities)
            .category(foundBookCategoryEntity)
            .build();

        Mockito.when(bookRepository.findById(1L)).thenReturn(Optional.of(foundBookEntity));

        BaseResponse<Void> deleteBookResponse = bookService.deleteBook(1L);

        Mockito.verify(bookRepository, Mockito.times(1)).findById(1L);
        Mockito.verify(bookRepository, Mockito.times(1)).deleteById(1L);
        Mockito.verifyNoMoreInteractions(bookMapper, authorService, categoryService, bookRepository, amazonS3Service);

        Assertions.assertEquals(HttpStatus.OK.value(), deleteBookResponse.getStatus());
        Assertions.assertEquals("Book deleted successfully.", deleteBookResponse.getMessage());
        Assertions.assertTrue(deleteBookResponse.isSuccess());
        Assertions.assertNull(deleteBookResponse.getData());
    }

    @Test
    @DisplayName(value = "Testing deleteBook() method when book does not exist")
    public void givenDeleteBookWhenBookDoesNotExistThenThrowBookNotFoundException()
    {
        Mockito.when(bookRepository.findById(1L)).thenReturn(Optional.empty());
        
        BookNotFoundException exception = Assertions
            .assertThrows(BookNotFoundException.class, () -> bookService.deleteBook(1L));

        Assertions.assertEquals("Book not found with id : 1", exception.getMessage());

        Mockito.verify(bookRepository, Mockito.times(1)).findById(1L);
        Mockito.verifyNoMoreInteractions(bookMapper, categoryService, authorService, bookRepository, amazonS3Service);
    }

    @Test
    @DisplayName(value = "Testing updateBookStock() method when book exists")
    public void givenUpdateBookStockWhenBookExistsThenReturnSuccessResponse()
    {
        CategoryEntity foundBookCategoryEntity = CategoryEntity.builder().id(1L).name("Dram").build();
        
        Set<AuthorEntity> foundBookAuthorEntities = Set
            .of(
                AuthorEntity.builder()
                    .id(1L)
                    .firstName("Sona")
                    .lastName("Charaipotra")
                    .build(),
                AuthorEntity.builder()
                    .id(2L)
                    .firstName("Dhonielle")
                    .lastName("Clayton")
                    .build()
            ); 

        BookEntity foundBookEntity = BookEntity.builder()
            .id(1L)
            .title("Tiny Pretty Things")
            .stock(1)
            .authors(foundBookAuthorEntities)
            .category(foundBookCategoryEntity)
            .build();

        BookEntity updatedBookEntity = BookEntity.builder()
            .id(1L)
            .title("Tiny Pretty Things")
            .stock(2)
            .authors(foundBookAuthorEntities)
            .category(foundBookCategoryEntity)
            .build();

        Mockito.when(bookRepository.findById(1L)).thenReturn(Optional.of(foundBookEntity));
        Mockito.when(bookRepository.save(updatedBookEntity)).thenReturn(updatedBookEntity);
        
        BaseResponse<Void> serviceResponse = bookService.updateBookStock(1L, 2);
        
        Mockito.verify(bookRepository, Mockito.times(1)).findById(1L);
        Mockito.verify(bookRepository, Mockito.times(1)).save(updatedBookEntity);

        Assertions.assertEquals(HttpStatus.OK.value(), serviceResponse.getStatus());
        Assertions.assertEquals("Book stock updated successfully.", serviceResponse.getMessage());
        Assertions.assertTrue(serviceResponse.isSuccess());
        Assertions.assertNull(serviceResponse.getData());
    }

    @Test
    @DisplayName(value = "Testing updateBookStock() method when stock is less than 0")
    public void givenUpdateBookStockWhenStockIsLessThanZeroThenThrowBookOutOfStockException()
    {
        CategoryEntity foundBookCategoryEntity = CategoryEntity.builder().id(1L).name("Dram").build();
        
        Set<AuthorEntity> foundBookAuthorEntities = Set
            .of(
                AuthorEntity.builder()
                    .id(1L)
                    .firstName("Sona")
                    .lastName("Charaipotra")
                    .build(),
                AuthorEntity.builder()
                    .id(2L)
                    .firstName("Dhonielle")
                    .lastName("Clayton")
                    .build()
            ); 

        BookEntity foundBookEntity = BookEntity.builder()
            .id(1L)
            .title("Tiny Pretty Things")
            .stock(0)
            .authors(foundBookAuthorEntities)
            .category(foundBookCategoryEntity)
            .build();

        Mockito.when(bookRepository.findById(1L)).thenReturn(Optional.of(foundBookEntity));

        BookOutOfStockException exception = Assertions
            .assertThrows(BookOutOfStockException.class, () -> bookService.updateBookStock(1L, -1));

        Assertions.assertEquals("Book is out of stock.", exception.getMessage());

        Mockito.verify(bookRepository, Mockito.times(1)).findById(1L);
        Mockito.verifyNoMoreInteractions(bookRepository, bookMapper, categoryService, authorService, amazonS3Service);
    }

    @Test
    @DisplayName(value = "Testing updateBookStock() method when book doesn't exist")
    public void givenUpdateBookStockWhenBookDoesNotExistThenThrowBookNotFoundException() 
    {
        Mockito.when(bookRepository.findById(1L)).thenReturn(Optional.empty());
        
        BookNotFoundException exception = Assertions
            .assertThrows(BookNotFoundException.class, () -> bookService.updateBookStock(1L, 2));

        Assertions.assertEquals("Book not found with id : 1", exception.getMessage());

        Mockito.verify(bookRepository, Mockito.times(1)).findById(1L);
        Mockito.verifyNoMoreInteractions(bookMapper, categoryService, authorService, bookRepository, amazonS3Service);
    }

    @Test
    @DisplayName(value = "Testing uploadBookImage() method when book exists")
    public void givenUploadBookImageWhenBookExistsThenReturnSuccessResponse()
    {
        File bookImage = new File("/books/book1.jpg");
        String imageKey = "books/1.jpg";

        CategoryEntity foundBookCategoryEntity = CategoryEntity.builder().id(1L).name("Dram").build();
        
        Set<AuthorEntity> foundBookAuthorEntities = Set
            .of(
                AuthorEntity.builder()
                    .id(1L)
                    .firstName("Sona")
                    .lastName("Charaipotra")
                    .build(),
                AuthorEntity.builder()
                    .id(2L)
                    .firstName("Dhonielle")
                    .lastName("Clayton")
                    .build()
            ); 

        BookEntity foundBookEntity = BookEntity.builder()
            .id(1L)
            .title("Tiny Pretty Things")
            .stock(0)
            .authors(foundBookAuthorEntities)
            .category(foundBookCategoryEntity)
            .build();

        BookEntity updateBookEntity = BookEntity.builder()
            .id(1L)
            .title("Tiny Pretty Things")
            .stock(0)
            .authors(foundBookAuthorEntities)
            .category(foundBookCategoryEntity)
            .s3FileKey(imageKey)
            .build();

        Mockito.when(bookRepository.findById(1L)).thenReturn(Optional.of(foundBookEntity));
        
        Mockito.when(amazonS3Service.uploadFile(imageKey, bookImage))
            .thenReturn(
                BaseResponse.<Void>builder()
                    .status(HttpStatus.OK.value())
                    .message("File uploaded successfully.")
                    .success(true)
                    .build()
            );
        
        Mockito.when(bookRepository.save(updateBookEntity)).thenReturn(updateBookEntity);

        BaseResponse<Void> serviceResponse = bookService.uploadBookImage(1L, bookImage); 

        Mockito.verify(bookRepository, Mockito.times(1)).findById(1L);
        Mockito.verify(amazonS3Service, Mockito.times(1)).uploadFile(imageKey, bookImage);
        Mockito.verify(bookRepository, Mockito.times(1)).save(updateBookEntity); 
        Mockito.verifyNoMoreInteractions(bookMapper, categoryService, authorService, bookRepository, amazonS3Service);

        Assertions.assertEquals(HttpStatus.OK.value(), serviceResponse.getStatus());
        Assertions.assertEquals("Book image uploaded successfully.", serviceResponse.getMessage());
        Assertions.assertTrue(serviceResponse.isSuccess());
        Assertions.assertNull(serviceResponse.getData());
    }

    @Test
    @DisplayName(value = "Testing uploadBookImage() method when book doesn't exist")
    public void givenUploadBookImageWhenBookDoesNotExistThenThrowBookNotFoundException()
    {
        Mockito.when(bookRepository.findById(1L)).thenReturn(Optional.empty());
        
        BookNotFoundException exception = Assertions
            .assertThrows(BookNotFoundException.class, () -> bookService.updateBookStock(1L, 2));

        Assertions.assertEquals("Book not found with id : 1", exception.getMessage());

        Mockito.verify(bookRepository, Mockito.times(1)).findById(1L);
        Mockito.verifyNoMoreInteractions(bookMapper, categoryService, authorService, bookRepository, amazonS3Service);
    }

    @Test
    @DisplayName(value = "Testing getBookImage() method when book and its image exists")
    public void givenGetBookImageWhenBookAndItsImageExistsThenReturnSuccessResponse()
    {
        String bookFileKey = "books/1.jpg";
        byte[] imageData = new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9};

        CategoryEntity foundBookCategoryEntity = CategoryEntity.builder().id(1L).name("Dram").build();
        
        Set<AuthorEntity> foundBookAuthorEntities = Set
            .of(
                AuthorEntity.builder()
                    .id(1L)
                    .firstName("Sona")
                    .lastName("Charaipotra")
                    .build(),
                AuthorEntity.builder()
                    .id(2L)
                    .firstName("Dhonielle")
                    .lastName("Clayton")
                    .build()
            ); 

        BookEntity foundBookEntity = BookEntity.builder()
            .id(1L)
            .title("Tiny Pretty Things")
            .stock(0)
            .authors(foundBookAuthorEntities)
            .category(foundBookCategoryEntity)
            .s3FileKey(bookFileKey)
            .build();

        BookImageResponse bookImageResponse = BookImageResponse.builder()
            .fileKey(bookFileKey)
            .imageData(imageData)
            .build();

        Mockito.when(bookRepository.findById(1L)).thenReturn(Optional.of(foundBookEntity));

        Mockito.when(amazonS3Service.getFile(bookFileKey))
            .thenReturn(
                BaseResponse.<byte[]>builder()
                    .success(true)
                    .data(imageData)
                    .message("File retrieved successfully.")
                    .status(HttpStatus.OK.value())
                    .build()
            );

        BaseResponse<BookImageResponse> serviceResponse = bookService.getBookImage(1L);
        
        Mockito.verify(bookRepository, Mockito.times(1)).findById(1L);
        Mockito.verify(amazonS3Service, Mockito.times(1)).getFile(bookFileKey);
        Mockito.verifyNoMoreInteractions(bookMapper, categoryService, authorService, bookRepository, amazonS3Service);

        Assertions.assertEquals(HttpStatus.OK.value(), serviceResponse.getStatus());
        Assertions.assertEquals("Book image retrieved successfully.", serviceResponse.getMessage());
        Assertions.assertTrue(serviceResponse.isSuccess());
        Assertions.assertEquals(bookImageResponse, serviceResponse.getData());
        Assertions.assertNotNull(serviceResponse.getData());
        Assertions.assertEquals(bookFileKey, serviceResponse.getData().getFileKey());
        Assertions.assertEquals(imageData, serviceResponse.getData().getImageData());
    }

    @Test
    @DisplayName(value = "Testing getBookImage() method when book exists but its image doesn't")
    public void givenGetBookImageWhenBookExistsButItsImageDoesntExistThenReturnSuccessResponse()
    {
        String bookFileKey = "books/default.webp";
        byte[] imageData = new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9};

        CategoryEntity foundBookCategoryEntity = CategoryEntity.builder().id(1L).name("Dram").build();
        
        Set<AuthorEntity> foundBookAuthorEntities = Set
            .of(
                AuthorEntity.builder()
                    .id(1L)
                    .firstName("Sona")
                    .lastName("Charaipotra")
                    .build(),
                AuthorEntity.builder()
                    .id(2L)
                    .firstName("Dhonielle")
                    .lastName("Clayton")
                    .build()
            ); 

        BookEntity foundBookEntity = BookEntity.builder()
            .id(1L)
            .title("Tiny Pretty Things")
            .stock(0)
            .authors(foundBookAuthorEntities)
            .category(foundBookCategoryEntity)
            .build();

        BookImageResponse bookImageResponse = BookImageResponse.builder()
            .fileKey(bookFileKey)
            .imageData(imageData)
            .build();

        Mockito.when(bookRepository.findById(1L)).thenReturn(Optional.of(foundBookEntity));

        Mockito.when(amazonS3Service.getFile(bookFileKey))
            .thenReturn(
                BaseResponse.<byte[]>builder()
                    .success(true)
                    .data(imageData)
                    .message("File retrieved successfully.")
                    .status(HttpStatus.OK.value())
                    .build()
            );

        BaseResponse<BookImageResponse> serviceResponse = bookService.getBookImage(1L);
        
        Mockito.verify(bookRepository, Mockito.times(1)).findById(1L);
        Mockito.verify(amazonS3Service, Mockito.times(1)).getFile(bookFileKey);
        Mockito.verifyNoMoreInteractions(bookMapper, categoryService, authorService, bookRepository, amazonS3Service);

        Assertions.assertEquals(HttpStatus.OK.value(), serviceResponse.getStatus());
        Assertions.assertEquals("Book image retrieved successfully.", serviceResponse.getMessage());
        Assertions.assertTrue(serviceResponse.isSuccess());
        Assertions.assertEquals(bookImageResponse, serviceResponse.getData());
        Assertions.assertNotNull(serviceResponse.getData());
        Assertions.assertEquals(bookFileKey, serviceResponse.getData().getFileKey());
        Assertions.assertEquals(imageData, serviceResponse.getData().getImageData());
    }

    @Test
    @DisplayName(value = "Testing getBookImage() method when book doesn't exist")
    public void givenGetBookImageWhenBookDoesNotExistThenThrowBookNotFoundException()
    {
        Mockito.when(bookRepository.findById(1L)).thenReturn(Optional.empty());
        
        BookNotFoundException exception = Assertions
            .assertThrows(BookNotFoundException.class, () -> bookService.updateBookStock(1L, 2));

        Assertions.assertEquals("Book not found with id : 1", exception.getMessage());

        Mockito.verify(bookRepository, Mockito.times(1)).findById(1L);
        Mockito.verifyNoMoreInteractions(bookMapper, categoryService, authorService, bookRepository, amazonS3Service);
    }

    @Test
    @DisplayName(value = "Testing getBookEntityById() method when book exists")
    public void givenGetBookEntityByIdWhenBookExistsThenReturnBookEntity()
    {
        CategoryEntity foundBookCategoryEntity = CategoryEntity.builder().id(1L).name("Dram").build();
        
        Set<AuthorEntity> foundBookAuthorEntities = Set
            .of(
                AuthorEntity.builder()
                    .id(1L)
                    .firstName("Sona")
                    .lastName("Charaipotra")
                    .build(),
                AuthorEntity.builder()
                    .id(2L)
                    .firstName("Dhonielle")
                    .lastName("Clayton")
                    .build()
            ); 

        BookEntity foundBookEntity = BookEntity.builder()
            .id(1L)
            .title("Tiny Pretty Things")
            .stock(0)
            .authors(foundBookAuthorEntities)
            .category(foundBookCategoryEntity)
            .build();

        Mockito.when(bookRepository.findById(1L)).thenReturn(Optional.of(foundBookEntity));
        
        BookEntity serviceResponse = bookService.getBookEntityById(1L);
        
        Mockito.verify(bookRepository, Mockito.times(1)).findById(1L);
        Mockito.verifyNoMoreInteractions(bookMapper, categoryService, authorService, bookRepository, amazonS3Service);
        
        Assertions.assertNotNull(serviceResponse);
        Assertions.assertEquals(foundBookEntity.getId(), serviceResponse.getId());
        Assertions.assertEquals(foundBookEntity.getTitle(), serviceResponse.getTitle());
        Assertions.assertEquals(foundBookEntity.getStock(), serviceResponse.getStock());
        Assertions.assertEquals(foundBookEntity.getAuthors(), serviceResponse.getAuthors());
        Assertions.assertEquals(foundBookEntity.getCategory(), serviceResponse.getCategory());
        Assertions.assertEquals(foundBookEntity.getS3FileKey(), serviceResponse.getS3FileKey());
    }

    @Test
    @DisplayName(value = "Testing getBookEntityById() method when book does not exist")
    public void givenGetBookEntityByIdWhenBookDoesNotExistThenThrowBookNotFoundException() 
    {
        Mockito.when(bookRepository.findById(1L)).thenReturn(Optional.empty());
        
        BookNotFoundException exception = Assertions
           .assertThrows(BookNotFoundException.class, () -> bookService.getBookEntityById(1L));

        Assertions.assertEquals("Book not found with id : 1", exception.getMessage());
        
        Mockito.verify(bookRepository, Mockito.times(1)).findById(1L);
        Mockito.verifyNoMoreInteractions(bookMapper, categoryService, authorService, bookRepository, amazonS3Service);
    }
}