package az.atlacademy.libraryadp.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import az.atlacademy.libraryadp.exception.CategoryNotFoundException;
import az.atlacademy.libraryadp.mapper.CategoryMapper;
import az.atlacademy.libraryadp.model.dto.request.CategoryRequest;
import az.atlacademy.libraryadp.model.dto.response.BaseResponse;
import az.atlacademy.libraryadp.model.dto.response.CategoryResponse;
import az.atlacademy.libraryadp.model.entity.CategoryEntity;
import az.atlacademy.libraryadp.repository.CategoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService 
{
    private final CategoryRepository categoryRepository; 
    private final CategoryMapper categoryMapper; 

    @Transactional
    public BaseResponse<Void> createCategory(CategoryRequest categoryRequest)
    {
        CategoryEntity categoryEntity = categoryMapper.requestToEntity(categoryRequest); 
        categoryRepository.save(categoryEntity); 

        log.info("Created new category: {}", categoryEntity.getName());
        
        return BaseResponse.<Void>builder()
                .success(true)
                .message("Category created successfully.")
                .status(HttpStatus.CREATED.value())
                .build(); 
    }

    public BaseResponse<CategoryResponse> getCategoryById(long categoryId)
    {
        CategoryEntity categoryEntity = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new CategoryNotFoundException("Category not found with id : " + categoryId));

        CategoryResponse categoryResponse = categoryMapper.entityToResponse(categoryEntity); 
        
        log.info("Retrieved category with id: {}", categoryId);

        return BaseResponse.<CategoryResponse>builder()
                .success(true)
                .data(categoryResponse)
                .message("Category retrieved successfully.")
                .status(HttpStatus.OK.value())
                .build();
    }

    public BaseResponse<List<CategoryResponse>> getAllCategories()
    {
        List<CategoryEntity> categoryEntities = categoryRepository.findAll();
        
        List<CategoryResponse> categoryResponses = categoryEntities.stream()
            .map(categoryMapper::entityToResponse).collect(Collectors.toList());

        log.info("Retrieved all categories");

        return BaseResponse.<List<CategoryResponse>>builder()
                .success(true)
                .data(categoryResponses)
                .message("Categories retrieved successfully.")
                .status(HttpStatus.OK.value())
                .build();
    }

    @Transactional
    public BaseResponse<Void> updateCategory(long categoryId, CategoryRequest categoryRequest)
    {
        CategoryEntity categoryEntity = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new CategoryNotFoundException("Category not found with id : " + categoryId));

        categoryMapper.convertRequestToEntity(categoryRequest, categoryEntity);
        categoryRepository.save(categoryEntity);

        log.info("Updated category with id: {}", categoryId);

        return BaseResponse.<Void>builder()
                .success(true)
                .message("Category updated successfully.")
                .status(HttpStatus.OK.value())
                .build();
    }

    @Transactional
    public BaseResponse<Void> deleteCategory(long categoryId)
    {
        categoryRepository.deleteById(categoryId);
        
        log.info("Deleted category with id: {}", categoryId);
        
        return BaseResponse.<Void>builder()
            .success(true)
            .message("Category deleted successfully.")
            .status(HttpStatus.OK.value())
            .build();
    }
}
