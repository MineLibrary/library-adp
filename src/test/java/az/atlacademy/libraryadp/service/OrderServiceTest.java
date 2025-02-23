package az.atlacademy.libraryadp.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import az.atlacademy.libraryadp.exception.BookNotFoundException;
import az.atlacademy.libraryadp.exception.BookOutOfStockException;
import az.atlacademy.libraryadp.exception.OrderNotFoundException;
import az.atlacademy.libraryadp.exception.StudentNotFoundException;
import az.atlacademy.libraryadp.mapper.OrderMapper;
import az.atlacademy.libraryadp.model.dto.request.OrderRequest;
import az.atlacademy.libraryadp.model.dto.response.BaseResponse;
import az.atlacademy.libraryadp.model.dto.response.BookResponse;
import az.atlacademy.libraryadp.model.dto.response.OrderResponse;
import az.atlacademy.libraryadp.model.dto.response.StudentResponse;
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

    @Test
    @DisplayName(value = "Testing createOrder() method when book and student exists and book stock is enough but days to return is equal to 0")
    public void givenCreateOrderWhenBookAndStudentExistsAndBookStockIsEnoughButDaysToReturnIsEqualToZeroThenReturnSuccessResponse()
    {
        int daysToReturn = 7; 
        LocalDateTime orderTime = LocalDateTime.now();
        LocalDateTime returnTime = orderTime.plusDays(daysToReturn); 
        MockedStatic<LocalDateTime> mockedLocalDateTime = Mockito.mockStatic(LocalDateTime.class);

        OrderRequest createOrderRequest = OrderRequest.builder()
            .bookId(1L)
            .studentId(1L)
            .daysToReturn(0)
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

    @Test
    @DisplayName(value = "Testing createOrder() method when book and student exists but book stock is not enough")
    public void givenCreateOrderWhenBookAndStudentExistsButBookStockIsNotEnoughThenThrowBookOutOfStockException()
    {
        LocalDateTime orderTime = LocalDateTime.now();
        MockedStatic<LocalDateTime> mockedLocalDateTime = Mockito.mockStatic(LocalDateTime.class);

        OrderRequest createOrderRequest = OrderRequest.builder()
            .bookId(1L)
            .studentId(1L)
            .daysToReturn(0)
            .build();

        BookEntity createOrderBookEntity = BookEntity.builder()
            .id(1L)
            .title("Tiny Pretty Things")
            .stock(0)
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

        Mockito.when(orderMapper.requestToEntity(createOrderRequest)).thenReturn(createOrderEntity);
        Mockito.when(bookService.getBookEntityById(1L)).thenReturn(createOrderBookEntity);
        Mockito.when(studentService.getStudentEntityById(1L)).thenReturn(createOrderStudentEntity);

        mockedLocalDateTime.when(LocalDateTime::now).thenReturn(orderTime);

        Mockito.when(bookService.updateBookStock(1L, -1))
            .thenThrow(new BookOutOfStockException("Book is out of stock."));

        Assertions.assertThrows(BookOutOfStockException.class, () -> orderService.createOrder(createOrderRequest));

        Mockito.verify(orderMapper, Mockito.times(1)).requestToEntity(createOrderRequest);
        Mockito.verify(bookService, Mockito.times(1)).getBookEntityById(1L);
        Mockito.verify(studentService, Mockito.times(1)).getStudentEntityById(1L);
        Mockito.verify(bookService, Mockito.times(1)).updateBookStock(1L, -1);
        Mockito.verifyNoMoreInteractions(bookService, studentService, orderRepository, orderMapper);
    
        mockedLocalDateTime.close();
    }

    @Test
    @DisplayName(value = "Testing createOrder() method when book exists but student doesn't")
    public void givenCreateOrderWhenBookExistsButStudentDoesntExistThenThrowStudentNotFoundException()
    {
        OrderRequest createOrderRequest = OrderRequest.builder()
            .bookId(1L)
            .studentId(1L)
            .daysToReturn(0)
            .build();

        BookEntity createOrderBookEntity = BookEntity.builder()
            .id(1L)
            .title("Tiny Pretty Things")
            .stock(0)
            .build();

        OrderEntity createOrderEntity = OrderEntity.builder().build(); 

        Mockito.when(orderMapper.requestToEntity(createOrderRequest)).thenReturn(createOrderEntity);
        Mockito.when(bookService.getBookEntityById(1L)).thenReturn(createOrderBookEntity);
        Mockito.when(studentService.getStudentEntityById(1L))
            .thenThrow(new StudentNotFoundException("Student not found with id : 1"));

        Assertions.assertThrows(StudentNotFoundException.class, () -> orderService.createOrder(createOrderRequest));

        Mockito.verify(orderMapper, Mockito.times(1)).requestToEntity(createOrderRequest);
        Mockito.verify(bookService, Mockito.times(1)).getBookEntityById(1L);
        Mockito.verify(studentService, Mockito.times(1)).getStudentEntityById(1L);
        Mockito.verifyNoMoreInteractions(bookService, studentService, orderRepository, orderMapper);
    }

    @Test
    @DisplayName(value = "Testing createOrder() method when book doesn't exist")
    public void givenCreateOrderWhenBookDoesNotExistThenThrowBookNotFoundException()
    {
        OrderRequest createOrderRequest = OrderRequest.builder()
            .bookId(1L)
            .studentId(1L)
            .daysToReturn(0)
            .build();

        OrderEntity createOrderEntity = OrderEntity.builder().build(); 

        Mockito.when(orderMapper.requestToEntity(createOrderRequest)).thenReturn(createOrderEntity);
        Mockito.when(bookService.getBookEntityById(1L))
            .thenThrow(new BookNotFoundException("Book not found with id : 1"));

        Assertions.assertThrows(BookNotFoundException.class, () -> orderService.createOrder(createOrderRequest));

        Mockito.verify(orderMapper, Mockito.times(1)).requestToEntity(createOrderRequest);
        Mockito.verify(bookService, Mockito.times(1)).getBookEntityById(1L);
        Mockito.verifyNoMoreInteractions(bookService, studentService, orderRepository, orderMapper);
    }

    @Test
    @DisplayName(value = "Testing getOrders() method")
    public void givenGetOrdersThenReturnBaseResponseOfListOfOrders()
    {
        int pageNumber = 0, pageSize = 1; 
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        int daysToReturn = 5; 
        LocalDateTime orderTime = LocalDateTime.now();
        LocalDateTime returnTime = orderTime.plusDays(daysToReturn); 

        BookEntity foundOrderBookEntity = BookEntity.builder()
            .id(1L)
            .title("Tiny Pretty Things")
            .stock(5)
            .build();

        BookResponse foundOrderBookResponse = BookResponse.builder()
            .id(1L)
            .title("Tiny Pretty Things")
            .stock(5)
            .build();

        StudentEntity foundOrderStudentEntity = StudentEntity.builder()
            .id(1L)
            .trustRate(100)
            .email("filankes@gmail.com")
            .finCode("5SFJ13D")
            .firstName("Filankes")
            .lastName("Filankesov")
            .phoneNumber("050 550 50 50")
            .build();

        StudentResponse foundOrderStudentResponse = StudentResponse.builder()
            .id(1L)
            .trustRate(100)
            .email("filankes@gmail.com")
            .finCode("5SFJ13D")
            .firstName("Filankes")
            .lastName("Filankesov")
            .phoneNumber("050 550 50 50")
            .build();

        List<OrderEntity> foundOrderEntities = List
            .of(
                OrderEntity.builder()
                    .id(1L)
                    .book(foundOrderBookEntity)
                    .student(foundOrderStudentEntity)
                    .orderTimestamp(orderTime)
                    .returnTimestamp(returnTime)
                    .build()
            );

        List<OrderResponse> foundOrderResponses = List
            .of(
                OrderResponse.builder()
                    .id(1L)
                    .book(foundOrderBookResponse)
                    .student(foundOrderStudentResponse)
                    .orderTimestamp(orderTime)
                    .returnTimestamp(returnTime)
                    .build()
            );

        Page<OrderEntity> orderPage = new PageImpl<>(foundOrderEntities);
        
        Mockito.when(orderRepository.findAll(pageable)).thenReturn(orderPage);
        for(int i = 0; i < pageSize; i++) 
        {
            Mockito.when(orderMapper.entityToResponse(foundOrderEntities.get(i)))
                .thenReturn(foundOrderResponses.get(i));
        }

        BaseResponse<List<OrderResponse>> serviceResponse = orderService.getOrders(pageNumber, pageSize);

        Mockito.verify(orderRepository, Mockito.times(1)).findAll(pageable); 
        for(int i = 0; i < pageSize; i++) 
        {
            Mockito.verify(orderMapper, Mockito.times(1))
                .entityToResponse(foundOrderEntities.get(i));
        }
        Mockito.verifyNoMoreInteractions(bookService, studentService, orderRepository, orderMapper);

        Assertions.assertEquals(HttpStatus.OK.value(), serviceResponse.getStatus());
        Assertions.assertEquals(true, serviceResponse.isSuccess());
        Assertions.assertEquals("Orders retrieved successfully.", serviceResponse.getMessage());
        Assertions.assertNotNull(serviceResponse.getData());
        Assertions.assertEquals(foundOrderResponses.size(), serviceResponse.getData().size());
        for(int i = 0; i < pageSize; i++) 
        {
            Assertions.assertEquals(foundOrderResponses.get(i).getBook(), serviceResponse.getData().get(i).getBook());
            Assertions.assertEquals(foundOrderResponses.get(i).getStudent(), serviceResponse.getData().get(i).getStudent());
            Assertions.assertEquals(foundOrderResponses.get(i).getOrderTimestamp(), serviceResponse.getData().get(i).getOrderTimestamp());
            Assertions.assertEquals(foundOrderResponses.get(i).getReturnTimestamp(), serviceResponse.getData().get(i).getReturnTimestamp());
        }
    }

    @Test
    @DisplayName(value = "Testing getOrderById() method when order exists")
    public void givenGetOrderByIdWhenOrderExistsThenReturnBaseResponseOfOrderResponse()
    {
        int daysToReturn = 5; 
        LocalDateTime orderTime = LocalDateTime.now();
        LocalDateTime returnTime = orderTime.plusDays(daysToReturn); 

        BookEntity foundOrderBookEntity = BookEntity.builder()
            .id(1L)
            .title("Tiny Pretty Things")
            .stock(5)
            .build();

        BookResponse foundOrderBookResponse = BookResponse.builder()
            .id(1L)
            .title("Tiny Pretty Things")
            .stock(5)
            .build();

        StudentEntity foundOrderStudentEntity = StudentEntity.builder()
            .id(1L)
            .trustRate(100)
            .email("filankes@gmail.com")
            .finCode("5SFJ13D")
            .firstName("Filankes")
            .lastName("Filankesov")
            .phoneNumber("050 550 50 50")
            .build();

        StudentResponse foundOrderStudentResponse = StudentResponse.builder()
            .id(1L)
            .trustRate(100)
            .email("filankes@gmail.com")
            .finCode("5SFJ13D")
            .firstName("Filankes")
            .lastName("Filankesov")
            .phoneNumber("050 550 50 50")
            .build();

        OrderEntity foundOrderEntity = OrderEntity.builder()
            .id(1L)
            .book(foundOrderBookEntity)
            .student(foundOrderStudentEntity)
            .orderTimestamp(orderTime)
            .returnTimestamp(returnTime)
            .build();
            

        OrderResponse foundOrderResponse = OrderResponse.builder()
            .id(1L)
            .book(foundOrderBookResponse)
            .student(foundOrderStudentResponse)
            .orderTimestamp(orderTime)
            .returnTimestamp(returnTime)
            .build();

        Mockito.when(orderRepository.findById(1L)).thenReturn(Optional.of(foundOrderEntity));
        Mockito.when(orderMapper.entityToResponse(foundOrderEntity)).thenReturn(foundOrderResponse);
            
        BaseResponse<OrderResponse> serviceResponse = orderService.getOrderById(1L);

        Mockito.verify(orderRepository, Mockito.times(1)).findById(1L);
        Mockito.verify(orderMapper, Mockito.times(1)).entityToResponse(foundOrderEntity);
        Mockito.verifyNoMoreInteractions(bookService, studentService, orderRepository, orderMapper);

        Assertions.assertEquals(HttpStatus.OK.value(), serviceResponse.getStatus());
        Assertions.assertEquals(true, serviceResponse.isSuccess());
        Assertions.assertEquals("Order retrieved successfully.", serviceResponse.getMessage());
        Assertions.assertNotNull(serviceResponse.getData());
        Assertions.assertEquals(foundOrderResponse.getId(), serviceResponse.getData().getId());
        Assertions.assertEquals(foundOrderResponse.getBook(), serviceResponse.getData().getBook());
        Assertions.assertEquals(foundOrderResponse.getStudent(), serviceResponse.getData().getStudent());
        Assertions.assertEquals(foundOrderResponse.getOrderTimestamp(), serviceResponse.getData().getOrderTimestamp());
        Assertions.assertEquals(foundOrderResponse.getReturnTimestamp(), serviceResponse.getData().getReturnTimestamp());
    }

    @Test
    @DisplayName(value = "Testing getOrderById() method when order does not exist")
    public void givenGetOrderByIdWhenOrderDoesNotExistThenThrowOrderNotFoundException()
    {
        Mockito.when(orderRepository.findById(1L)).thenReturn(Optional.empty());
        
        OrderNotFoundException exception = Assertions
            .assertThrows(OrderNotFoundException.class, () -> orderService.getOrderById(1L));

        Assertions.assertEquals("Order not found with id : 1", exception.getMessage());

        Mockito.verify(orderRepository, Mockito.times(1)).findById(1L);
        Mockito.verifyNoMoreInteractions(bookService, studentService, orderRepository, orderMapper);
    }

    @Test
    @DisplayName(value = "Testing getOrdersByStudentId() method when student exists")
    public void givenGetOrdersByStudentIdWhenStudentExistsThenReturnBaseResponseOfListOfOrders()
    {
        int pageNumber = 0, pageSize = 1; 
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        int daysToReturn = 5; 
        LocalDateTime orderTime = LocalDateTime.now();
        LocalDateTime returnTime = orderTime.plusDays(daysToReturn); 

        BookEntity foundOrderBookEntity = BookEntity.builder()
            .id(1L)
            .title("Tiny Pretty Things")
            .stock(5)
            .build();

        BookResponse foundOrderBookResponse = BookResponse.builder()
            .id(1L)
            .title("Tiny Pretty Things")
            .stock(5)
            .build();

        StudentEntity foundOrderStudentEntity = StudentEntity.builder()
            .id(1L)
            .trustRate(100)
            .email("filankes@gmail.com")
            .finCode("5SFJ13D")
            .firstName("Filankes")
            .lastName("Filankesov")
            .phoneNumber("050 550 50 50")
            .build();

        StudentResponse foundOrderStudentResponse = StudentResponse.builder()
            .id(1L)
            .trustRate(100)
            .email("filankes@gmail.com")
            .finCode("5SFJ13D")
            .firstName("Filankes")
            .lastName("Filankesov")
            .phoneNumber("050 550 50 50")
            .build();

        List<OrderEntity> foundOrderEntities = List
            .of(
                OrderEntity.builder()
                    .id(1L)
                    .book(foundOrderBookEntity)
                    .student(foundOrderStudentEntity)
                    .orderTimestamp(orderTime)
                    .returnTimestamp(returnTime)
                    .build()
            );

        List<OrderResponse> foundOrderResponses = List
            .of(
                OrderResponse.builder()
                    .id(1L)
                    .book(foundOrderBookResponse)
                    .student(foundOrderStudentResponse)
                    .orderTimestamp(orderTime)
                    .returnTimestamp(returnTime)
                    .build()
            );

        Page<OrderEntity> orderPage = new PageImpl<>(foundOrderEntities);

        Mockito.when(studentService.getStudentEntityById(1L)).thenReturn(foundOrderStudentEntity);
        Mockito.when(orderRepository.findByStudent(foundOrderStudentEntity, pageable)).thenReturn(orderPage);
        for(int i = 0; i < pageSize; i++) 
        {
            Mockito.when(orderMapper.entityToResponse(foundOrderEntities.get(i)))
                .thenReturn(foundOrderResponses.get(i));
        }

        BaseResponse<List<OrderResponse>> serviceResponse = orderService.getOrdersByStudentId(1L, pageNumber, pageSize);

        Mockito.verify(studentService, Mockito.times(1)).getStudentEntityById(1L);
        Mockito.verify(orderRepository, Mockito.times(1)).findByStudent(foundOrderStudentEntity, pageable);
        Mockito.verify(orderMapper, Mockito.times(pageSize)).entityToResponse(foundOrderEntities.get(0));
        Mockito.verifyNoMoreInteractions(bookService, studentService, orderRepository, orderMapper);

        Assertions.assertEquals(HttpStatus.OK.value(), serviceResponse.getStatus());
        Assertions.assertEquals(true, serviceResponse.isSuccess());
        Assertions.assertEquals("Orders retrieved successfully.", serviceResponse.getMessage());
        Assertions.assertNotNull(serviceResponse.getData());
        Assertions.assertEquals(foundOrderResponses.size(), serviceResponse.getData().size());
        for(int i = 0; i < pageSize; i++) 
        {
            Assertions.assertEquals(foundOrderResponses.get(i).getBook(), serviceResponse.getData().get(i).getBook());
            Assertions.assertEquals(foundOrderResponses.get(i).getStudent(), serviceResponse.getData().get(i).getStudent());
            Assertions.assertEquals(foundOrderResponses.get(i).getOrderTimestamp(), serviceResponse.getData().get(i).getOrderTimestamp());
            Assertions.assertEquals(foundOrderResponses.get(i).getReturnTimestamp(), serviceResponse.getData().get(i).getReturnTimestamp());
        }
    }

    @Test
    @DisplayName(value = "Testing getOrdersByStudentId() method when student does not exist")
    public void givenGetOrdersByStudentIdWhenStudentDoesNotExistThenThrowStudentNotFoundException()
    {
        Mockito.when(studentService.getStudentEntityById(1L))
            .thenThrow(new StudentNotFoundException("Student not found with id : 1"));
        
        Assertions
            .assertThrows(
                StudentNotFoundException.class, 
                () -> orderService.getOrdersByStudentId(1L, 0, 1)
            );
        
        Mockito.verify(studentService, Mockito.times(1)).getStudentEntityById(1L); 
        Mockito.verifyNoMoreInteractions(bookService, studentService, orderRepository, orderMapper);
    }
    
    @Test
    @DisplayName(value = "Testing getOrdersByBookId() method when book exists")
    public void givenGetOrdersByBookIdWhenBookExistsThenReturnBaseResponseOfListOfOrders()
    {
        int pageNumber = 0, pageSize = 1; 
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        int daysToReturn = 5; 
        LocalDateTime orderTime = LocalDateTime.now();
        LocalDateTime returnTime = orderTime.plusDays(daysToReturn); 

        BookEntity foundOrderBookEntity = BookEntity.builder()
            .id(1L)
            .title("Tiny Pretty Things")
            .stock(5)
            .build();

        BookResponse foundOrderBookResponse = BookResponse.builder()
            .id(1L)
            .title("Tiny Pretty Things")
            .stock(5)
            .build();

        StudentEntity foundOrderStudentEntity = StudentEntity.builder()
            .id(1L)
            .trustRate(100)
            .email("filankes@gmail.com")
            .finCode("5SFJ13D")
            .firstName("Filankes")
            .lastName("Filankesov")
            .phoneNumber("050 550 50 50")
            .build();

        StudentResponse foundOrderStudentResponse = StudentResponse.builder()
            .id(1L)
            .trustRate(100)
            .email("filankes@gmail.com")
            .finCode("5SFJ13D")
            .firstName("Filankes")
            .lastName("Filankesov")
            .phoneNumber("050 550 50 50")
            .build();

        List<OrderEntity> foundOrderEntities = List
            .of(
                OrderEntity.builder()
                    .id(1L)
                    .book(foundOrderBookEntity)
                    .student(foundOrderStudentEntity)
                    .orderTimestamp(orderTime)
                    .returnTimestamp(returnTime)
                    .build()
            );

        List<OrderResponse> foundOrderResponses = List
            .of(
                OrderResponse.builder()
                    .id(1L)
                    .book(foundOrderBookResponse)
                    .student(foundOrderStudentResponse)
                    .orderTimestamp(orderTime)
                    .returnTimestamp(returnTime)
                    .build()
            );

        Page<OrderEntity> orderPage = new PageImpl<>(foundOrderEntities);

        Mockito.when(bookService.getBookEntityById(1L)).thenReturn(foundOrderBookEntity);
        Mockito.when(orderRepository.findByBook(foundOrderBookEntity, pageable)).thenReturn(orderPage);
        for(int i = 0; i < pageSize; i++) 
        {
            Mockito.when(orderMapper.entityToResponse(foundOrderEntities.get(i)))
                .thenReturn(foundOrderResponses.get(i));
        }

        BaseResponse<List<OrderResponse>> serviceResponse = orderService.getOrdersByBookId(1L, pageNumber, pageSize);

        Mockito.verify(bookService, Mockito.times(1)).getBookEntityById(1L);
        Mockito.verify(orderRepository, Mockito.times(1)).findByBook(foundOrderBookEntity, pageable);
        Mockito.verify(orderMapper, Mockito.times(pageSize)).entityToResponse(foundOrderEntities.get(0));
        Mockito.verifyNoMoreInteractions(bookService, studentService, orderRepository, orderMapper);

        Assertions.assertEquals(HttpStatus.OK.value(), serviceResponse.getStatus());
        Assertions.assertEquals(true, serviceResponse.isSuccess());
        Assertions.assertEquals("Orders retrieved successfully.", serviceResponse.getMessage());
        Assertions.assertNotNull(serviceResponse.getData());
        Assertions.assertEquals(foundOrderResponses.size(), serviceResponse.getData().size());
        for(int i = 0; i < pageSize; i++) 
        {
            Assertions.assertEquals(foundOrderResponses.get(i).getBook(), serviceResponse.getData().get(i).getBook());
            Assertions.assertEquals(foundOrderResponses.get(i).getStudent(), serviceResponse.getData().get(i).getStudent());
            Assertions.assertEquals(foundOrderResponses.get(i).getOrderTimestamp(), serviceResponse.getData().get(i).getOrderTimestamp());
            Assertions.assertEquals(foundOrderResponses.get(i).getReturnTimestamp(), serviceResponse.getData().get(i).getReturnTimestamp());
        }
    }

    @Test
    @DisplayName(value = "Testing getOrdersByBookId() method when book does not exist")
    public void givenGetOrdersByBookIdWhenBookDoesNotExistThenThrowBookNotFoundException()
    {
        Mockito.when(bookService.getBookEntityById(1L))
            .thenThrow(new BookNotFoundException("Book not found with id : 1"));
        
        Assertions
            .assertThrows(
                BookNotFoundException.class, 
                () -> orderService.getOrdersByBookId(1L, 0, 1)
            );
        
        Mockito.verify(bookService, Mockito.times(1)).getBookEntityById(1L); 
        Mockito.verifyNoMoreInteractions(bookService, studentService, orderRepository, orderMapper);
    }
    
    @Test
    @DisplayName(value = "Testing updateOrder() method when order, book, and student exist")
    public void givenUpdateOrderWhenOrderAndBookAndStudentExistsThenReturnSuccessResponse()
    {
        int daysToReturn = 5, updatedDaysToReturn = 6; 
        LocalDateTime orderTime = LocalDateTime.of(2025, 2, 23, 12, 9, 31);
        LocalDateTime returnTime = orderTime.plusDays(daysToReturn); 
        LocalDateTime updatedReturnTime = orderTime.plusDays(updatedDaysToReturn);

        OrderRequest updateOrderRequest = OrderRequest.builder()
            .bookId(1L)
            .studentId(1L)
            .daysToReturn(updatedDaysToReturn)
            .build();

        BookEntity foundOrderBookEntity = BookEntity.builder()
            .id(1L)
            .title("Tiny Pretty Things")
            .stock(5)
            .build();

        StudentEntity foundOrderStudentEntity = StudentEntity.builder()
            .id(1L)
            .trustRate(100)
            .email("filankes@gmail.com")
            .finCode("5SFJ13D")
            .firstName("Filankes")
            .lastName("Filankesov")
            .phoneNumber("050 550 50 50")
            .build();

        OrderEntity foundOrderEntity = OrderEntity.builder()
            .id(1L)
            .book(foundOrderBookEntity)
            .student(foundOrderStudentEntity)
            .orderTimestamp(orderTime)
            .returnTimestamp(returnTime)
            .build(); 
            
        OrderEntity updatedOrderEntity = OrderEntity.builder()
            .id(1L)
            .book(foundOrderBookEntity)
            .student(foundOrderStudentEntity)
            .orderTimestamp(orderTime)
            .returnTimestamp(updatedReturnTime)
            .build(); 

        Mockito.when(orderRepository.findById(1L)).thenReturn(Optional.of(foundOrderEntity));
        Mockito.when(bookService.getBookEntityById(1L)).thenReturn(foundOrderBookEntity);
        Mockito.when(studentService.getStudentEntityById(1L)).thenReturn(foundOrderStudentEntity);
        Mockito.when(orderRepository.save(updatedOrderEntity)).thenReturn(updatedOrderEntity);

        BaseResponse<Void> serviceResponse = orderService.updateOrder(1L, updateOrderRequest);

        Mockito.verify(orderRepository, Mockito.times(1)).findById(1L);
        Mockito.verify(orderMapper, Mockito.times(1)).convertRequestToEntity(updateOrderRequest, foundOrderEntity);
        Mockito.verify(bookService, Mockito.times(1)).getBookEntityById(1L);
        Mockito.verify(studentService, Mockito.times(1)).getStudentEntityById(1L);
        Mockito.verify(orderRepository, Mockito.times(1)).save(updatedOrderEntity);
        Mockito.verifyNoMoreInteractions(bookService, studentService, orderMapper, orderRepository);

        Assertions.assertEquals(HttpStatus.OK.value(), serviceResponse.getStatus());
        Assertions.assertEquals(true, serviceResponse.isSuccess());
        Assertions.assertEquals("Order updated successfully.", serviceResponse.getMessage());
        Assertions.assertNull(serviceResponse.getData());
    }

    @Test
    @DisplayName(value = "Testing updateOrder() method when order, book exist but student doesn't")
    public void givenUpdateOrderWhenOrderAndBookExistsButStudentDoesNotExistsThenThrowStudentNotFoundException()
    {
        int daysToReturn = 5, updatedDaysToReturn = 6; 
        LocalDateTime orderTime = LocalDateTime.of(2025, 2, 23, 12, 9, 31);
        LocalDateTime returnTime = orderTime.plusDays(daysToReturn); 

        OrderRequest updateOrderRequest = OrderRequest.builder()
            .bookId(1L)
            .studentId(2L)
            .daysToReturn(updatedDaysToReturn)
            .build();

        BookEntity foundOrderBookEntity = BookEntity.builder()
            .id(1L)
            .title("Tiny Pretty Things")
            .stock(5)
            .build();

        StudentEntity foundOrderStudentEntity = StudentEntity.builder()
            .id(1L)
            .trustRate(100)
            .email("filankes@gmail.com")
            .finCode("5SFJ13D")
            .firstName("Filankes")
            .lastName("Filankesov")
            .phoneNumber("050 550 50 50")
            .build();

        OrderEntity foundOrderEntity = OrderEntity.builder()
            .id(1L)
            .book(foundOrderBookEntity)
            .student(foundOrderStudentEntity)
            .orderTimestamp(orderTime)
            .returnTimestamp(returnTime)
            .build(); 

        Mockito.when(orderRepository.findById(1L)).thenReturn(Optional.of(foundOrderEntity));
        Mockito.when(bookService.getBookEntityById(1L)).thenReturn(foundOrderBookEntity);
        Mockito.when(studentService.getStudentEntityById(2L))
            .thenThrow(new StudentNotFoundException("Student not found with id : 2"));

        Assertions
            .assertThrows(
                StudentNotFoundException.class,
                () -> orderService.updateOrder(1L, updateOrderRequest)
            );
        
        Mockito.verify(orderRepository, Mockito.times(1)).findById(1L);
        Mockito.verify(orderMapper, Mockito.times(1)).convertRequestToEntity(updateOrderRequest, foundOrderEntity);
        Mockito.verify(bookService, Mockito.times(1)).getBookEntityById(1L);
        Mockito.verify(studentService, Mockito.times(1)).getStudentEntityById(2L);
        Mockito.verifyNoMoreInteractions(bookService, studentService, orderMapper, orderRepository);
    }

    @Test
    @DisplayName(value = "Testing updateOrder() method when order, student exist but book doesn't")
    public void givenUpdateOrderWhenOrderAndStudentExistsButBookDoesNotExistsThenThrowBookNotFoundException()
    {
        int daysToReturn = 5, updatedDaysToReturn = 6; 
        LocalDateTime orderTime = LocalDateTime.of(2025, 2, 23, 12, 9, 31);
        LocalDateTime returnTime = orderTime.plusDays(daysToReturn);

        OrderRequest updateOrderRequest = OrderRequest.builder()
            .bookId(2L)
            .studentId(1L)
            .daysToReturn(updatedDaysToReturn)
            .build();

        BookEntity foundOrderBookEntity = BookEntity.builder()
            .id(1L)
            .title("Tiny Pretty Things")
            .stock(5)
            .build();

        StudentEntity foundOrderStudentEntity = StudentEntity.builder()
            .id(1L)
            .trustRate(100)
            .email("filankes@gmail.com")
            .finCode("5SFJ13D")
            .firstName("Filankes")
            .lastName("Filankesov")
            .phoneNumber("050 550 50 50")
            .build();

        OrderEntity foundOrderEntity = OrderEntity.builder()
            .id(1L)
            .book(foundOrderBookEntity)
            .student(foundOrderStudentEntity)
            .orderTimestamp(orderTime)
            .returnTimestamp(returnTime)
            .build();

        Mockito.when(orderRepository.findById(1L)).thenReturn(Optional.of(foundOrderEntity));
        Mockito.when(bookService.getBookEntityById(2L))
            .thenThrow(new BookNotFoundException("Book not found with id : 1"));

        Assertions
           .assertThrows(
                BookNotFoundException.class,
                () -> orderService.updateOrder(1L, updateOrderRequest)
            );
        
        Mockito.verify(orderRepository, Mockito.times(1)).findById(1L);
        Mockito.verify(orderMapper, Mockito.times(1)).convertRequestToEntity(updateOrderRequest, foundOrderEntity);
        Mockito.verify(bookService, Mockito.times(1)).getBookEntityById(2L);
        Mockito.verifyNoMoreInteractions(bookService, studentService, orderMapper, orderRepository);
    }

    @Test
    @DisplayName(value = "Testing updateOrder() method when order doesn't exist")
    public void givenUpdateOrderWhenOrderDoesNotExistThenThrowOrderNotFoundException()
    {
        OrderRequest updateOrderRequest = OrderRequest.builder()
            .bookId(1L)
            .studentId(1L)
            .daysToReturn(6)
            .build();

        Mockito.when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        OrderNotFoundException exception = Assertions
           .assertThrows(
                OrderNotFoundException.class,
                () -> orderService.updateOrder(1L, updateOrderRequest)
            );

        Assertions.assertEquals("Order not found with id : 1", exception.getMessage());
        
        Mockito.verify(orderRepository, Mockito.times(1)).findById(1L);
        Mockito.verifyNoMoreInteractions(bookService, studentService, orderMapper, orderRepository);
    }

    @Test
    @DisplayName(value = "Testing deleteOrder() method")
    public void givenDeleteOrderThenReturnSuccessResponse()
    {
        BaseResponse<Void> serviceResponse = orderService.deleteOrder(1L);

        Mockito.verify(orderRepository, Mockito.times(1)).deleteById(1L);
        Mockito.verifyNoMoreInteractions(bookService, studentService, orderMapper, orderRepository);

        Assertions.assertEquals(HttpStatus.OK.value(), serviceResponse.getStatus());
        Assertions.assertEquals(true, serviceResponse.isSuccess());
        Assertions.assertEquals("Order deleted successfully.", serviceResponse.getMessage());
        Assertions.assertNull(serviceResponse.getData());
    }

    @Test
    @DisplayName(value = "Testing returnOrderBook() method when order exists and time is before return time")
    public void givenReturnOrderBookWhenOrderExistsAndTimeIsBeforeReturnTimeThenReturnSuccessResponse()
    {
        int daysToReturn = 5; 
        LocalDateTime orderTime = LocalDateTime.of(2025, 2, 5, 8, 9, 10);
        LocalDateTime returnTimeExpected = orderTime.plusDays(daysToReturn); 
        LocalDateTime returnTime = orderTime.plusDays(daysToReturn - 1);
        MockedStatic<LocalDateTime> mockedLocalDateTime = Mockito.mockStatic(LocalDateTime.class);

        BookEntity foundOrderBookEntity = BookEntity.builder()
            .id(1L)
            .title("Tiny Pretty Things")
            .stock(5)
            .build();

        StudentEntity foundOrderStudentEntity = StudentEntity.builder()
            .id(1L)
            .trustRate(100)
            .email("filankes@gmail.com")
            .finCode("5SFJ13D")
            .firstName("Filankes")
            .lastName("Filankesov")
            .phoneNumber("050 550 50 50")
            .build();

        OrderEntity foundOrderEntity = OrderEntity.builder()
            .id(1L)
            .book(foundOrderBookEntity)
            .student(foundOrderStudentEntity)
            .orderTimestamp(orderTime)
            .returnTimestamp(returnTimeExpected)
            .build(); 

        Mockito.when(orderRepository.findById(1L)).thenReturn(Optional.of(foundOrderEntity));
        Mockito.when(bookService.updateBookStock(foundOrderBookEntity.getId(), foundOrderBookEntity.getStock() + 1))
            .thenReturn(
                BaseResponse.<Void>builder()
                    .status(HttpStatus.OK.value())
                    .success(true)
                    .message("Book stock updated successfully.")
                    .build()
            );

        mockedLocalDateTime.when(LocalDateTime::now).thenReturn(returnTime);

        Mockito
            .when(
                studentService.updateStudentTrustRate(foundOrderStudentEntity.getId(), 
                foundOrderStudentEntity.getTrustRate() + 10)
            )
            .thenReturn(
                BaseResponse.<Void>builder()
                    .success(true)
                    .message("Trust rate updated successfully.")
                    .status(HttpStatus.OK.value())
                    .build()
            );

        BaseResponse<Void> serviceResponse = orderService.returnOrderBook(1L);

        Mockito.verify(orderRepository, Mockito.times(1)).findById(1L);
        Mockito.verify(bookService, Mockito.times(1)).updateBookStock(1L, 6);
        Mockito.verify(studentService, Mockito.times(1)).updateStudentTrustRate(1L, 110);
        Mockito.verify(orderRepository, Mockito.times(1)).deleteById(1L);
        Mockito.verifyNoMoreInteractions(bookService, studentService, orderMapper, orderRepository);

        Assertions.assertEquals(HttpStatus.OK.value(), serviceResponse.getStatus());
        Assertions.assertEquals(true, serviceResponse.isSuccess());
        Assertions.assertEquals("Order book returned successfully.", serviceResponse.getMessage());
        Assertions.assertNull(serviceResponse.getData());

        mockedLocalDateTime.close();
    }

    @Test
    @DisplayName(value = "Testing returnOrderBook() method when order exists and time is after return time")
    public void givenReturnOrderBookWhenOrderExistsAndTimeIsAfterReturnTimeThenReturnSuccessResponse()
    {
        int daysToReturn = 5; 
        LocalDateTime orderTime = LocalDateTime.of(2025, 2, 5, 8, 9, 10);
        LocalDateTime returnTimeExpected = orderTime.plusDays(daysToReturn); 
        LocalDateTime returnTime = orderTime.plusDays(daysToReturn + 1);
        MockedStatic<LocalDateTime> mockedLocalDateTime = Mockito.mockStatic(LocalDateTime.class);

        BookEntity foundOrderBookEntity = BookEntity.builder()
            .id(1L)
            .title("Tiny Pretty Things")
            .stock(5)
            .build();

        StudentEntity foundOrderStudentEntity = StudentEntity.builder()
            .id(1L)
            .trustRate(100)
            .email("filankes@gmail.com")
            .finCode("5SFJ13D")
            .firstName("Filankes")
            .lastName("Filankesov")
            .phoneNumber("050 550 50 50")
            .build();

        OrderEntity foundOrderEntity = OrderEntity.builder()
            .id(1L)
            .book(foundOrderBookEntity)
            .student(foundOrderStudentEntity)
            .orderTimestamp(orderTime)
            .returnTimestamp(returnTimeExpected)
            .build(); 

        Mockito.when(orderRepository.findById(1L)).thenReturn(Optional.of(foundOrderEntity));
        Mockito.when(bookService.updateBookStock(foundOrderBookEntity.getId(), foundOrderBookEntity.getStock() + 1))
            .thenReturn(
                BaseResponse.<Void>builder()
                    .status(HttpStatus.OK.value())
                    .success(true)
                    .message("Book stock updated successfully.")
                    .build()
            );

        mockedLocalDateTime.when(LocalDateTime::now).thenReturn(returnTime);

        Mockito
            .when(
                studentService.updateStudentTrustRate(foundOrderStudentEntity.getId(), 
                foundOrderStudentEntity.getTrustRate() - 10)
            )
            .thenReturn(
                BaseResponse.<Void>builder()
                    .success(true)
                    .message("Trust rate updated successfully.")
                    .status(HttpStatus.OK.value())
                    .build()
            );

        BaseResponse<Void> serviceResponse = orderService.returnOrderBook(1L);

        Mockito.verify(orderRepository, Mockito.times(1)).findById(1L);
        Mockito.verify(bookService, Mockito.times(1)).updateBookStock(1L, 6);
        Mockito.verify(studentService, Mockito.times(1)).updateStudentTrustRate(1L, 90);
        Mockito.verify(orderRepository, Mockito.times(1)).deleteById(1L);
        Mockito.verifyNoMoreInteractions(bookService, studentService, orderMapper, orderRepository);

        Assertions.assertEquals(HttpStatus.OK.value(), serviceResponse.getStatus());
        Assertions.assertEquals(true, serviceResponse.isSuccess());
        Assertions.assertEquals("Order book returned successfully.", serviceResponse.getMessage());
        Assertions.assertNull(serviceResponse.getData());

        mockedLocalDateTime.close();
    }

    @Test
    @DisplayName(value = "Testing returnOrderBook() method when order does not exist")
    public void givenReturnOrderBookWhenOrderDoesNotExistThenThrowOrderNotFoundException()
    {
        Mockito.when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        OrderNotFoundException exception = Assertions
           .assertThrows(
                OrderNotFoundException.class,
                () -> orderService.returnOrderBook(1L)
            );

        Assertions.assertEquals("Order not found with id : 1", exception.getMessage());
        
        Mockito.verify(orderRepository, Mockito.times(1)).findById(1L);
        Mockito.verifyNoMoreInteractions(bookService, studentService, orderMapper, orderRepository);
    }
}