package com.rideSystem.Ride.POJO;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name="Trajectory")
@Data
public class Trajectory implements Serializable {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="tracjectory_id")
    private Integer trackingId;


    private Integer correspondedRideId;

    private LocalDateTime timeSequence;

    private Float GPSLongitude;

    private Float GPSLatitude;

    private Float speed;

    private Float altitude;

}
