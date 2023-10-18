package com.rideSystem.Ride.utils;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Response {

    private Integer status;
    private String msg;
    private Map<String, Object> data;

    public Response(){

    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public static Response successResponse(){
        Response response = new Response();
        response.setStatus(0);
        response.setMsg("success");
        HashMap<String,Object> data = new HashMap<>();
        response.setData(data);
        return response;
    }
    public static Response failedResponse(String responseMessage, HttpStatus httpStatus){

        Response response = new Response();
        response.setStatus(httpStatus.value());
        response.setMsg("\"{fail\""+ responseMessage + "\"}"+ httpStatus);
        return  response;
    }

}
