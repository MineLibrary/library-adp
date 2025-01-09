package az.atlacademy.libraryadp.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest 
{
    private long studentId; 
    private long bookId;
    private int daysToReturn;
}
