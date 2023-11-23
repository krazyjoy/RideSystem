package com.rideSystem.Ride.DAO;

import com.rideSystem.Ride.POJO.Trajectory;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TrajectoryDao extends JpaRepository<Trajectory, Integer> {
    List<Trajectory> findRideTrajectory(Integer rideId);

    @Transactional
    default void deleteAllTrajectories(){

        deleteAllInBatch();

    }

    @Modifying
    @Transactional
    @Query("DELETE FROM Trajectory t WHERE t.trackingId IN :ids")
    void deleteTrajectoriesByIds(@Param("ids")List<Integer> ids);
}
