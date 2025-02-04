package az.atlacademy.libraryadp.service;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
import az.atlacademy.libraryadp.exception.CategoryNotFoundException;
import az.atlacademy.libraryadp.mapper.BookMapper;
import az.atlacademy.libraryadp.model.dto.request.BookRequest;
import az.atlacademy.libraryadp.model.dto.response.BaseResponse;
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
}
