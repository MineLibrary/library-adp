package az.atlacademy.libraryadp.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import az.atlacademy.libraryadp.model.dto.request.StudentRequest;
import az.atlacademy.libraryadp.model.dto.response.BaseResponse;
import az.atlacademy.libraryadp.model.dto.response.StudentResponse;
import az.atlacademy.libraryadp.service.StudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/student")
public class StudentController 
{
    private final StudentService studentService; 
    
    private static final String LOG_TEMPLATE = "{} request to /api/v1/student{}";

    @PostMapping
    @ResponseStatus(value = HttpStatus.CREATED)
    public BaseResponse<Void> createStudent(@RequestBody StudentRequest studentRequest)
    {
        log.info(LOG_TEMPLATE, "POST", "");
        return studentService.createStudent(studentRequest);
    }

    @GetMapping(value = "/get-by-fin-code")
    @ResponseStatus(value = HttpStatus.OK)
    public BaseResponse<StudentResponse> getStudentByFinCode(
        @RequestParam(value = "finCode", required = true) String finCode
    ){
        log.info(LOG_TEMPLATE, "GET", "/get-by-fin-code");
        return studentService.getStudentByFinCode(finCode);
    }

    @GetMapping(value = "/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    public BaseResponse<StudentResponse> getStudentById(@PathVariable(value = "id") long studentId)
    {
        log.info(LOG_TEMPLATE, "GET", "/" + studentId);
        return studentService.getStudentById(studentId);
    }

    @GetMapping
    @ResponseStatus(value = HttpStatus.OK)
    public BaseResponse<List<StudentResponse>> getStudents(
        @RequestParam(value = "pageNumber", required = false, defaultValue = "0") int pageNumber,
        @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize
    ){
        log.info(LOG_TEMPLATE, "GET", "");
        return studentService.getStudents(pageNumber, pageSize);
    }

    @PutMapping(value = "/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    public BaseResponse<Void> updateStudent(
        @PathVariable(value = "id") long studentId, 
        @RequestBody StudentRequest studentRequest
    ){
        log.info(LOG_TEMPLATE, "PUT", "/" + studentId);
        return studentService.updateStudent(studentId, studentRequest);
    }

    @DeleteMapping(value = "/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    public BaseResponse<Void> deleteStudent(@PathVariable(value = "id") long studentId)
    {
        log.info(LOG_TEMPLATE, "DELETE", "/" + studentId);
        return studentService.deleteStudent(studentId);
    }

    @PatchMapping(value = "/{id}/update-trust-rate")
    @ResponseStatus(value = HttpStatus.OK)
    public BaseResponse<Void> updateStudentTrustRate(
        @PathVariable(value = "id") long studentId,
        @RequestParam(value = "trustRate", required = true) int trustRate
    ){
        log.info(LOG_TEMPLATE, "PATCH", "/" + studentId + "/update-trust-rate");
        return studentService.updateStudentTrustRate(studentId, trustRate);
    }
}
