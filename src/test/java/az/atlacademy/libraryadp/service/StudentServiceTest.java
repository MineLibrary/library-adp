package az.atlacademy.libraryadp.service;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import az.atlacademy.libraryadp.exception.FinCodeAlreadyExistsException;
import az.atlacademy.libraryadp.exception.StudentNotFoundException;
import az.atlacademy.libraryadp.mapper.StudentMapper;
import az.atlacademy.libraryadp.model.dto.request.StudentRequest;
import az.atlacademy.libraryadp.model.dto.response.BaseResponse;
import az.atlacademy.libraryadp.model.dto.response.StudentResponse;
import az.atlacademy.libraryadp.model.entity.StudentEntity;
import az.atlacademy.libraryadp.repository.StudentRepository;

@ExtendWith(value = MockitoExtension.class)
public class StudentServiceTest 
{
    @Spy
    @InjectMocks
    private StudentService studentService; 

    @Mock
    private StudentRepository studentRepository; 

    @Mock
    private StudentMapper studentMapper; 
    
    @Test
    @DisplayName(value = "Testing createStudent() method when fin code does not exist")
    public void givenCreateStudentWhenFinCodeDoesNotExistThenReturnSuccessResponse()
    {
        StudentRequest createStudentRequest = StudentRequest.builder()
            .email("filankes@gmail.com")
            .finCode("5SFJ13D")
            .firstName("Filankes")
            .lastName("Filankesov")
            .phoneNumber("050 550 50 50")
            .build(); 

        StudentEntity createStudentEntity = StudentEntity.builder()
            .email("filankes@gmail.com")
            .finCode("5SFJ13D")
            .firstName("Filankes")
            .lastName("Filankesov")
            .phoneNumber("050 550 50 50")
            .build(); 

        StudentEntity createdStudentEntity = StudentEntity.builder()
            .id(1L)
            .trustRate(100)
            .email("filankes@gmail.com")
            .finCode("5SFJ13D")
            .firstName("Filankes")
            .lastName("Filankesov")
            .phoneNumber("050 550 50 50")
            .build();

        String finCode = "5SFJ13D";

        Mockito.doThrow(new StudentNotFoundException("Student not found with fin code : " + finCode))
            .when(studentService).getStudentByFinCode(finCode);
        
        Mockito.when(studentMapper.requestToEntity(createStudentRequest)).thenReturn(createStudentEntity); 
        Mockito.when(studentRepository.save(createStudentEntity)).thenReturn(createdStudentEntity);

        BaseResponse<Void> serviceResponse = studentService.createStudent(createStudentRequest); 

        Mockito.verify(studentService, Mockito.times(1)).getStudentByFinCode(finCode);
        Mockito.verify(studentMapper, Mockito.times(1)).requestToEntity(createStudentRequest);
        Mockito.verify(studentRepository, Mockito.times(1)).save(createStudentEntity);
        Mockito.verifyNoMoreInteractions(studentRepository, studentMapper);
        
        Assertions.assertEquals(true, serviceResponse.isSuccess());
        Assertions.assertEquals("Student created successfully.", serviceResponse.getMessage());
        Assertions.assertEquals(HttpStatus.CREATED.value(), serviceResponse.getStatus());
        Assertions.assertNull(serviceResponse.getData());
    }

    @Test
    @DisplayName(value = "Testing createStudent() method when fin code exists")
    public void givenCreateStudentWhenFinCodeExistsThenThrowFinCodeAlreadyExistsException()
    {
        StudentResponse foundStudentResponse = StudentResponse.builder()
            .id(1L)
            .trustRate(100)
            .email("filankes@gmail.com")
            .finCode("5SFJ13D")
            .firstName("Filankes")
            .lastName("Filankesov")
            .phoneNumber("050 550 50 50")
            .build();

        StudentRequest createStudentRequest = StudentRequest.builder()
            .email("yenifilankes@gmail.com")
            .finCode("5SFJ13D")
            .firstName("yenifilankes")
            .lastName("Yenifilankesov")
            .phoneNumber("050 550 50 50")
            .build();
        
        String finCode = "5SFJ13D";

        Mockito
            .doReturn(
                BaseResponse.<StudentResponse>builder()
                    .data(foundStudentResponse)
                    .success(true)
                    .message("Student retrieved successfully.")
                    .status(HttpStatus.OK.value())
                    .build()
            )
            .when(studentService)
            .getStudentByFinCode(finCode);

        FinCodeAlreadyExistsException exception = Assertions
            .assertThrows(FinCodeAlreadyExistsException.class, () -> studentService.createStudent(createStudentRequest));
        
        Assertions.assertEquals("A student with this fin code already exists.", exception.getMessage());

        Mockito.verify(studentService, Mockito.times(1)).getStudentByFinCode(finCode);
        Mockito.verifyNoMoreInteractions(studentMapper, studentRepository);
    }

    @Test
    @DisplayName(value = "Testing getStudentByFinCode() method when student exists")
    public void givenGetStudentByFinCodeWhenStudentExistsThenReturnBaseResponseOfStudentResponse()
    {
        StudentEntity foundStudentEntity = StudentEntity.builder()
            .id(1L)
            .trustRate(100)
            .email("filankes@gmail.com")
            .finCode("5SFJ13D")
            .firstName("Filankes")
            .lastName("Filankesov")
            .phoneNumber("050 550 50 50")
            .build();

        StudentResponse foundStudentResponse = StudentResponse.builder()
            .id(1L)
            .trustRate(100)
            .email("filankes@gmail.com")
            .finCode("5SFJ13D")
            .firstName("Filankes")
            .lastName("Filankesov")
            .phoneNumber("050 550 50 50")
            .build();

        String finCode = "5SFJ13D";

        Mockito.when(studentRepository.findByFinCode(finCode)).thenReturn(Optional.of(foundStudentEntity));
        Mockito.when(studentMapper.entityToResponse(foundStudentEntity)).thenReturn(foundStudentResponse);
        
        BaseResponse<StudentResponse> serviceResponse = studentService.getStudentByFinCode(finCode);

        Mockito.verify(studentRepository, Mockito.times(1)).findByFinCode(finCode);
        Mockito.verify(studentMapper, Mockito.times(1)).entityToResponse(foundStudentEntity);
        Mockito.verifyNoMoreInteractions(studentRepository, studentMapper);

        Assertions.assertEquals(true, serviceResponse.isSuccess());
        Assertions.assertEquals("Student retrieved successfully.", serviceResponse.getMessage());
        Assertions.assertEquals(HttpStatus.OK.value(), serviceResponse.getStatus());
        Assertions.assertNotNull(serviceResponse.getData());
        Assertions.assertEquals(1L, serviceResponse.getData().getId());
        Assertions.assertEquals("Filankes", serviceResponse.getData().getFirstName());
        Assertions.assertEquals("Filankesov", serviceResponse.getData().getLastName());
        Assertions.assertEquals("050 550 50 50", serviceResponse.getData().getPhoneNumber());
        Assertions.assertEquals(100, serviceResponse.getData().getTrustRate());
        Assertions.assertEquals("filankes@gmail.com", serviceResponse.getData().getEmail());
        Assertions.assertEquals("5SFJ13D", serviceResponse.getData().getFinCode());
    }

    @Test
    @DisplayName(value = "Testing getStudentByFinCode() method when student does not exist")
    public void givenGetStudentByFinCodeWhenStudentDoesNotExistThenThrowStudentNotFoundException()
    {
        Mockito.when(studentRepository.findByFinCode("5SFJ13D")).thenReturn(Optional.empty());

        StudentNotFoundException exception = Assertions
            .assertThrows(StudentNotFoundException.class, () -> studentService.getStudentByFinCode("5SFJ13D"));

        Assertions.assertEquals("Student not found with fin code : 5SFJ13D", exception.getMessage());

        Mockito.verify(studentRepository, Mockito.times(1)).findByFinCode("5SFJ13D");
        Mockito.verifyNoMoreInteractions(studentRepository, studentMapper);
    }
}
