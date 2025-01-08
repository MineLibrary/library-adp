package az.atlacademy.library_management.model.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orders")
public class OrderEntity 
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;     

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "book_id")
    private BookEntity book; 

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "student_id")
    private StudentEntity student; 

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime orderTimestamp = LocalDateTime.now();

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime returnTimestamp = LocalDateTime.now().plusDays(7);
}
