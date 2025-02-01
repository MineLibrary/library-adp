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
import org.springframework.http.HttpStatus;

import az.atlacademy.libraryadp.exception.CategoryNotFoundException;
import az.atlacademy.libraryadp.mapper.CategoryMapper;
import az.atlacademy.libraryadp.model.dto.request.CategoryRequest;
import az.atlacademy.libraryadp.model.dto.response.BaseResponse;
import az.atlacademy.libraryadp.model.dto.response.CategoryResponse;
import az.atlacademy.libraryadp.model.entity.CategoryEntity;
import az.atlacademy.libraryadp.repository.CategoryRepository;

@ExtendWith(value = MockitoExtension.class)
public class CategoryServiceTest 
{
    @InjectMocks
    private CategoryService categoryService;

    @Mock
    private CategoryRepository categoryRepository; 

    @Mock
    private CategoryMapper categoryMapper;

    @Test
    @DisplayName(value = "Testing createCategory() method")
    public void givenCreateCategoryThenReturnSuccessResponse()
    {
        CategoryRequest createCategoryRequest = new CategoryRequest("drama");
        CategoryEntity createCategoryEntity = CategoryEntity.builder().name("drama").build();
        CategoryEntity createdCategoryEntity = CategoryEntity.builder().id(1L).name("drama").build();

        Mockito.when(categoryMapper.requestToEntity(createCategoryRequest)).thenReturn(createCategoryEntity); 
        Mockito.when(categoryRepository.save(createCategoryEntity)).thenReturn(createdCategoryEntity);
        Mockito.verifyNoInteractions(categoryRepository, categoryMapper);

        BaseResponse<Void> serviceResponse = categoryService.createCategory(createCategoryRequest);
        
        Mockito.verify(categoryMapper, Mockito.times(1)).requestToEntity(createCategoryRequest);
        Mockito.verify(categoryRepository, Mockito.times(1)).save(createCategoryEntity);

        Assertions.assertEquals(true, serviceResponse.isSuccess());
        Assertions.assertEquals("Category created successfully.", serviceResponse.getMessage());
        Assertions.assertEquals(HttpStatus.CREATED.value(), serviceResponse.getStatus());
    }

    @Test
    @DisplayName(value = "Testing getCategoryById() method when Category exists")
    public void givenGetCategoryByIdWhenCategoryExistsThenReturnBaseResponseOfCategoryResponse()
    {
        CategoryEntity foundCategoryEntity = CategoryEntity.builder().id(1L).name("drama").build(); 
        CategoryResponse foundCategoryResponse = CategoryResponse.builder().id(1L).name("drama").build();

        Mockito.when(categoryRepository.findById(1L)).thenReturn(Optional.of(foundCategoryEntity));
        Mockito.when(categoryMapper.entityToResponse(foundCategoryEntity)).thenReturn(foundCategoryResponse); 

        BaseResponse<CategoryResponse> serviceResponse = categoryService.getCategoryById(1L);
        
        Mockito.verify(categoryRepository, Mockito.times(1)).findById(1L);
        Mockito.verify(categoryMapper, Mockito.times(1)).entityToResponse(foundCategoryEntity);
        Mockito.verifyNoMoreInteractions(categoryRepository, categoryMapper);

        Assertions.assertEquals(true, serviceResponse.isSuccess());
        Assertions.assertEquals("Category retrieved successfully.", serviceResponse.getMessage());
        Assertions.assertEquals(HttpStatus.OK.value(), serviceResponse.getStatus());
        Assertions.assertNotNull(serviceResponse.getData());
        Assertions.assertEquals(1L, serviceResponse.getData().getId());
        Assertions.assertEquals("drama", serviceResponse.getData().getName());
    }

    @Test
    @DisplayName(value = "Testing getCategoryById() method when Category does not exist")
    public void givenGetCategoryByIdWhenCategoryDoesNotExistThenThrowCategoryNotFoundException()
    {
        Mockito.when(categoryRepository.findById(2L)).thenReturn(Optional.empty());

        CategoryNotFoundException exception = Assertions
            .assertThrows(CategoryNotFoundException.class, () -> categoryService.getCategoryById(2L));
            
        Assertions.assertEquals("Category not found with id : 2", exception.getMessage());

        Mockito.verify(categoryRepository, Mockito.times(1)).findById(2L);
        Mockito.verifyNoMoreInteractions(categoryRepository, categoryMapper);
    }

    @Test
    @DisplayName(value = "Testing getAllCategories() method")
    public void givenGetAllCategoriesThenReturnBaseResponseOfListOfCategories()
    {
        List<CategoryEntity> foundCategoryEntities = List.of(
            CategoryEntity.builder().id(1L).name("drama").build(),
            CategoryEntity.builder().id(2L).name("comedy").build());

        List<CategoryResponse> foundCategoryResponses = List.of(
            CategoryResponse.builder().id(1L).name("drama").build(),
            CategoryResponse.builder().id(2L).name("comedy").build());

        int length = foundCategoryEntities.size();
        
        Mockito.when(categoryRepository.findAll()).thenReturn(foundCategoryEntities);
        for(int i = 0; i < length; i++)
        {
            Mockito.when(categoryMapper.entityToResponse(foundCategoryEntities.get(i)))
                .thenReturn(foundCategoryResponses.get(i));
        }

        BaseResponse<List<CategoryResponse>> serviceResponse = categoryService.getAllCategories();
        
        Mockito.verify(categoryRepository, Mockito.times(1)).findAll();
        for(int i = 0; i < length; i++)
        {
            Mockito.verify(categoryMapper, Mockito.times(1))
                .entityToResponse(foundCategoryEntities.get(i));
        }
        Mockito.verifyNoMoreInteractions(categoryRepository, categoryMapper);

        Assertions.assertEquals("Categories retrieved successfully.", serviceResponse.getMessage());
        Assertions.assertEquals(true, serviceResponse.isSuccess());
        Assertions.assertEquals(HttpStatus.OK.value(), serviceResponse.getStatus());
        Assertions.assertNotNull(serviceResponse.getData());
        Assertions.assertEquals(length, serviceResponse.getData().size());
        for(int i = 0; i < length; i++)
        {
            Assertions.assertEquals(foundCategoryResponses.get(i).getId(), serviceResponse.getData().get(i).getId());
            Assertions.assertEquals(foundCategoryResponses.get(i).getName(), serviceResponse.getData().get(i).getName());
        }
    }

    @Test
    @DisplayName(value = "Testing updateCategory() method when Category exists")
    public void givenUpdateCategoryWhenCategoryExistsThenReturnSuccessResponse()
    {
        CategoryRequest categoryRequest = CategoryRequest.builder().name("drama").build();    
        CategoryEntity foundCategoryEntity = CategoryEntity.builder().id(1L).name("comedy").build();
        CategoryEntity updatedCategoryEntity = CategoryEntity.builder().id(1L).name("drama").build();
        
        Mockito.when(categoryRepository.findById(1L)).thenReturn(Optional.of(foundCategoryEntity));

        Mockito
            .doAnswer(invocation -> {
                CategoryRequest mapperCategoryRequest = invocation.getArgument(0);
                CategoryEntity mapperCategoryEntity = invocation.getArgument(1);
                mapperCategoryEntity.setName(mapperCategoryRequest.getName());
                return null;
            })
            .when(categoryMapper)
            .convertRequestToEntity(
                Mockito.any(CategoryRequest.class), 
                Mockito.any(CategoryEntity.class)
            );

        Mockito.when(categoryRepository.save(updatedCategoryEntity)).thenReturn(updatedCategoryEntity);

        BaseResponse<Void> serviceResponse = categoryService.updateCategory(1L, categoryRequest); 

        Mockito.verify(categoryRepository, Mockito.times(1)).findById(1L);
        Mockito.verify(categoryMapper, Mockito.times(1))
            .convertRequestToEntity(categoryRequest, foundCategoryEntity);
        Mockito.verify(categoryRepository, Mockito.times(1)).save(updatedCategoryEntity);
        Mockito.verifyNoMoreInteractions(categoryRepository, categoryMapper);

        Assertions.assertEquals(true, serviceResponse.isSuccess());
        Assertions.assertEquals("Category updated successfully.", serviceResponse.getMessage());
        Assertions.assertEquals(HttpStatus.OK.value(), serviceResponse.getStatus());
    }
    
}
