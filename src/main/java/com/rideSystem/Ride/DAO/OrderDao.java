package com.rideSystem.Ride.DAO;

import com.rideSystem.Ride.POJO.Order;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderDao extends JpaRepository<Order, Integer> {
    @Transactional
    default void deleteAllOrders(){
        deleteAllInBatch();
    }
    @Modifying
    @Query("DELETE FROM Order o WHERE o.orderId IN :orderIds")
    void deleteOrdersById(@Param("orderIds") List<Integer> orderIds);
}
