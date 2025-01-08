package az.atlacademy.libraryadp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;

import az.atlacademy.libraryadp.model.dto.request.BookRequest;
import az.atlacademy.libraryadp.model.dto.response.BookResponse;
import az.atlacademy.libraryadp.model.entity.BookEntity;

@Mapper(componentModel = "spring")
public interface BookMapper 
{
    @Mappings(value = {
        @Mapping(target = "id", ignore = true), 
        @Mapping(target = "authors", ignore = true), 
        @Mapping(target = "orders", ignore = true), 
        @Mapping(target = "category", ignore = true)
    })
    public BookEntity requestToEntity(BookRequest request); 

    public BookResponse entityToResponse(BookEntity entity);

    @Mappings(value = {
        @Mapping(target = "id", ignore = true), 
        @Mapping(target = "authors", ignore = true), 
        @Mapping(target = "orders", ignore = true), 
        @Mapping(target = "category", ignore = true)
    })
    public void convertRequestToEntity(BookRequest request, @MappingTarget BookEntity entity); 
}
