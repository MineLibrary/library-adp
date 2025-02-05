package az.atlacademy.libraryadp.service;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
import az.atlacademy.libraryadp.exception.BookNotFoundException;
import az.atlacademy.libraryadp.exception.CategoryNotFoundException;
import az.atlacademy.libraryadp.mapper.BookMapper;
import az.atlacademy.libraryadp.model.dto.request.BookRequest;
import az.atlacademy.libraryadp.model.dto.response.AuthorResponse;
import az.atlacademy.libraryadp.model.dto.response.BaseResponse;
import az.atlacademy.libraryadp.model.dto.response.BookResponse;
import az.atlacademy.libraryadp.model.dto.response.CategoryResponse;
import az.atlacademy.libraryadp.model.entity.AuthorEntity;
import az.atlacademy.libraryadp.model.entity.BookEntity;
import az.atlacademy.libraryadp.model.entity.CategoryEntity;
import az.atlacademy.libraryadp.repository.BookRepository;

@ExtendWith(value = MockitoExtension.class)
public class BookServiceTests 
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

        CategoryEntity foundCategoryEntity = CategoryEntity.builder().name("Dram").build(); 

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
        Mockito.verifyNoMoreInteractions(bookMapper, categoryService, authorService, bookRepository);

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
        Mockito.verifyNoMoreInteractions(bookMapper, categoryService, authorService, bookRepository);
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

        CategoryEntity foundCategoryEntity = CategoryEntity.builder().name("Dram").build(); 

        Mockito.when(bookMapper.requestToEntity(createBookRequest)).thenReturn(createBookEntity);
        Mockito.when(categoryService.getCategoryEntityById(1L)).thenReturn(foundCategoryEntity);
        Mockito.when(authorService.getAuthorEntityById(authorIds.get(0)))
            .thenThrow(new AuthorNotFoundException("Author not found with id : " + authorIds.get(0)));

        Assertions.assertThrows(AuthorNotFoundException.class, () -> bookService.createBook(createBookRequest));

        Mockito.verify(bookMapper, Mockito.times(1)).requestToEntity(createBookRequest);
        Mockito.verify(categoryService, Mockito.times(1)).getCategoryEntityById(1L);
        Mockito.verify(authorService, Mockito.times(1)).getAuthorEntityById(authorIds.get(0));
        Mockito.verifyNoMoreInteractions(bookMapper, categoryService, authorService, bookRepository);
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
        Mockito.verifyNoMoreInteractions(bookMapper, categoryService, authorService, bookRepository);

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
        Mockito.verifyNoMoreInteractions(bookMapper, categoryService, authorService, bookRepository);
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
        Mockito.verifyNoMoreInteractions(bookMapper, categoryService, authorService, bookRepository);

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
        Mockito.verifyNoMoreInteractions(bookMapper, categoryService, authorService, bookRepository);
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
        Mockito.verifyNoMoreInteractions(bookMapper, categoryService, authorService, bookRepository);

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
        Mockito.verifyNoMoreInteractions(bookMapper, categoryService, authorService, bookRepository);

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
        Mockito.verifyNoMoreInteractions(bookMapper, categoryService, authorService, bookRepository);
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
        Mockito.verifyNoMoreInteractions(bookMapper, categoryService, authorService, bookRepository);
        
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
}
