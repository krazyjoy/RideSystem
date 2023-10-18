package com.rideSystem.Ride.Service_IMPL;

import com.alipay.easysdk.kernel.Config;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rideSystem.Ride.DAO.OrderDao;
import com.rideSystem.Ride.DAO.RideDao;
import com.rideSystem.Ride.DAO.UserDao;
import com.rideSystem.Ride.POJO.*;
import com.rideSystem.Ride.Service.OrderService;
import com.rideSystem.Ride.utils.Alipay;
import com.rideSystem.Ride.utils.ObjectToHashMapConverter;
import com.rideSystem.Ride.utils.Response;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.alipay.easysdk.factory.Factory;

import java.time.LocalDateTime;
import java.util.*;

@Service
@NoArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {
    @Autowired
    OrderDao orderDao;
    @Autowired
    UserDao userDao;
    @Autowired
    RideDao rideDao;

    public OrderServiceImpl(OrderDao orderDao, UserDao userDao, RideDao rideDao){
        this.orderDao = orderDao;
        this.userDao = userDao;
        this.rideDao = rideDao;
    }
    public Response createOrder(Map<String,String> requestMap){
        log.info("inside create order");
        try{
            if(validateCreateOrderMap(requestMap)){

                Order order = initializeOrder(requestMap);
                Map<String,Object> order_data = ObjectToHashMapConverter.convertObjectToMap(order);

                log.info("order data {}",order_data);
                Response response = Response.successResponse();
                response.setData(order_data);
                return response;
            }else{
                return Response.failedResponse("none existing user", HttpStatus.BAD_REQUEST);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return Response.failedResponse("failed: create order", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public Response getOrder(Integer orderId, Map<String, String> requestMap) {
        try{
            Order order = orderDao.findById(orderId).orElseThrow();
            Response response = Response.successResponse();
            Map<String,Object> order_data = order_info(order);
            response.setData(order_data);
            return response;

        }catch(Exception e){
            e.printStackTrace();
        }
        return Response.failedResponse("failed (OrderServiceImpl): get order", HttpStatus.INTERNAL_SERVER_ERROR);
    }
//    private Object jsonObjectMapper(Order order, List<Order> list){
//        ObjectMapper jsonMapper = new ObjectMapper();
//        jsonMapper.writeValueAsString(list);
//        return jsonMapper;
//    }
    private boolean validateCreateOrderMap(Map<String,String> requestMap){
        log.info("validate: create order map");
        if(requestMap.containsKey("rid")){
            log.info("true");
            return true;
        }else{
            log.info("false");
            return false;
        }
    }

    private Order initializeOrder(Map<String,String> requestMap){

        Order order = new Order();
        log.info("order info: ", order.getOrderId().toString());
        orderDao.save(order);
        Ride ride = rideDao.findById(Integer.parseInt(requestMap.get("rid"))).orElseThrow();
        if(ride == null)
            log.info("initialize order: ride is null");
        log.info("ride: ",ride.getRideId().toString());
        order.setRideId(ride);
        log.info("status: ",OrderStatus.UNPAID.toString());

        order.setOrderStatus(OrderStatus.UNPAID);
        log.info(order.getOrderId().toString());
        log.info(order.getOrderStatus().toString());
        order.setOrderCreatedTime(LocalDateTime.now());
        orderDao.save(order);
        return order;
    }
    private Map<String,Object> order_info(Order order){
        String orderId = order.getOrderId().toString();
        Ride ride = order.getRideId();
        String rideId = ride.getRideId().toString();
        String createTime = "";
        String totalPrice="";
        if(order.getOrderCreatedTime()!=null)
            order.getOrderCreatedTime().toString();

        if(order.getTotalPrice()!=null)
            totalPrice = order.getTotalPrice().toString();

        Map<String, Object> order_data = new HashMap<>();
        order_data.put("orderId", orderId);
        order_data.put("rideId", rideId);
        order_data.put("createTime", createTime);
        order_data.put("totalPrice", totalPrice);
        return  order_data;
    }

    private void invokeAlipay() throws Exception{
        // 1. set options
        Factory.setOptions(Alipay.getOptions());
        try{
            // 2. API call

        }catch(Exception e){

        }
    }

}
