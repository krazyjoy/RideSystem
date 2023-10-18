package com.rideSystem.Ride.utils;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class RideUtils {

    private RideUtils(){

    }

    public static ResponseEntity<String> getResponseEntity(String responseMessage, HttpStatus httpStatus){
        return new ResponseEntity<String>("\"message\":\"" + responseMessage + "\"}", httpStatus);
    }





}
