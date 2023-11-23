package com.rideSystem.Ride.Service_IMPL;

import com.rideSystem.Ride.DAO.OrderDao;
import com.rideSystem.Ride.DAO.RideDao;
import com.rideSystem.Ride.DAO.UserDao;
import com.rideSystem.Ride.POJO.*;
import com.rideSystem.Ride.Service.OrderService;
import com.rideSystem.Ride.utils.ObjectToHashMapConverter;
import com.rideSystem.Ride.utils.Response;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;


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
    @Override
    public Response createOrder(Map<String,String> requestMap){
        log.info("inside create order: requestMap {} ", requestMap);
        try{
            // Order order = orderDao.findById(1).orElseThrow();

            if(validateCreateOrderMap(requestMap)){

                Order order = initializeOrder(requestMap);


                Integer rideId = order.getRideId();

                Ride ride = rideDao.findById(rideId).orElseThrow();
                Float time_of_travel = random_number(1,120);
                ride.setTimeOfTravel(time_of_travel);
                rideDao.save(ride);

                String state = ride.getMQTTTopic();

                order = filter_price_by_state(order.getOrderId(), state);

                Float total_price = calculate_total_price(order, ride);

                order.setTotalPrice(total_price);
                orderDao.save(order);
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
            // TODO: TOKEN should match the ride person(driver/passenger) in requestMap
            Response response = Response.successResponse();
            Map<String,Object> order_data = order_info(order);
            response.setData(order_data);
            return response;

        }catch(Exception e){
            e.printStackTrace();
        }
        return Response.failedResponse("failed (OrderServiceImpl): get order", HttpStatus.INTERNAL_SERVER_ERROR);
    }

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
        log.info("requestMap: {}", requestMap);
        Integer rideId = Integer.parseInt(requestMap.get("rid"));
        log.info("rideId: {}", rideId);
        order.setRideId(rideId);
        log.info("order: {} ", order);
        orderDao.save(order);

        order.setOrderStatus(OrderStatus.UNPAID);
        log.info(order.getOrderId().toString());
        log.info(order.getOrderStatus().toString());
        order.setOrderCreatedTime(LocalDateTime.now());
        orderDao.save(order);
        return order;
    }

    private Map<String,Object> order_info(Order order){
        String orderId = order.getOrderId().toString();
        Integer rideId = order.getRideId();

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
    public Order payment(Integer orderId){
        Order order = orderDao.findById(orderId).orElseThrow();
        Integer rideId = order.getRideId();
        Ride ride = rideDao.findById(rideId).orElseThrow();
        order.setOrderPlatform("STRIPE");
        order.setPlatformId(String.valueOf("32-853295285"));
        return order;
    }
    public Response paymentResult(Integer order_id, Map<String, Boolean> success){
        try{
            Order order = orderDao.findById(order_id).orElseThrow();

            if(success.containsKey("success")){
                if(success.get("success").equals(true)){
                    order.setPlatformPaymentResult("success");
                    order.setOrderStatus(OrderStatus.PAID);
                    orderDao.save(order);
                    Response success_payment = Response.successResponse();
                    Map<String,Object> order_response = ObjectToHashMapConverter.convertObjectToMap(order);
                    success_payment.setData(order_response);
                    return success_payment;
                }
                else{
                    log.info("failed to pay");
                    return Response.failedResponse("failed: payment result failed", HttpStatus.BAD_REQUEST);

                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return Response.failedResponse("failed: payment result", HttpStatus.INTERNAL_SERVER_ERROR);
    }
    public Order filter_price_by_state(Integer order_id, String state){

        Order order = orderDao.findById(order_id).orElseThrow();
        Float starting_price;
        Float time_price;
        Float travel_n_gas_price;

        Float dynamic_price = random_number(0, 10);
        if(state.equals("Hawaii")){
            starting_price = (float) 20;
            time_price = (float) 3;
            travel_n_gas_price = (float) 2;
        }
        else if(state.equals("Georgia")){
            starting_price = (float) 25;
            time_price = (float) 4;
            travel_n_gas_price = (float) 1;
        }
        else if(state.equals("Pennsylvania")){
            starting_price = (float) 16;
            time_price = (float) 3;
            travel_n_gas_price = (float) 2;
        }
        else if(state.equals("Texas")){
            starting_price = (float) 18;
            time_price = (float) 3.2;
            travel_n_gas_price = (float) 1;
        }
        else{
            starting_price = (float) 15;
            time_price = (float) 2.9;
            travel_n_gas_price = (float) 4;
        }

        order.setDynamicPrice(dynamic_price);
        order.setSpecialLocationFee((float)0);
        order.setStartingPrice(starting_price);
        order.setTimePrice(time_price);
        order.setTravelGasFee(travel_n_gas_price);
        orderDao.save(order);
        return order;
    }
    public float random_number(int upper_limit, int lower_limit){
        return (float) Math.random() * (upper_limit - lower_limit) + lower_limit;
    }
    public float calculate_total_price(Order order, Ride ride){
        if(order.getStartingPrice() != null && order.getSpecialLocationFee() != null
        && order.getDynamicPrice() != null && order.getTimePrice() != null && order.getTravelGasFee() != null
        && ride.getTimeOfTravel() != null){
            Float starting_price = order.getStartingPrice();
            Float dynamic_price = order.getDynamicPrice();
            Float time_price = ride.getTimeOfTravel();
            Double travel_distance = haversine_distance(ride.getDepartureLatitude(), ride.getDepartureLatitude(),
                    ride.getDestinationLatitude(), ride.getDepartureLongitude());
            Float travel_time = (float) (time_price * travel_distance);
            Float travel_n_gas_fee = (float) (travel_distance * order.getTravelGasFee());
            Float special_location_fee = order.getSpecialLocationFee();
            Float total_price = starting_price + travel_time + (float)(dynamic_price * travel_distance)+travel_n_gas_fee + special_location_fee;
            return total_price;
        }
        return 0;
    }
    public double haversine_distance(Float depart_lat, Float depart_long, Float dest_lat, Float dest_long){
        double R = 3958.8;
        double rlat1 = depart_lat * (Math.PI/180);
        double rlat2 = dest_lat * (Math.PI/180);
        double difflat = rlat2 - rlat1;
        double difflong = (dest_long - depart_long)*(Math.PI/180);
        double d =2 * R * Math.asin(Math.sqrt(Math.sin(difflat/2)*Math.sin(difflat/2)+Math.cos(rlat1)*Math.cos(rlat2)*Math.sin(difflong/2)*Math.sin(difflong/2)));
        return d;
    }

    public Response deleteAllOrders(){
        try{
            orderDao.deleteAllOrders();
            return Response.successResponse();
        }catch(Exception e){
            e.printStackTrace();
        }
        return Response.failedResponse("failed: Order Service Impl: ", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public Response deleteOrdersByIds(List<Integer> ids){
        try{
            orderDao.deleteOrdersById(ids);
            return Response.successResponse();
        }catch (Exception e){
            e.printStackTrace();
        }
        return Response.failedResponse("failed at delete Orders by ids", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
