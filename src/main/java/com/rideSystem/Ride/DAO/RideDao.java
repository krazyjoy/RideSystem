package com.rideSystem.Ride.DAO;

import com.rideSystem.Ride.POJO.Ride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface RideDao extends JpaRepository<Ride, Integer> {
//    Ride createRideRequest(@Param("uid") Integer passenger_id,@Param("pickup_longitude") Float departure_longitude,
//                           @Param("pickup_latitude") Float departure_latitude, @Param("dest_longitude") Float destination_longitude,
//                           @Param("dest_latitude") Float destination_latitude);
}
