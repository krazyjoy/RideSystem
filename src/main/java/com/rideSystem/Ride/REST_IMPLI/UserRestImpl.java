package com.rideSystem.Ride.REST_IMPLI;

import com.rideSystem.Ride.Constants.RideConstants;
import com.rideSystem.Ride.POJO.Ride;
import com.rideSystem.Ride.POJO.User;
import com.rideSystem.Ride.REST.UserRest;
import com.rideSystem.Ride.Service.UserService;
import com.rideSystem.Ride.utils.RideUtils;
import com.rideSystem.Ride.wrapper.UserWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class UserRestImpl implements UserRest {

    @Autowired // Tells the application context to inject an instance of UserService here
    UserService userService;
    @Override
    public ResponseEntity<String> signUp(Map<String, String> requestMap) {
        try{
            return userService.signUp(requestMap); // The userServiceImpl is already injected and you can use it

        }catch(Exception ex){
            ex.printStackTrace();
        }
        return RideUtils.getResponseEntity(RideConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> login(Map<String, String> requestMap){
        try{
            return userService.login(requestMap);
        }catch(Exception ex){
            ex.printStackTrace();
        }

        return RideUtils.getResponseEntity(RideConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<UserWrapper>> getAllUsers() {
        try{
            return userService.getAllUsers();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<List<UserWrapper>>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<User> updatePersonalAccount(Integer id, User updated_user) {
        try{
            return userService.updatePersonalAccount(id, updated_user);
        }catch(Exception e){
            e.printStackTrace();
        }
        return new ResponseEntity<>(new User(),HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public ResponseEntity<String> updateUser(Map<String,String> requestMap){
        try{
            return userService.updateUser(requestMap);
        }catch(Exception e){
            e.printStackTrace();
        }

        return RideUtils.getResponseEntity(RideConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> checkToken(){
        try{
            return userService.checkToken();
        }catch(Exception e){
            e.printStackTrace();
        }
        return RideUtils.getResponseEntity(RideConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @Override
    public ResponseEntity<String> changePassword(Map<String, String> requestMap) {
        try{
            return userService.changePassword(requestMap);
        }catch (Exception e){
            e.printStackTrace();
        }
        return RideUtils.getResponseEntity(RideConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @Override
    public ResponseEntity<User> getUser(Integer user_id) {
        try{
            return userService.getUser(user_id);
        }catch(Exception e){
            e.printStackTrace();
        }
        return new ResponseEntity<>(new User(),HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @Override
    public ResponseEntity<User> getUserByName(String username){
        try{
            return userService.getUserByName(username);
        }catch (Exception e){
            e.printStackTrace();
        }
        return new ResponseEntity<>(new User(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @Override
    public ResponseEntity<User> getUserByCell(@RequestParam("contact_number") String contact_number){
        try{
            return userService.getUserByCell(contact_number);
        }catch(Exception e){
            e.printStackTrace();
        }

        return new ResponseEntity<>(new User(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @Override
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String tokenHeader){
        try{
            return userService.logout(tokenHeader);
        }catch(Exception e){
            e.printStackTrace();
        }
        return new ResponseEntity<>(RideConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);

    }
}
