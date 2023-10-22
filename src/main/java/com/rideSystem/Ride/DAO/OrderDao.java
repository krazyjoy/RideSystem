package com.rideSystem.Ride.DAO;

import com.rideSystem.Ride.POJO.Order;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;

public interface OrderDao extends JpaRepository<Order, Integer> {

}
