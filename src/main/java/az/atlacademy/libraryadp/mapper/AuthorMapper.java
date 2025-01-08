package az.atlacademy.libraryadp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;

import az.atlacademy.libraryadp.model.dto.request.AuthorRequest;
import az.atlacademy.libraryadp.model.dto.response.AuthorResponse;
import az.atlacademy.libraryadp.model.entity.AuthorEntity;

@Mapper(componentModel = "spring")
public interface AuthorMapper 
{
    @Mappings(value = {
        @Mapping(target = "books", ignore = true), 
        @Mapping(target = "id", ignore = true)
    })
    public AuthorEntity requestToEntity(AuthorRequest request);
    
    public AuthorResponse entityToResponse(AuthorEntity entity);

    @Mappings(value = {
        @Mapping(target = "books", ignore = true), 
        @Mapping(target = "id", ignore = true)
    })
    public void convertRequestToEntity(AuthorRequest request, @MappingTarget AuthorEntity entity); 
}
