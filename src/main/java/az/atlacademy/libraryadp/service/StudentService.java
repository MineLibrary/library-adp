package az.atlacademy.libraryadp.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import az.atlacademy.libraryadp.exception.FinCodeAlreadyExistsException;
import az.atlacademy.libraryadp.exception.StudentNotFoundException;
import az.atlacademy.libraryadp.mapper.StudentMapper;
import az.atlacademy.libraryadp.model.dto.request.StudentRequest;
import az.atlacademy.libraryadp.model.dto.response.BaseResponse;
import az.atlacademy.libraryadp.model.dto.response.StudentResponse;
import az.atlacademy.libraryadp.model.entity.StudentEntity;
import az.atlacademy.libraryadp.repository.StudentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentService 
{
    private final StudentRepository studentRepository; 
    private final StudentMapper studentMapper; 

    @Transactional
    public BaseResponse<Void> createStudent(StudentRequest studentRequest)
    {
        try
        {
            this.getStudentByFinCode(studentRequest.getFinCode()); 
            throw new FinCodeAlreadyExistsException("A student with this fin code already exists.");
        }
        catch (StudentNotFoundException e) {}

        StudentEntity studentEntity = studentMapper.requestToEntity(studentRequest);
        studentRepository.save(studentEntity);

        log.info("Created a new student : {}", studentEntity.toString());
        
        return BaseResponse.<Void>builder()
                .success(true)
                .message("Student created successfully.")
                .status(HttpStatus.CREATED.value())
                .build();
    }

    public BaseResponse<StudentResponse> getStudentByFinCode(String finCode)
    {
        StudentEntity studentEntity = studentRepository.findByFinCode(finCode)
            .orElseThrow(() -> new StudentNotFoundException("Student not found with fin code : " + finCode));
        
        StudentResponse studentResponse = studentMapper.entityToResponse(studentEntity);

        log.info("Retrieved student with fin code: {}", finCode);

        return BaseResponse.<StudentResponse>builder()
                .success(true)
                .data(studentResponse)
                .message("Student retrieved successfully.")
                .status(HttpStatus.OK.value())
                .build();
    }

    public BaseResponse<StudentResponse> getStudentById(long studentId)
    {
        StudentEntity studentEntity = studentRepository.findById(studentId)
           .orElseThrow(() -> new StudentNotFoundException("Student not found with id : " + studentId));
        
        StudentResponse studentResponse = studentMapper.entityToResponse(studentEntity);

        log.info("Retrieved student with id: {}", studentId);

        return BaseResponse.<StudentResponse>builder()
                .success(true)
                .data(studentResponse)
                .message("Student retrieved successfully.")
                .status(HttpStatus.OK.value())
                .build();
    }

    public BaseResponse<List<StudentResponse>> getStudents(int pageNumber, int pageSize)
    {
        Pageable pageable = PageRequest.of(pageNumber, pageSize); 
        Page<StudentEntity> studentPage = studentRepository.findAll(pageable); 
        
        List<StudentEntity> studentEntities = studentPage.getContent(); 

        List<StudentResponse> studentResponses = studentEntities.stream()
            .map(studentMapper::entityToResponse).collect(Collectors.toList());

        log.info("Retrieved students (page: {}, size: {})", pageNumber, pageSize);

        return BaseResponse.<List<StudentResponse>>builder()
                .success(true)
                .data(studentResponses)
                .message("Students retrieved successfully.")
                .status(HttpStatus.OK.value())
                .build();
    }

    @Transactional
    public BaseResponse<Void> updateStudent(long studentId, StudentRequest studentRequest)
    {
        StudentEntity studentEntity = studentRepository.findById(studentId)
            .orElseThrow(() -> new StudentNotFoundException("Student not found with id : " + studentId));

        studentMapper.convertRequestToEntity(studentRequest, studentEntity);
        studentRepository.save(studentEntity);

        log.info("Updated student with id: {}", studentId);

        return BaseResponse.<Void>builder()
                .success(true)
                .message("Student updated successfully.")
                .status(HttpStatus.OK.value())
                .build();
    }

    @Transactional
    public BaseResponse<Void> deleteStudent(long studentId)
    {
        studentRepository.deleteById(studentId);
        
        log.info("Deleted student with id: {}", studentId);

        return BaseResponse.<Void>builder()
                .success(true)
                .message("Student deleted successfully.")
                .status(HttpStatus.OK.value())
                .build();
    }

    @Transactional 
    public BaseResponse<Void> updateStudentTrustRate(long studentId, int trustRate)
    {
        StudentEntity studentEntity = studentRepository.findById(studentId)
            .orElseThrow(() -> new StudentNotFoundException("Student not found with id : " + studentId));

        studentEntity.setTrustRate(trustRate);
        studentRepository.save(studentEntity);

        log.info("Updated trust rate for student with id: {}", studentId);
        
        return BaseResponse.<Void>builder()
                .success(true)
                .message("Trust rate updated successfully.")
                .status(HttpStatus.OK.value())
                .build();
    }

    protected StudentEntity getStudentEntityById(long studentId)
    {
        return studentRepository.findById(studentId)
           .orElseThrow(() -> new StudentNotFoundException("Student not found with id : " + studentId));
    }
}
