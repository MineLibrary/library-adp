package az.atlacademy.libraryadp.service;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import az.atlacademy.libraryadp.mapper.OrderMapper;
import az.atlacademy.libraryadp.model.dto.request.OrderRequest;
import az.atlacademy.libraryadp.model.dto.response.BaseResponse;
import az.atlacademy.libraryadp.model.entity.BookEntity;
import az.atlacademy.libraryadp.model.entity.OrderEntity;
import az.atlacademy.libraryadp.model.entity.StudentEntity;
import az.atlacademy.libraryadp.repository.OrderRepository;

@ExtendWith(value = MockitoExtension.class)
public class OrderServiceTest 
{
    @InjectMocks
    private OrderService orderService;

    @Mock
    private BookService bookService;

    @Mock
    private StudentService studentService;

    @Mock
    private OrderRepository orderRepository; 

    @Mock
    private OrderMapper orderMapper; 

    @Test
    @DisplayName(value = "Testing createOrder() method when book and student exists and book stock is enough and days to return is greater than 0")
    public void givenCreateOrderWhenBookAndStudentExistsAndBookStockIsEnoughAndDaysToReturnIsGreaterThanZeroThenReturnSuccessResponse()
    {
        int daysToReturn = 5; 
        LocalDateTime orderTime = LocalDateTime.now();
        LocalDateTime returnTime = orderTime.plusDays(daysToReturn); 
        MockedStatic<LocalDateTime> mockedLocalDateTime = Mockito.mockStatic(LocalDateTime.class);

        OrderRequest createOrderRequest = OrderRequest.builder()
            .bookId(1L)
            .studentId(1L)
            .daysToReturn(daysToReturn)
            .build();

        BookEntity createOrderBookEntity = BookEntity.builder()
            .id(1L)
            .title("Tiny Pretty Things")
            .stock(5)
            .build();

        StudentEntity createOrderStudentEntity = StudentEntity.builder()
            .id(1L)
            .trustRate(100)
            .email("filankes@gmail.com")
            .finCode("5SFJ13D")
            .firstName("Filankes")
            .lastName("Filankesov")
            .phoneNumber("050 550 50 50")
            .build();

        OrderEntity createOrderEntity = OrderEntity.builder().build(); 

        OrderEntity createOrderEntityWithRelations = OrderEntity.builder()
            .book(createOrderBookEntity)
            .student(createOrderStudentEntity)
            .orderTimestamp(orderTime)
            .returnTimestamp(returnTime)
            .build(); 

        OrderEntity createdOrderEntity = OrderEntity.builder()
            .id(1L)
            .book(createOrderBookEntity)
            .student(createOrderStudentEntity)
            .orderTimestamp(orderTime)
            .returnTimestamp(returnTime)
            .build(); 

        Mockito.when(orderMapper.requestToEntity(createOrderRequest)).thenReturn(createOrderEntity);
        Mockito.when(bookService.getBookEntityById(1L)).thenReturn(createOrderBookEntity);
        Mockito.when(studentService.getStudentEntityById(1L)).thenReturn(createOrderStudentEntity);

        mockedLocalDateTime.when(LocalDateTime::now).thenReturn(orderTime);

        Mockito.when(bookService.updateBookStock(1L, 4))
            .thenReturn(
                BaseResponse.<Void>builder()
                    .status(HttpStatus.OK.value())
                    .success(true)
                    .message("Book stock updated successfully.")
                    .build()
            );

        Mockito.when(orderRepository.save(createOrderEntityWithRelations)).thenReturn(createdOrderEntity);

        BaseResponse<Void> serviceResponse = orderService.createOrder(createOrderRequest);

        Mockito.verify(orderMapper, Mockito.times(1)).requestToEntity(createOrderRequest);
        Mockito.verify(bookService, Mockito.times(1)).getBookEntityById(1L);
        Mockito.verify(studentService, Mockito.times(1)).getStudentEntityById(1L);
        Mockito.verify(bookService, Mockito.times(1)).updateBookStock(1L, 4);
        Mockito.verify(orderRepository, Mockito.times(1)).save(createOrderEntityWithRelations);
        Mockito.verifyNoMoreInteractions(bookService, studentService, orderRepository, orderMapper);
        
        Assertions.assertEquals(HttpStatus.CREATED.value(), serviceResponse.getStatus());
        Assertions.assertEquals("Order created successfully.", serviceResponse.getMessage());
        Assertions.assertTrue(serviceResponse.isSuccess());
        Assertions.assertNull(serviceResponse.getData());

        mockedLocalDateTime.close();
    }
}
