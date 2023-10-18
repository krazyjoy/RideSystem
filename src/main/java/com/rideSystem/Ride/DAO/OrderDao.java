package com.rideSystem.Ride.DAO;

import com.rideSystem.Ride.POJO.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderDao extends JpaRepository<Order, Integer> {

}
