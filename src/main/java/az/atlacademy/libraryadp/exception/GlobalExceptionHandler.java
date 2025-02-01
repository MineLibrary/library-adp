package az.atlacademy.libraryadp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import az.atlacademy.libraryadp.model.dto.response.BaseResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler 
{
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    @ExceptionHandler(value = AuthorNotFoundException.class)
    public BaseResponse<Void> handleAuthorNotFoundException(AuthorNotFoundException exception)
    {
        return BaseResponse.<Void>builder()
                .message(exception.getMessage())
                .success(false)
                .status(HttpStatus.NOT_FOUND.value())
                .build(); 
    }

    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    @ExceptionHandler(value = CategoryNotFoundException.class)
    public BaseResponse<Void> handleCategoryNotFoundException(CategoryNotFoundException exception)
    {
        return BaseResponse.<Void>builder()
                .message(exception.getMessage())
                .success(false)
                .status(HttpStatus.NOT_FOUND.value())
                .build();
    }

    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    @ExceptionHandler(value = BookNotFoundException.class)
    public BaseResponse<Void> handleBookNotFoundException(BookNotFoundException exception)
    {
        return BaseResponse.<Void>builder()
                .message(exception.getMessage())
                .success(false)
                .status(HttpStatus.NOT_FOUND.value())
                .build();
    }

    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    @ExceptionHandler(value = StudentNotFoundException.class)
    public BaseResponse<Void> handleStudentNotFoundException(StudentNotFoundException exception)
    {
        return BaseResponse.<Void>builder()
                .message(exception.getMessage())
                .success(false)
                .status(HttpStatus.NOT_FOUND.value())
                .build();
    }
    
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    @ExceptionHandler(value = OrderNotFoundException.class)
    public BaseResponse<Void> handleOrderNotFoundException(OrderNotFoundException exception)
    {
        return BaseResponse.<Void>builder()
                .message(exception.getMessage())
                .success(false)
                .status(HttpStatus.NOT_FOUND.value())
                .build();
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = FinCodeAlreadyExistsException.class)
    public BaseResponse<Void> handleFinCodeAlreadyExistsException(FinCodeAlreadyExistsException exception)
    {
        return BaseResponse.<Void>builder()
                .message(exception.getMessage())
                .success(false)
                .status(HttpStatus.BAD_REQUEST.value())
                .build();
    }

    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(value = AmazonS3Exception.class)
    public BaseResponse<Void> handleAmazonS3Exception(AmazonS3Exception exception)
    {
        return BaseResponse.<Void>builder()
                .message(exception.getMessage())
                .success(false)
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .build();
    }

    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    @ExceptionHandler(value = AdminUserNotFoundException.class)
    public BaseResponse<Void> handleAdminUserNotFoundException(AdminUserNotFoundException exception)
    {
        return BaseResponse.<Void>builder()
                .message(exception.getMessage())
                .success(false)
                .status(HttpStatus.NOT_FOUND.value())
                .build();
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = BookOutOfStockException.class)
    public BaseResponse<Void> handleBookOutOfStockException(BookOutOfStockException exception) 
    {
        return BaseResponse.<Void>builder()
                .message(exception.getMessage())
                .success(false)
                .status(HttpStatus.BAD_REQUEST.value())
                .build();
    }
}
