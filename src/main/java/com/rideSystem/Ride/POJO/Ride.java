package com.rideSystem.Ride.POJO;


import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.time.LocalDateTime;

// @NamedQuery(name="Ride.createRideRequest", query="insert into Rides(passenger_id, departure_longitude, departure_latitude)")
@Component
@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name="Rides")
public class Ride implements Serializable {

    public static final Long serialVersionUid = 1345L;
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="ride_id")
    private Integer rideId;

    @Column(name="order_created_time")
    private LocalDateTime orderCreatedTime;

    @ManyToOne(fetch = FetchType.EAGER) // AVOID Jackson serialization issue
    @JoinColumn(name="passenger_id_fk", nullable = false)
    private User passengerId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="driver_id_fk", nullable = true)
    private User driverId;


    @Column(name= "MQTT_Topic")
    private String MQTTTopic;

    @Enumerated(EnumType.STRING)
    private RideType rideType;

    // missing
    @Column(name="departure_latitude")
    private Float departureLatitude;

    // missing
    @Column(name="departure_longitude")
    private Float departureLongitude;

    @Column(name="departure_address")
    private String departureAddress;
    // missing
    @Column(name="destination_latitude")
    private Float destinationLatitude;
    // missing
    @Column(name="destination_longitude")
    private Float destinationLongitude;

    @Column(name="destination_address")
    private String destinationAddress;

    @Enumerated(EnumType.STRING)
    private RideStatus rideStatus;

    @Column(name="driver_received_order_time")
    private LocalDateTime driverReceivedOrderTime;

    @Column(name="picked_up_time")
    private LocalDateTime pickedUpTime;
    @Column(name="arrival_time")
    private LocalDateTime arrivalTime;

    @Column(name="cancel_time")
    private LocalDateTime cancelTime;

    @Column(name="time_of_travel")
    private Float timeOfTravel;

    @Column(name="ride_order_id")
    private Integer rideOrderId;

    @Column(name="ride_rating")
    private Float rideRating;

    @Column(name="ride_review", columnDefinition = "json")
    private String Review;

}


