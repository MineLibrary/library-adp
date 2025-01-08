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
public class StudentResponse 
{
    private long id; 
    private String finCode;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private int trustRate;

    @ToString.Exclude
    private List<OrderResponse> orders; 
}
