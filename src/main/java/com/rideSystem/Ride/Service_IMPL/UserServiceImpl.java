package com.rideSystem.Ride.Service_IMPL;

import com.rideSystem.Ride.Constants.RideConstants;
import com.rideSystem.Ride.DAO.UserDao;
//import com.rideSystem.Ride.JWT.CustomerUsersDetailsService;
//import com.rideSystem.Ride.JWT.JwtFilter;
//import com.rideSystem.Ride.JWT.JwtUtil;
import com.rideSystem.Ride.JWT.CustomerUsersDetailsService;
import com.rideSystem.Ride.JWT.JwtFilter;
import com.rideSystem.Ride.JWT.JwtUtil;
import com.rideSystem.Ride.POJO.User;
import com.rideSystem.Ride.Service.UserService;
import com.rideSystem.Ride.utils.RideUtils;
import com.rideSystem.Ride.wrapper.UserWrapper;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import javax.print.DocFlavor;

import java.util.*;


@NoArgsConstructor
@Slf4j
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserDao userDao;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private CustomerUsersDetailsService customerUsersDetailsService;
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private JwtFilter jwtFilter;


    @Autowired
    public UserServiceImpl(UserDao userDao, AuthenticationManager authenticationManager, CustomerUsersDetailsService customerUsersDetailsService, JwtUtil jwtUtil, JwtFilter jwtFilter) {
        this.userDao = userDao;
        this.authenticationManager = authenticationManager;
        this.customerUsersDetailsService = customerUsersDetailsService;
        this.jwtUtil = jwtUtil;
        this.jwtFilter = jwtFilter;
    }



    @Override
    public ResponseEntity<String> signUp(Map<String, String> requestMap){
        log.info("Inside signup {}", requestMap);
        try{
            if(validateSignUpMap(requestMap)){
                // create a user object if already exists
                User user = userDao.findByContactNumber(requestMap.get("contactNumber"));
                log.info("is valid user");
                if(Objects.isNull(user)){
                    userDao.save(getUserFromMap(requestMap));
                    return RideUtils.getResponseEntity("Successfully Registered", HttpStatus.OK);
                }else{
                    return RideUtils.getResponseEntity("Contact Number already exists", HttpStatus.BAD_REQUEST);
                }
            }else{
                return RideUtils.getResponseEntity(RideConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return RideUtils.getResponseEntity("user service internal error", HttpStatus.INTERNAL_SERVER_ERROR);

    }
    @Override
    public ResponseEntity<String> login(Map<String, String> requestMap){
        log.info("Inside login");

        try{
             //create an authenticator
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(requestMap.get("userName"), requestMap.get("password"))
            );

            log.info("created auth\n");
            log.info("check if auth is authenticated\n");
            if(auth.isAuthenticated()){
                log.info("check status\n");
                if (customerUsersDetailsService.getUserDetail().getStatus().equalsIgnoreCase("true")){
                    log.info("give token\n");
                    log.info(customerUsersDetailsService.getUserDetail().getUserName());
                    log.info(customerUsersDetailsService.getUserDetail().getRole());
                    return new ResponseEntity<String>("{\"token\":\""+
                            jwtUtil.generateToken(customerUsersDetailsService.getUserDetail().getUserName(),customerUsersDetailsService.getUserDetail().getRole()) + "\"}",
                            HttpStatus.OK);
                }else{
                    log.info("not accessible with this status\n");
                    return new ResponseEntity<String>("{\"message\":\""+"Wait for admin approval."+"\"}", HttpStatus.BAD_REQUEST);
                }
            }

        }catch(Exception ex){
            ex.printStackTrace();
        }

        return new ResponseEntity<String>("{\"message\":\""+"Bad Credentials."+"\"}", HttpStatus.BAD_REQUEST);
    }
    @Override
    public ResponseEntity<List<UserWrapper>> getAllUsers(){
        try{
            log.info("get all users()");
//            return jwtFilter.isAdmin()?new ResponseEntity<>(userDao.getAllUsers(), HttpStatus.OK): new ResponseEntity<>(
//                    new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            return new ResponseEntity<>(userDao.getAllUsers(), HttpStatus.OK);

        }catch(Exception e){
            e.printStackTrace();
        }

        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @Override
    public ResponseEntity<String> updateUser(Map<String, String> requestMap){
        try{
            log.info("inside update user status");
            //if(jwtFilter.isAdmin()){

            if(true){
                log.info("is admin");
                // TODO:
                //Optional<User> optional = userDao.findByUserName(requestMap.get("userName")); userName did not work
                Optional<User> optional = userDao.findById(Integer.parseInt(requestMap.get("id")));
                if(!optional.isEmpty()){
                    log.info("optional is not empty");
                    userDao.updateStatus(requestMap.get("status"), Integer.parseInt(requestMap.get("id")));
                    return RideUtils.getResponseEntity("User Status updated Successfully", HttpStatus.OK);
                }
            }else{
                RideUtils.getResponseEntity(RideConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return RideUtils.getResponseEntity(RideConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public String createUsername(){

        UUID uuid = UUID.randomUUID();
        String uuidAsString = uuid.toString();
        System.out.println("Your UUID is: " + uuidAsString);
        return uuidAsString;

    }
    private boolean validateSignUpMap(Map<String, String> requestMap){

        if (requestMap.containsKey("password")
                && requestMap.containsKey("contactNumber") && requestMap.containsKey("identity")){
            return true;
        }
        return false;
    }


    private User getUserFromMap(Map<String, String> requestMap){
        User user = new User();
        if(!requestMap.containsKey("userName")){

            String fakeUsername = createUsername();
            log.info("create username",fakeUsername);
            user.setUserName(fakeUsername);
        }else{
            user.setUserName(requestMap.get("userName"));
        }
        if(requestMap.containsKey("contactNumber")){
            user.setContactNumber(requestMap.get("contactNumber"));
        }
        if(requestMap.containsKey("identity")){
            user.setIdentity(requestMap.get("identity"));
            if(user.getIdentity() == "driver"){
                if(requestMap.containsKey("vehicleType")){
                    user.setVehicleType(requestMap.get("vehicleType"));
                }
                if(requestMap.containsKey("licensePlateNumber"))
                    user.setLicensePlateNumber(requestMap.get("licensePlateNumber"));
                if(requestMap.containsKey("mileage"))
                    user.setMileage(Long.valueOf(requestMap.get("mileage")));
            }
        }
        if(requestMap.containsKey("password")){
            user.setPassword(requestMap.get("password"));
        }


        if(requestMap.containsKey("state"))
            user.setState(requestMap.get("state"));
        if(requestMap.containsKey("city"))
            user.setCity(requestMap.get("city"));

        user.setStatus("true");
        user.setRole("user");
        return user;
    }

    public ResponseEntity<String> checkToken(){
        return RideUtils.getResponseEntity("true", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> changePassword(Map<String,String> requestMap) {
        try{
            User userObj = userDao.findByUserName(jwtFilter.getCurrentUser()); // USE TOKEN VALUE TO EXTRACT USER PASSWORD
            if(!userObj.equals(null)){
                if(userObj.getPassword().equals(requestMap.get("oldPassword"))){
                    userObj.setPassword(requestMap.get("newPassword"));
                    userDao.save(userObj);
                    return RideUtils.getResponseEntity("Password Updateed Successfully!", HttpStatus.OK);
                }
                return RideUtils.getResponseEntity("Incorrect Old Password", HttpStatus.BAD_REQUEST);
            }
            return RideUtils.getResponseEntity(RideConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);

        }catch(Exception e){
            e.printStackTrace();
        }
        return RideUtils.getResponseEntity(RideConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<User> updatePersonalAccount(Integer id, User updated_user) {
        try{
           User exists_user = userDao.findById(id).orElseThrow();
           if(updated_user.getUserName()!=null){
               exists_user.setUserName(updated_user.getUserName());
           }
           if(updated_user.getContactNumber()!=null){
               exists_user.setContactNumber(updated_user.getContactNumber());
           }
           if(updated_user.getCity()!=null){
               exists_user.setCity(updated_user.getCity());
           }

           userDao.save(exists_user);
           return new ResponseEntity<>(exists_user, HttpStatus.OK);
        }catch(Exception e){
            e.printStackTrace();
        }
        return new ResponseEntity<User>(new User(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<User> getUser(Integer user_id) {
        try {
            log.info("UserServiceImpl: user_id : {}", user_id);
            User exists_user = userDao.findById(user_id).orElseThrow();
            log.info("exists_user: {}", exists_user.getUserName());
            return new ResponseEntity<>(exists_user, HttpStatus.OK);
        }catch(Exception e){
            e.printStackTrace();
        }
        return new ResponseEntity<User>(new User(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<User> getUserByName(String username) {
        try{
            log.info("GetUserByName: {}", username);
            User exist_user= userDao.findByUserName(username);
            log.info("exist_user: {}", exist_user.getUserName());
            return new ResponseEntity<>(exist_user, HttpStatus.OK);
        }catch(Exception e){
            e.printStackTrace();
        }
        return new ResponseEntity<>(new User(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<User> getUserByCell(@RequestParam("contact_number") String contact_number){
        try{
            log.info("GetUserByCell: {}", contact_number);
            User exist_user = userDao.findByContactNumber(contact_number);
            log.info("exist_user: {}", exist_user.getUserName());
            return new ResponseEntity<>(exist_user, HttpStatus.OK);
        }catch(Exception e){
            e.printStackTrace();
        }

        return new ResponseEntity<>(new User(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @Override
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String tokenHeader){
        String token = tokenHeader.replace("Bearer ", "");
        jwtUtil.expireToken(token);
        return ResponseEntity.ok("Logged out successfully");
    }
}
