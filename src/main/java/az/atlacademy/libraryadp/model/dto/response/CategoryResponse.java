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
public class CategoryResponse 
{
    private long id; 
    private String name;
    
    @ToString.Exclude
    private List<BookResponse> books; 
}
