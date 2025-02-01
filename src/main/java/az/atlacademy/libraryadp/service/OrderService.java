package az.atlacademy.libraryadp.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import az.atlacademy.libraryadp.exception.OrderNotFoundException;
import az.atlacademy.libraryadp.mapper.OrderMapper;
import az.atlacademy.libraryadp.model.dto.request.OrderRequest;
import az.atlacademy.libraryadp.model.dto.response.BaseResponse;
import az.atlacademy.libraryadp.model.dto.response.OrderResponse;
import az.atlacademy.libraryadp.model.entity.OrderEntity;
import az.atlacademy.libraryadp.repository.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService 
{
    private final OrderRepository orderRepository; 
    private final OrderMapper orderMapper; 
    private final BookService bookService; 
    private final StudentService studentService;

    @Transactional
    public BaseResponse<Void> createOrder(OrderRequest orderRequest)
    {
        OrderEntity orderEntity = orderMapper.requestToEntity(orderRequest); 

        orderEntity.setBook(bookService.getBookEntityById(orderRequest.getBookId()));
        orderEntity.setStudent(studentService.getStudentEntityById(orderRequest.getStudentId()));
        orderEntity.setOrderTimestamp(LocalDateTime.now());

        int daysToReturn;
        if ((daysToReturn = orderRequest.getDaysToReturn()) <= 0) 
        {
            daysToReturn = 7;
        }

        orderEntity.setReturnTimestamp(orderEntity.getOrderTimestamp().plusDays(daysToReturn));

        bookService.updateBookStock(orderEntity.getBook().getId(), orderEntity.getBook().getStock() - 1);
        orderRepository.save(orderEntity);

        log.info("Created new order for book: {}, student: {}", orderRequest.getBookId(), orderRequest.getStudentId());

        return BaseResponse.<Void>builder()
               .success(true)
               .status(HttpStatus.CREATED.value()) 
               .message("Order created successfully.")
               .build();
    }

    public BaseResponse<List<OrderResponse>> getOrders(int pageNumber, int pageSize)
    {
        Pageable pageable = PageRequest.of(pageNumber, pageSize); 
        Page<OrderEntity> orderPage = orderRepository.findAll(pageable);

        List<OrderEntity> orderEntities = orderPage.getContent();
        
        List<OrderResponse> orderResponses = orderEntities.stream()
            .map(orderMapper::entityToResponse).collect(Collectors.toList()); 

        log.info("Retrieved all orders (page: {}, size: {})", pageNumber, pageSize);

        return BaseResponse.<List<OrderResponse>>builder()
                .success(true)
                .data(orderResponses)
                .message("Orders retrieved successfully.")
                .status(HttpStatus.OK.value())
                .build();
    }

    public BaseResponse<OrderResponse> getOrderById(long orderId)
    {
        OrderEntity orderEntity = orderRepository.findById(orderId)
           .orElseThrow(() -> new OrderNotFoundException("Order not found with id : " + orderId));

        OrderResponse orderResponse = orderMapper.entityToResponse(orderEntity);

        log.info("Retrieved order with id: {}", orderId);

        return BaseResponse.<OrderResponse>builder()
                .success(true)
                .data(orderResponse)
                .message("Order retrieved successfully.")
                .status(HttpStatus.OK.value())
                .build();
    }

    public BaseResponse<List<OrderResponse>> getOrdersByStudentId(
        long studentId, int pageNumber, int pageSize
    ){
        Pageable pageable = PageRequest.of(pageNumber, pageSize); 
        Page<OrderEntity> orderPage = orderRepository
            .findByStudent(studentService.getStudentEntityById(studentId), pageable);

        List<OrderEntity> orderEntities = orderPage.getContent(); 
        
        List<OrderResponse> orderResponses = orderEntities.stream()
            .map(orderMapper::entityToResponse).collect(Collectors.toList());

        log.info("Retrieved orders by student with id: {} (page: {}, size: {})", studentId, pageNumber, pageSize);

        return BaseResponse.<List<OrderResponse>>builder()
                .success(true)
                .data(orderResponses)
                .message("Orders retrieved successfully.")
                .status(HttpStatus.OK.value())
                .build();
    }

    public BaseResponse<List<OrderResponse>> getOrdersByBookId(
        long bookId, int pageNumber, int pageSize
    ){
        Pageable pageable = PageRequest.of(pageNumber, pageSize); 
        Page<OrderEntity> orderPage = orderRepository
            .findByBook(bookService.getBookEntityById(bookId), pageable);

        List<OrderEntity> orderEntities = orderPage.getContent(); 
        
        List<OrderResponse> orderResponses = orderEntities.stream()
            .map(orderMapper::entityToResponse).collect(Collectors.toList());

        log.info("Retrieved orders by book with id: {} (page: {}, size: {})", bookId, pageNumber, pageSize);

        return BaseResponse.<List<OrderResponse>>builder()
                .success(true)
                .data(orderResponses)
                .message("Orders retrieved successfully.")
                .status(HttpStatus.OK.value())
                .build();
    }
    
    @Transactional
    public BaseResponse<Void> updateOrder(long orderId, OrderRequest orderRequest)
    {
        OrderEntity orderEntity = orderRepository.findById(orderId)
           .orElseThrow(() -> new OrderNotFoundException("Order not found with id : " + orderId));

        orderMapper.convertRequestToEntity(orderRequest, orderEntity);

        if (orderRequest.getDaysToReturn() > 0)
        {
            orderEntity.setReturnTimestamp(
                orderEntity.getOrderTimestamp().plusDays(orderRequest.getDaysToReturn())
            );
        }

        orderRepository.save(orderEntity);

        log.info("Updated order with id: {}", orderId);

        return BaseResponse.<Void>builder()
                .success(true)
                .message("Order updated successfully.")
                .status(HttpStatus.OK.value())
                .build();
    }

    @Transactional
    public BaseResponse<Void> deleteOrder(long orderId)
    {
        orderRepository.deleteById(orderId);

        log.info("Deleted order with id: {}", orderId);

        return BaseResponse.<Void>builder()
                .success(true)
                .message("Order deleted successfully.")
                .status(HttpStatus.OK.value())
                .build();
    }

    @Transactional
    public BaseResponse<Void> returnOrderBook(long orderId)
    {
        OrderEntity orderEntity = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException("Order not found with id : " + orderId));

        bookService.updateBookStock(orderEntity.getBook().getId(), orderEntity.getBook().getStock() + 1);

        LocalDateTime returnTime = LocalDateTime.now(); 

        if (returnTime.isAfter(orderEntity.getReturnTimestamp())) 
        {
            studentService
                .updateStudentTrustRate(orderEntity.getStudent().getId(), orderEntity.getStudent().getTrustRate() - 10);
        }
        else 
        {
            studentService
                .updateStudentTrustRate(orderEntity.getStudent().getId(), orderEntity.getStudent().getTrustRate() + 10);
        }
        
        orderRepository.deleteById(orderId);

        log.info("Deleted order with id: {}", orderId);

        return BaseResponse.<Void>builder()
                .success(true)
                .message("Order deleted successfully.")
                .status(HttpStatus.OK.value())
                .build();
    }


}
