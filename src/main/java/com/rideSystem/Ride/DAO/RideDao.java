package com.rideSystem.Ride.DAO;

import com.rideSystem.Ride.POJO.Ride;
import com.rideSystem.Ride.POJO.RideStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface RideDao extends JpaRepository<Ride, Integer> {
//    Ride createRideRequest(@Param("uid") Integer passenger_id,@Param("pickup_longitude") Float departure_longitude,
//                           @Param("pickup_latitude") Float departure_latitude, @Param("dest_longitude") Float destination_longitude,
//                           @Param("dest_latitude") Float destination_latitude);
    Page<Ride> findLatestRide(Pageable pageable);
    List<Ride> findByRideStatus(RideStatus rideStatus);

    @Query("SELECT r FROM Ride r WHERE r.rideId = :rideId")
    Ride findByRideIdWithStatus(@Param("rideId") Integer rideId);
    @Transactional
    default void deleteAllRides(){

        deleteAllInBatch();

    }
}
