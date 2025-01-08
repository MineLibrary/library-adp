package az.atlacademy.libraryadp.model.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse 
{
    private long id; 
    private LocalDateTime orderTimestamp; 
    private LocalDateTime returnTimestamp;

    @ToString.Exclude
    private BookResponse book; 
    
    @ToString.Exclude
    private StudentResponse student; 
}
