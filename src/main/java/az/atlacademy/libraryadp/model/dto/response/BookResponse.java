package az.atlacademy.libraryadp.model.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookResponse 
{
    private long id; 
    private String title; 
    private int stock; 

    @ToString.Exclude
    private List<AuthorResponse> authors; 

    @ToString.Exclude
    private CategoryResponse category; 

    @ToString.Exclude
    private List<OrderResponse> orders; 
}
