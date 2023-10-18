package com.rideSystem.Ride.Service;

import com.rideSystem.Ride.POJO.User;
import com.rideSystem.Ride.wrapper.UserWrapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

public interface UserService {
    ResponseEntity<String> signUp(Map<String, String> requestMap);
    ResponseEntity<String> login(Map<String, String> requestMap);
    ResponseEntity<List<UserWrapper>> getAllUsers();

    ResponseEntity<String> updateUser(Map<String, String> requestMap);

    ResponseEntity<String> checkToken();

    ResponseEntity<String> changePassword(Map<String,String> requestMap);


    ResponseEntity<User> updatePersonalAccount(@PathVariable("userId") Integer id,
                                                      @RequestBody User updated_user);
}