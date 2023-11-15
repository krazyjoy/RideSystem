package com.rideSystem.Ride.DAO;

import com.rideSystem.Ride.POJO.Ride;
import com.rideSystem.Ride.POJO.RideStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RideDao extends JpaRepository<Ride, Integer> {
//    Ride createRideRequest(@Param("uid") Integer passenger_id,@Param("pickup_longitude") Float departure_longitude,
//                           @Param("pickup_latitude") Float departure_latitude, @Param("dest_longitude") Float destination_longitude,
//                           @Param("dest_latitude") Float destination_latitude);
    Page<Ride> findLatestRide(Pageable pageable);
    List<Ride> findByRideStatus(RideStatus rideStatus);

}
