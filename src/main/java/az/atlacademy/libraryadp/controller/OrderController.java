package az.atlacademy.libraryadp.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import az.atlacademy.libraryadp.model.dto.request.OrderRequest;
import az.atlacademy.libraryadp.model.dto.response.BaseResponse;
import az.atlacademy.libraryadp.model.dto.response.OrderResponse;
import az.atlacademy.libraryadp.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/order")
public class OrderController 
{
    private final OrderService orderService;

    private static final String LOG_TEMPLATE = "{} request to /api/v1/order{}";

    @PostMapping
    @ResponseStatus(value = HttpStatus.CREATED)
    public BaseResponse<Void> createOrder(@RequestBody OrderRequest orderRequest)
    {
        log.info(LOG_TEMPLATE, "POST", "");
        return orderService.createOrder(orderRequest); 
    }

    @GetMapping
    @ResponseStatus(value = HttpStatus.OK)
    public BaseResponse<List<OrderResponse>> getOrders(
        @RequestParam(value = "pageNumber", required = false, defaultValue = "0") int pageNumber,
        @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize
    ){
        log.info(LOG_TEMPLATE, "GET", "");
        return orderService.getOrders(pageNumber, pageSize);
    }

    @GetMapping(value = "/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    public BaseResponse<OrderResponse> getOrderById(@PathVariable(value = "id") long orderId)
    {
        log.info(LOG_TEMPLATE, "GET", "/" + orderId);
        return orderService.getOrderById(orderId);
    }

    @GetMapping(value = "/get-by-student-id")
    @ResponseStatus(value = HttpStatus.OK)
    public BaseResponse<List<OrderResponse>> getOrdersByStudentId(
        @RequestParam(value = "studentId") long studentId, 
        @RequestParam(value = "pageNumber", required = false, defaultValue = "0") int pageNumber,
        @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize
    ){
        log.info(LOG_TEMPLATE, "GET", "/get-by-student-id/" + studentId);
        return orderService.getOrdersByStudentId(studentId, pageNumber, pageSize);
    }

    @GetMapping(value = "/get-by-book-id")
    @ResponseStatus(value = HttpStatus.OK)
    public BaseResponse<List<OrderResponse>> getOrdersByBookId(
        @RequestParam(value = "bookId") long bookId,
        @RequestParam(value = "pageNumber", required = false, defaultValue = "0") int pageNumber,
        @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize
    ){
        log.info(LOG_TEMPLATE, "GET", "/get-by-book-id/" + bookId);
        return orderService.getOrdersByBookId(bookId, pageNumber, pageSize);
    }

    @PutMapping(value = "/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    public BaseResponse<Void> updateOrder(
        @PathVariable(value = "id") long orderId,
        @RequestBody OrderRequest orderRequest
    ){
        log.info(LOG_TEMPLATE, "PUT", "/" + orderId);
        return orderService.updateOrder(orderId, orderRequest);
    }

    @DeleteMapping(value = "/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    public BaseResponse<Void> deleteOrder(@PathVariable(value = "id") long orderId)
    {
        log.info(LOG_TEMPLATE, "DELETE", "/" + orderId);
        return orderService.deleteOrder(orderId);
    }

    @PostMapping(value = "/{id}/return-order-book")
    @ResponseStatus(value = HttpStatus.OK)
    public BaseResponse<Void> returnOrderBook(@PathVariable(value = "id") long orderId)
    {
        log.info(LOG_TEMPLATE, "POST", "/" + orderId + "/return-order-book");
        return orderService.returnOrderBook(orderId);
    }

}
