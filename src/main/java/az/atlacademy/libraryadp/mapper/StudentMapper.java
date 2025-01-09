package az.atlacademy.libraryadp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;

import az.atlacademy.libraryadp.model.dto.request.StudentRequest;
import az.atlacademy.libraryadp.model.dto.response.StudentResponse;
import az.atlacademy.libraryadp.model.entity.StudentEntity;

@Mapper(componentModel = "spring")
public interface StudentMapper 
{
    @Mappings(value = {
        @Mapping(target = "id", ignore = true), 
        @Mapping(target = "orders", ignore = true), 
        @Mapping(target = "trustRate", ignore = true)
    })
    public StudentEntity requestToEntity(StudentRequest request); 
    
    public StudentResponse entityToResponse(StudentEntity entity);

    @Mappings(value = {
        @Mapping(target = "id", ignore = true), 
        @Mapping(target = "orders", ignore = true),
        @Mapping(target = "trustRate", ignore = true)
    })
    public void convertRequestToEntity(StudentRequest request, @MappingTarget StudentEntity entity);
}
