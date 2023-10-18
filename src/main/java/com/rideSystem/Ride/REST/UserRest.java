package com.rideSystem.Ride.REST;


import com.rideSystem.Ride.POJO.User;
import com.rideSystem.Ride.wrapper.UserWrapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequestMapping(path="/user")
public interface UserRest {

    @PostMapping(path="/signup")
    public ResponseEntity<String> signUp(@RequestBody(required = true)
                                         Map<String, String> requestMap);

    @PostMapping(path="/login")
    public ResponseEntity<String> login(@RequestBody(required = true)
                                        Map<String, String> requestMap);

    @GetMapping(path="/getusers")
    public ResponseEntity<List<UserWrapper>> getAllUsers();

    @PutMapping("/{userId}")
    public ResponseEntity<User> updatePersonalAccount(@PathVariable("userId") Integer id,@RequestBody User updated_user);
    @PutMapping(path="/updateUser")
    public ResponseEntity<String> updateUser(@RequestBody(required = true) Map<String,String> requestMap);

    @GetMapping(path="/checkToken")
    public ResponseEntity<String> checkToken();


    @PostMapping(path="/changePassword")
    public ResponseEntity<String> changePassword(@RequestBody Map<String,String> requestMap);
}
