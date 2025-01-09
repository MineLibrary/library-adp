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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import az.atlacademy.libraryadp.model.dto.request.CategoryRequest;
import az.atlacademy.libraryadp.model.dto.response.BaseResponse;
import az.atlacademy.libraryadp.model.dto.response.CategoryResponse;
import az.atlacademy.libraryadp.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/category")
public class CategoryController 
{
    private final CategoryService categoryService; 

    private static final String LOG_TEMPLATE = "{} request to /api/v1/category{}";

    @PostMapping
    @ResponseStatus(value = HttpStatus.CREATED)
    public BaseResponse<Void> createCategory(@RequestBody CategoryRequest categoryRequest)
    {
        log.info(LOG_TEMPLATE, "POST", "");
        return categoryService.createCategory(categoryRequest); 
    }

    @GetMapping
    @ResponseStatus(value = HttpStatus.OK)
    public BaseResponse<List<CategoryResponse>> getAllCategories()
    {
        log.info(LOG_TEMPLATE, "GET", "");
        return categoryService.getAllCategories();
    }

    @GetMapping(value = "/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    public BaseResponse<CategoryResponse> getCategoryById(@PathVariable(value = "id") long categoryId)
    {
        log.info(LOG_TEMPLATE, "GET", "/" + categoryId);
        return categoryService.getCategoryById(categoryId);
    }

    @PutMapping(value = "/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    public BaseResponse<Void> updateCategory(
        @PathVariable(value = "id") long categoryId, 
        @RequestBody CategoryRequest categoryRequest
    ){
        log.info(LOG_TEMPLATE, "PUT", "/" + categoryId);
        return categoryService.updateCategory(categoryId, categoryRequest);    
    }

    @DeleteMapping(value = "/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    public BaseResponse<Void> deleteCategory(@PathVariable(value = "id") long categoryId)
    {
        log.info(LOG_TEMPLATE, "DELETE", "/" + categoryId);
        return categoryService.deleteCategory(categoryId);
    }
    
}
