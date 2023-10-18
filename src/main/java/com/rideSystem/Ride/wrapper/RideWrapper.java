package com.rideSystem.Ride.wrapper;

import com.rideSystem.Ride.POJO.RideStatus;
import com.rideSystem.Ride.POJO.RideType;

import java.time.LocalDateTime;

public class RideWrapper {
    private Integer rideId;
    private LocalDateTime orderCreatedTime;
    private Integer passengerId;
    private Integer driverId;

    private String MQTTTopic;
    private RideType rideType;

    private Float departureLatitude;

    private Float departureLongitude;
    private String departureAddress;
    private Float destinationLatitude;
    private Float destinationLongitude;
    private String destinationAddress;
    private RideStatus rideStatus;
    private LocalDateTime driverReceivedOrderTime;
    private LocalDateTime pickedUpTime;
    private LocalDateTime arrivalTime;
    private LocalDateTime cancelTime;
    private Float timeOfTravel;
    private Integer rideOrderId;
    private Float rideRating;
    private String Review;

    public RideWrapper(Integer rideId, LocalDateTime orderCreatedTime, Integer passengerId, Integer driverId, String MQTTTopic, RideType rideType, Float departureLatitude, Float departureLongitude, String departureAddress, Float destinationLatitude, Float destinationLongitude, String destinationAddress, RideStatus rideStatus, LocalDateTime driverReceivedOrderTime, LocalDateTime pickedUpTime, LocalDateTime arrivalTime, LocalDateTime cancelTime, Float timeOfTravel, Integer rideOrderId, Float rideRating, String review) {
        this.rideId = rideId;
        this.orderCreatedTime = orderCreatedTime;
        this.passengerId = passengerId;
        this.driverId = driverId;
        this.MQTTTopic = MQTTTopic;
        this.rideType = rideType;
        this.departureLatitude = departureLatitude;
        this.departureLongitude = departureLongitude;
        this.departureAddress = departureAddress;
        this.destinationLatitude = destinationLatitude;
        this.destinationLongitude = destinationLongitude;
        this.destinationAddress = destinationAddress;
        this.rideStatus = rideStatus;
        this.driverReceivedOrderTime = driverReceivedOrderTime;
        this.pickedUpTime = pickedUpTime;
        this.arrivalTime = arrivalTime;
        this.cancelTime = cancelTime;
        this.timeOfTravel = timeOfTravel;
        this.rideOrderId = rideOrderId;
        this.rideRating = rideRating;
        Review = review;
    }

    public Integer getRideId() {
        return rideId;
    }

    public void setRideId(Integer rideId) {
        this.rideId = rideId;
    }

    public LocalDateTime getOrderCreatedTime() {
        return orderCreatedTime;
    }

    public void setOrderCreatedTime(LocalDateTime orderCreatedTime) {
        this.orderCreatedTime = orderCreatedTime;
    }

    public Integer getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(Integer passengerId) {
        this.passengerId = passengerId;
    }

    public Integer getDriverId() {
        return driverId;
    }

    public void setDriverId(Integer driverId) {
        this.driverId = driverId;
    }

    public String getMQTTTopic() {
        return MQTTTopic;
    }

    public void setMQTTTopic(String MQTTTopic) {
        this.MQTTTopic = MQTTTopic;
    }

    public RideType getRideType() {
        return rideType;
    }

    public void setRideType(RideType rideType) {
        this.rideType = rideType;
    }

    public Float getDepartureLatitude() {
        return departureLatitude;
    }

    public void setDepartureLatitude(Float departureLatitude) {
        this.departureLatitude = departureLatitude;
    }

    public Float getDepartureLongitude() {
        return departureLongitude;
    }

    public void setDepartureLongitude(Float departureLongitude) {
        this.departureLongitude = departureLongitude;
    }

    public String getDepartureAddress() {
        return departureAddress;
    }

    public void setDepartureAddress(String departureAddress) {
        this.departureAddress = departureAddress;
    }

    public Float getDestinationLatitude() {
        return destinationLatitude;
    }

    public void setDestinationLatitude(Float destinationLatitude) {
        this.destinationLatitude = destinationLatitude;
    }

    public Float getDestinationLongitude() {
        return destinationLongitude;
    }

    public void setDestinationLongitude(Float destinationLongitude) {
        this.destinationLongitude = destinationLongitude;
    }

    public String getDestinationAddress() {
        return destinationAddress;
    }

    public void setDestinationAddress(String destinationAddress) {
        this.destinationAddress = destinationAddress;
    }

    public RideStatus getRideStatus() {
        return rideStatus;
    }

    public void setRideStatus(RideStatus rideStatus) {
        this.rideStatus = rideStatus;
    }

    public LocalDateTime getDriverReceivedOrderTime() {
        return driverReceivedOrderTime;
    }

    public void setDriverReceivedOrderTime(LocalDateTime driverReceivedOrderTime) {
        this.driverReceivedOrderTime = driverReceivedOrderTime;
    }

    public LocalDateTime getPickedUpTime() {
        return pickedUpTime;
    }

    public void setPickedUpTime(LocalDateTime pickedUpTime) {
        this.pickedUpTime = pickedUpTime;
    }

    public LocalDateTime getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(LocalDateTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public LocalDateTime getCancelTime() {
        return cancelTime;
    }

    public void setCancelTime(LocalDateTime cancelTime) {
        this.cancelTime = cancelTime;
    }

    public Float getTimeOfTravel() {
        return timeOfTravel;
    }

    public void setTimeOfTravel(Float timeOfTravel) {
        this.timeOfTravel = timeOfTravel;
    }

    public Integer getRideOrderId() {
        return rideOrderId;
    }

    public void setRideOrderId(Integer rideOrderId) {
        this.rideOrderId = rideOrderId;
    }

    public Float getRideRating() {
        return rideRating;
    }

    public void setRideRating(Float rideRating) {
        this.rideRating = rideRating;
    }

    public String getReview() {
        return Review;
    }

    public void setReview(String review) {
        Review = review;
    }
}