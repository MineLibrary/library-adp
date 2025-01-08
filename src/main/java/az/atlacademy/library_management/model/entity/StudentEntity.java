package az.atlacademy.library_management.model.entity;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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
@Table(name = "students")
public class StudentEntity 
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; 

    @Column(nullable = false, unique = true)
    private String finCode; 

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;
    
    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String phoneNumber;

    @Builder.Default
    @Column(nullable = false)
    private int trustRate = 100;

    @ToString.Exclude
    @OneToMany(mappedBy = "student")
    private List<OrderEntity> orders; 
}
