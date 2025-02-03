package az.atlacademy.libraryadp.service;

import java.util.List;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
            .firstName("Yenifilankes")
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

    @Test
    @DisplayName(value = "Testing getStudentById() method when student exists")
    public void givenGetStudentByIdWhenStudentExistsThenReturnBaseResponseOfStudentResponse()
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

        Mockito.when(studentRepository.findById(1L)).thenReturn(Optional.of(foundStudentEntity));
        Mockito.when(studentMapper.entityToResponse(foundStudentEntity)).thenReturn(foundStudentResponse);
        
        BaseResponse<StudentResponse> serviceResponse = studentService.getStudentById(1L);

        Mockito.verify(studentRepository, Mockito.times(1)).findById(1L);
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
    @DisplayName(value = "Testing getStudentById() method when student does not exist")
    public void givenGetStudentByIdWhenStudentDoesNotExistThenThrowStudentNotFoundException()
    {
        Mockito.when(studentRepository.findById(1L)).thenReturn(Optional.empty());

        StudentNotFoundException exception = Assertions
            .assertThrows(StudentNotFoundException.class, () -> studentService.getStudentById(1L));

        Assertions.assertEquals("Student not found with id : 1", exception.getMessage());

        Mockito.verify(studentRepository, Mockito.times(1)).findById(1L);
        Mockito.verifyNoMoreInteractions(studentRepository, studentMapper);
    }

    @Test
    @DisplayName(value = "Testing getStudents() method")
    public void givenGetStudentsThenReturnBaseResponseOfListOfStudents()
    {
        int pageNumber = 0, pageSize = 2; 
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        
        List<StudentEntity> foundStudentEntities = List
            .of(
                 StudentEntity.builder()
                    .id(1L)
                    .trustRate(100)
                    .email("filankes@gmail.com")
                    .finCode("5SFJ13D")
                    .firstName("Filankes")
                    .lastName("Filankesov")
                    .phoneNumber("050 550 50 50")
                    .build(),
                 StudentEntity.builder()
                    .id(2L)
                    .trustRate(90)
                    .email("yenifilankes@gmail.com")
                    .finCode("5SFJ13D")
                    .firstName("Yenifilankes")
                    .lastName("Yenifilankesov")
                    .phoneNumber("050 550 50 50")
                    .build()
             );

        List<StudentResponse> foundStudentResponses = List
            .of(
                 StudentResponse.builder()
                    .id(1L)
                    .trustRate(100)
                    .email("filankes@gmail.com")
                    .finCode("5SFJ13D")
                    .firstName("Filankes")
                    .lastName("Filankesov")
                    .phoneNumber("050 550 50 50")
                    .build(),
                 StudentResponse.builder()
                    .id(2L)
                    .trustRate(90)
                    .email("yenifilankes@gmail.com")
                    .finCode("5SFJ13D")
                    .firstName("Yenifilankes")
                    .lastName("Yenifilankesov")
                    .phoneNumber("050 550 50 50")
                    .build()
             );

        Page<StudentEntity> foundStudentPage = new PageImpl<>(foundStudentEntities); 

        Mockito.when(studentRepository.findAll(pageable)).thenReturn(foundStudentPage); 
        for (int i = 0; i < pageSize; i++) 
        {
            Mockito.when(studentMapper.entityToResponse(foundStudentEntities.get(i)))
                .thenReturn(foundStudentResponses.get(i));    
        }

        BaseResponse<List<StudentResponse>> serviceResponse = studentService.getStudents(pageNumber, pageSize);

        Mockito.verify(studentRepository, Mockito.times(1)).findAll(pageable);
        for (int i = 0; i < pageSize; i++)
        {
            Mockito.verify(studentMapper, Mockito.times(1))
                .entityToResponse(foundStudentEntities.get(i));
        }
        Mockito.verifyNoMoreInteractions(studentRepository, studentMapper);

        Assertions.assertEquals(true, serviceResponse.isSuccess());
        Assertions.assertEquals("Students retrieved successfully.", serviceResponse.getMessage());
        Assertions.assertEquals(HttpStatus.OK.value(), serviceResponse.getStatus());
        Assertions.assertNotNull(serviceResponse.getData());
        Assertions.assertEquals(pageSize, serviceResponse.getData().size());
        for (int i = 0; i < pageSize; i++) 
        {
            Assertions.assertEquals(foundStudentResponses.get(i).getId(), serviceResponse.getData().get(i).getId());
            Assertions.assertEquals(foundStudentResponses.get(i).getTrustRate(), serviceResponse.getData().get(i).getTrustRate());
            Assertions.assertEquals(foundStudentResponses.get(i).getEmail(), serviceResponse.getData().get(i).getEmail());
            Assertions.assertEquals(foundStudentResponses.get(i).getFinCode(), serviceResponse.getData().get(i).getFinCode());
            Assertions.assertEquals(foundStudentResponses.get(i).getFirstName(), serviceResponse.getData().get(i).getFirstName());
            Assertions.assertEquals(foundStudentResponses.get(i).getLastName(), serviceResponse.getData().get(i).getLastName());
            Assertions.assertEquals(foundStudentResponses.get(i).getPhoneNumber(), serviceResponse.getData().get(i).getPhoneNumber());    
        }
    }

    @Test
    @DisplayName(value = "Testing updateStudent() method when student exists")
    public void givenUpdateStudentWhenStudentExistsThenReturnSuccessResponse()
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

        StudentRequest updatedStudentRequest = StudentRequest.builder()
            .email("yenifilankes@gmail.com")
            .finCode("5SFJ13D")
            .firstName("Yenifilankes")
            .lastName("Yenifilankesov")
            .phoneNumber("050 550 50 50")
            .build();

        Mockito.when(studentRepository.findById(1L)).thenReturn(Optional.of(foundStudentEntity)); 

        Mockito
            .doAnswer(invocation -> {
                StudentRequest mapperStudentRequest = invocation.getArgument(0);
                StudentEntity mapperStudentEntity = invocation.getArgument(1);

                mapperStudentEntity.setEmail(mapperStudentRequest.getEmail());
                mapperStudentEntity.setFinCode(mapperStudentRequest.getFinCode());
                mapperStudentEntity.setFirstName(mapperStudentRequest.getFirstName());
                mapperStudentEntity.setLastName(mapperStudentRequest.getLastName());
                mapperStudentEntity.setPhoneNumber(mapperStudentRequest.getPhoneNumber());

                return null; 
            })
            .when(studentMapper)
            .convertRequestToEntity(
                Mockito.any(StudentRequest.class), 
                Mockito.any(StudentEntity.class)
            );

        BaseResponse<Void> serviceResponse = studentService.updateStudent(1L, updatedStudentRequest);

        Mockito.verify(studentRepository, Mockito.times(1)).findById(1L);
        Mockito.verify(studentMapper, Mockito.times(1))
            .convertRequestToEntity(updatedStudentRequest, foundStudentEntity);
        Mockito.verify(studentRepository, Mockito.times(1)).save(foundStudentEntity);
        Mockito.verifyNoMoreInteractions(studentRepository, studentMapper);

        Assertions.assertEquals(true, serviceResponse.isSuccess());
        Assertions.assertEquals("Student updated successfully.", serviceResponse.getMessage());
        Assertions.assertEquals(HttpStatus.OK.value(), serviceResponse.getStatus());
        Assertions.assertNull(serviceResponse.getData());
    }

    @Test
    @DisplayName(value = "Testing updateStudent() method when student does not exist")
    public void givenUpdateStudentWhenStudentDoesNotExistThenThrowStudentNotFoundException() 
    {
        StudentRequest updatedStudentRequest = StudentRequest.builder()
            .email("yenifilankes@gmail.com")
            .finCode("5SFJ13D")
            .firstName("Yenifilankes")
            .lastName("Yenifilankesov")
            .phoneNumber("050 550 50 50")
            .build();

        Mockito.when(studentRepository.findById(1L)).thenReturn(Optional.empty());
        
        StudentNotFoundException exception = Assertions
            .assertThrows(
                StudentNotFoundException.class, 
                () -> studentService.updateStudent(1L, updatedStudentRequest)    
            );
            
        Assertions.assertEquals("Student not found with id : 1", exception.getMessage());

        Mockito.verify(studentRepository, Mockito.times(1)).findById(1L);
        Mockito.verifyNoMoreInteractions(studentRepository, studentMapper);
    }

    @Test
    @DisplayName(value = "Testing deleteStudent() method")
    public void givenDeleteStudentThenReturnSuccessResponse()
    {
        BaseResponse<Void> serviceResponse = studentService.deleteStudent(1L); 

        Mockito.verify(studentRepository, Mockito.times(1)).deleteById(1L);
        Mockito.verifyNoMoreInteractions(studentRepository, studentMapper);

        Assertions.assertEquals(true, serviceResponse.isSuccess());
        Assertions.assertEquals("Student deleted successfully.", serviceResponse.getMessage());
        Assertions.assertEquals(HttpStatus.OK.value(), serviceResponse.getStatus());
        Assertions.assertNull(serviceResponse.getData());
    }

    @Test
    @DisplayName(value = "Testing updateStudentTrustRate() method when student exists")
    public void givenUpdateStudentTrustRateWhenStudentExistsThenReturnSuccessResponse()
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

        StudentEntity updatedStudentEntity = StudentEntity.builder()
            .id(1L)
            .trustRate(90)
            .email("filankes@gmail.com")
            .finCode("5SFJ13D")
            .firstName("Filankes")
            .lastName("Filankesov")
            .phoneNumber("050 550 50 50")
            .build();

        Mockito.when(studentRepository.findById(1L)).thenReturn(Optional.of(foundStudentEntity));
        Mockito.when(studentRepository.save(updatedStudentEntity)).thenReturn(updatedStudentEntity); 

        BaseResponse<Void> serviceResponse = studentService.updateStudentTrustRate(1L, 90);

        Mockito.verify(studentRepository, Mockito.times(1)).findById(1L);
        Mockito.verify(studentRepository, Mockito.times(1)).save(updatedStudentEntity);
        Mockito.verifyNoMoreInteractions(studentRepository, studentMapper);

        Assertions.assertEquals(true, serviceResponse.isSuccess());
        Assertions.assertEquals("Trust rate updated successfully.", serviceResponse.getMessage());
        Assertions.assertEquals(HttpStatus.OK.value(), serviceResponse.getStatus());
        Assertions.assertNull(serviceResponse.getData());
    }

    @Test
    @DisplayName(value = "Testing updateStudentTrustRate() method when student does not exist")
    public void givenUpdateStudentTrustRateWhenStudentDoesNotExistThenThrowStudentNotFoundException() 
    {
        Mockito.when(studentRepository.findById(1L)).thenReturn(Optional.empty());
        
        StudentNotFoundException exception = Assertions
            .assertThrows(
                StudentNotFoundException.class, 
                () -> studentService.updateStudentTrustRate(1L, 90)    
            );
            
        Assertions.assertEquals("Student not found with id : 1", exception.getMessage());

        Mockito.verify(studentRepository, Mockito.times(1)).findById(1L);
        Mockito.verifyNoMoreInteractions(studentRepository, studentMapper);
    }

    @Test
    @DisplayName(value = "Testing updateStudentTrustRate() method when trust rate is greater than 100")
    public void givenUpdateStudentTrustRateWhenTrustRateIsGreaterThanOneHundredThenReturnSuccessResponse()
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

        StudentEntity updatedStudentEntity = StudentEntity.builder()
            .id(1L)
            .trustRate(100)
            .email("filankes@gmail.com")
            .finCode("5SFJ13D")
            .firstName("Filankes")
            .lastName("Filankesov")
            .phoneNumber("050 550 50 50")
            .build();

        Mockito.when(studentRepository.findById(1L)).thenReturn(Optional.of(foundStudentEntity));
        Mockito.when(studentRepository.save(updatedStudentEntity)).thenReturn(updatedStudentEntity);

        BaseResponse<Void> serviceResponse = studentService.updateStudentTrustRate(1L, 110);
        
        Mockito.verify(studentRepository, Mockito.times(1)).findById(1L);
        Mockito.verify(studentRepository, Mockito.times(1)).save(updatedStudentEntity);
        Mockito.verifyNoMoreInteractions(studentRepository, studentMapper);

        Assertions.assertEquals(true, serviceResponse.isSuccess());
        Assertions.assertEquals("Trust rate updated successfully.", serviceResponse.getMessage());
        Assertions.assertEquals(HttpStatus.OK.value(), serviceResponse.getStatus());
        Assertions.assertNull(serviceResponse.getData());
    }

    @Test
    @DisplayName(value = "Testing updateStudentTrustRate() method when trust rate is less than zero")
    public void givenUpdateStudentTrustRateWhenTrustRateIsLessThanZeroThenReturnSuccessResponse()
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

        StudentEntity updatedStudentEntity = StudentEntity.builder()
            .id(1L)
            .trustRate(0)
            .email("filankes@gmail.com")
            .finCode("5SFJ13D")
            .firstName("Filankes")
            .lastName("Filankesov")
            .phoneNumber("050 550 50 50")
            .build();

        Mockito.when(studentRepository.findById(1L)).thenReturn(Optional.of(foundStudentEntity));
        Mockito.when(studentRepository.save(updatedStudentEntity)).thenReturn(updatedStudentEntity);

        BaseResponse<Void> serviceResponse = studentService.updateStudentTrustRate(1L, -10);
        
        Mockito.verify(studentRepository, Mockito.times(1)).findById(1L);
        Mockito.verify(studentRepository, Mockito.times(1)).save(updatedStudentEntity);
        Mockito.verifyNoMoreInteractions(studentRepository, studentMapper);

        Assertions.assertEquals(true, serviceResponse.isSuccess());
        Assertions.assertEquals("Trust rate updated successfully.", serviceResponse.getMessage());
        Assertions.assertEquals(HttpStatus.OK.value(), serviceResponse.getStatus());
        Assertions.assertNull(serviceResponse.getData());
    }

    @Test
    @DisplayName(value = "Testing getStudentEntityById() method when student exists")
    public void givenGetStudentEntityByIdWhenStudentExistsThenReturnStudentEntity()
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

        Mockito.when(studentRepository.findById(1L)).thenReturn(Optional.of(foundStudentEntity));

        StudentEntity serviceResponse = studentService.getStudentEntityById(1L);

        Mockito.verify(studentRepository, Mockito.times(1)).findById(1L);
        Mockito.verifyNoMoreInteractions(studentRepository, studentMapper);

        Assertions.assertNotNull(serviceResponse);
        Assertions.assertEquals(foundStudentEntity.getId(), serviceResponse.getId());
        Assertions.assertEquals(foundStudentEntity.getTrustRate(), serviceResponse.getTrustRate());
        Assertions.assertEquals(foundStudentEntity.getEmail(), serviceResponse.getEmail());
        Assertions.assertEquals(foundStudentEntity.getFinCode(), serviceResponse.getFinCode());
        Assertions.assertEquals(foundStudentEntity.getFirstName(), serviceResponse.getFirstName());
        Assertions.assertEquals(foundStudentEntity.getLastName(), serviceResponse.getLastName());
        Assertions.assertEquals(foundStudentEntity.getPhoneNumber(), serviceResponse.getPhoneNumber());
    }

    @Test
    @DisplayName(value = "Testing getStudentEntityById() method when student does not exist")
    public void givenGetStudentEntityByIdWhenStudentDoesNotExistThenThrowStudentNotFoundException()
    {
        Mockito.when(studentRepository.findById(1L)).thenReturn(Optional.empty());
        
        StudentNotFoundException exception = Assertions
            .assertThrows(StudentNotFoundException.class, () -> studentService.getStudentEntityById(1L));
            
        Assertions.assertEquals("Student not found with id : 1", exception.getMessage());

        Mockito.verify(studentRepository, Mockito.times(1)).findById(1L);
        Mockito.verifyNoMoreInteractions(studentRepository, studentMapper);
    }
}
