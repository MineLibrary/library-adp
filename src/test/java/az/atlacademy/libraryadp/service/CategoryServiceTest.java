package az.atlacademy.libraryadp.service;

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
    @DisplayName(value = "Testing createCategory() method in case of correct data.")
    public void givenCreateCategoryThenReturnSuccessResponse()
    {
        CategoryRequest createCategoryRequest = new CategoryRequest("drama");
        CategoryEntity createCategoryEntity = CategoryEntity.builder().name("drama").build();
        CategoryEntity createdCategoryEntity = CategoryEntity.builder().id(1L).name("drama").build();

        Mockito.when(categoryMapper.requestToEntity(createCategoryRequest)).thenReturn(createCategoryEntity); 
        Mockito.when(categoryRepository.save(createCategoryEntity)).thenReturn(createdCategoryEntity);

        BaseResponse<Void> serviceResponse = categoryService.createCategory(createCategoryRequest);
        
        Mockito.verify(categoryMapper, Mockito.times(1)).requestToEntity(createCategoryRequest);
        Mockito.verify(categoryRepository, Mockito.times(1)).save(createCategoryEntity);

        Assertions.assertEquals(true, serviceResponse.isSuccess());
        Assertions.assertEquals("Category created successfully.", serviceResponse.getMessage());
        Assertions.assertEquals(HttpStatus.CREATED.value(), serviceResponse.getStatus());
    }

    @Test
    @DisplayName(value = "Testing getCategoryById() method when Category Exists")
    public void givenGetCategoryByIdWhenCategoryExistsThenReturnBaseResponseOfCategoryResponse()
    {
        CategoryEntity foundCategoryEntity = CategoryEntity.builder().id(1L).name("drama").build(); 
        CategoryResponse foundCategoryResponse = CategoryResponse.builder().id(1L).name("drama").build();

        Mockito.when(categoryRepository.findById(1L)).thenReturn(Optional.of(foundCategoryEntity));
        Mockito.when(categoryMapper.entityToResponse(foundCategoryEntity)).thenReturn(foundCategoryResponse); 

        BaseResponse<CategoryResponse> serviceResponse = categoryService.getCategoryById(1L);
        
        Mockito.verify(categoryRepository, Mockito.times(1)).findById(1L);
        Mockito.verify(categoryMapper, Mockito.times(1)).entityToResponse(foundCategoryEntity);

        Assertions.assertEquals(true, serviceResponse.isSuccess());
        Assertions.assertEquals("Category retrieved successfully.", serviceResponse.getMessage());
        Assertions.assertEquals(HttpStatus.OK.value(), serviceResponse.getStatus());
        Assertions.assertNotNull(serviceResponse.getData());
        Assertions.assertEquals(1L, serviceResponse.getData().getId());
        Assertions.assertEquals("drama", serviceResponse.getData().getName());
    }

    
}
