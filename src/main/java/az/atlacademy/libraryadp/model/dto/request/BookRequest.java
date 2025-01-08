package az.atlacademy.libraryadp.model.dto.request;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookRequest 
{
    private String title; 
    private int stock;    
    private List<Long> authorIds; 
    private Long categoryId;
}
