package az.atlacademy.libraryadp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;

import az.atlacademy.libraryadp.model.dto.request.CategoryRequest;
import az.atlacademy.libraryadp.model.dto.response.CategoryResponse;
import az.atlacademy.libraryadp.model.entity.CategoryEntity;

@Mapper(componentModel = "spring")
public interface CategoryMapper 
{
    @Mappings(value = {
        @Mapping(target = "id", ignore = true), 
        @Mapping(target = "books", ignore = true)
    })
    public CategoryEntity requestToEntity(CategoryRequest request); 

    public CategoryResponse entityToResponse(CategoryEntity entity);

    @Mappings(value = {
        @Mapping(target = "id", ignore = true), 
        @Mapping(target = "books", ignore = true)
    })
    public void convertRequestToEntity(CategoryRequest request, @MappingTarget CategoryEntity entity);
}
