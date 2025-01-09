package az.atlacademy.libraryadp.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaseResponse <T>
{
    private T data; 
    private String message;
    private int status; 
    private boolean success; 
}
