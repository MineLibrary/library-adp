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
public class AuthorResponse 
{
    private long id;
    private String firstName; 
    private String lastName;

    @ToString.Exclude
    private List<BookResponse> books; 
}
