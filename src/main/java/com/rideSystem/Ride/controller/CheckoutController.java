//package com.rideSystem.Ride.controller;
//import com.rideSystem.Ride.stripe.ChargeRequest;
//import com.rideSystem.Ride.utils.Response;
//import com.stripe.exception.StripeException;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.data.repository.query.Param;
//import org.springframework.http.client.support.BasicAuthenticationInterceptor;
//import org.springframework.stereotype.Controller;
//import org.springframework.stereotype.Service;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.client.RestTemplate;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.ResponseEntity;
//
//
//
//import java.util.Base64;
//import java.util.HashMap;
//import java.util.Map;
//import org.springframework.security.access.prepost.PreAuthorize;
//@Slf4j
//@Controller
//public class CheckoutController {
//    @Value("${STRIPE_PUBLIC_KEY}")
//    private String stripePublicKey;
//
//
//
//    // @PostMapping(path="/ride/{oId}") // requests from supervisor
//    @RequestMapping("/checkout") // thymeleaf template
//    public String checkout(@RequestBody Map<String,String> requestMap,Model model) throws StripeException {
//        try{
//            Integer order_id = Integer.parseInt(requestMap.get("oId"));
//            model.addAttribute("amount", 50*100);
//            model.addAttribute("stripePublicKey", stripePublicKey);
//            model.addAttribute("currency", ChargeRequest.Currency.USD);
//            model.addAttribute("order_id", order_id);
//
//            Response response = Response.successResponse();
//            Map<String,Object> order_data = new HashMap<>();
//            order_data.put("order_id", order_id);
//            response.setData(order_data);
//            log.info("/checkout, {}", response);
//
//        }catch(Exception ex){
//            ex.printStackTrace();
//        }
//        log.info("checkout --- ");
//        log.info("end of checkout");
//        return "checkout";
//    }
//
//
//    private String addBasicAuth(Integer oId, String username, String password){
//        // Get username and password for Basic Authentication
//        // Create the credentials string for Basic Authentication
//        String credentials = username + ":" + password;
//        String encodedCredentials = new String(Base64.getEncoder().encode(credentials.getBytes()));
//
//        // Set up headers for Basic Authentication
//        HttpHeaders headers = new HttpHeaders();
//        headers.add("Authorization", "Basic " + encodedCredentials);
//
//        // Create a RestTemplate with Basic Authentication
//        RestTemplate restTemplate = new RestTemplate();
//        restTemplate.getInterceptors().add(new BasicAuthenticationInterceptor(username, password));
//
//        // Create the request URL
//        String url = "http://localhost:8089/checkout/ride/"+oId; // Replace with your actual URL
//        log.info("string URL: {}", url);
//        // Create the request entity with headers (for POST request)
//        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
//
//        // Make the POST request
//        ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
//
//        // Get the response body
//        String responseBody = responseEntity.getBody();
//
//
//        return responseBody;
//    }
//}
package com.rideSystem.Ride.controller;

import com.rideSystem.Ride.DAO.OrderDao;
import com.rideSystem.Ride.POJO.Order;
import com.rideSystem.Ride.stripe.ChargeRequest;
import com.stripe.exception.StripeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@Slf4j
public class CheckoutController{

    @Value("${STRIPE_PUBLIC_KEY}")
    private String stripePublicKey;
    @Autowired
    OrderDao orderDao;


    @PostMapping(path="/checkout")
    public String checkout(@RequestParam("order_id") String orderId, Model model) throws StripeException {
        log.info("/checkout");

        Order order = orderDao.findById(Integer.parseInt(orderId)).orElseThrow();
        Integer amount=0;
        if(order !=null)
            amount = order.getTotalPrice().intValue();
            log.info("amount, {}", amount);
        model.addAttribute("order_id", orderId);
        model.addAttribute("amount", amount*100); // in cents
        model.addAttribute("stripePublicKey", stripePublicKey);
        model.addAttribute("currency", ChargeRequest.Currency.EUR);

        return "checkout";
    }
}