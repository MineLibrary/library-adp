package az.atlacademy.libraryadp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;

import az.atlacademy.libraryadp.model.dto.request.OrderRequest;
import az.atlacademy.libraryadp.model.dto.response.OrderResponse;
import az.atlacademy.libraryadp.model.entity.OrderEntity;

@Mapper(componentModel = "spring")
public interface OrderMapper 
{
    @Mappings(value = {
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "book", ignore = true), 
        @Mapping(target = "student", ignore = true)
    })
    public OrderEntity requestToEntity(OrderRequest request); 

    public OrderResponse entityToResponse(OrderEntity entity);

    @Mappings(value = {
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "book", ignore = true),
        @Mapping(target = "student", ignore = true)
    })
    public void convertRequestToEntity(OrderRequest request, @MappingTarget OrderEntity entity);
}
