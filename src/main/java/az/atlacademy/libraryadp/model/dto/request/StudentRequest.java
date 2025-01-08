package az.atlacademy.libraryadp.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentRequest 
{
    private String finCode;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private int trustRate;
}
