package az.atlacademy.libraryadp.model.dto.request;

import java.time.LocalDateTime;

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
    private LocalDateTime orderTimestamp; 
    private LocalDateTime returnTimestamp;
}
