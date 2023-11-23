package com.rideSystem.Ride.DAO;

import com.rideSystem.Ride.POJO.User;
import com.rideSystem.Ride.wrapper.UserWrapper;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface UserDao extends JpaRepository<User, Integer> {
    User findByContactNumber(@Param("contactNumber") String contactNumber);
    User findByUserName(@Param("userName") String userName);

    List<UserWrapper> getAllUsers();
    List<String> getAllAdmin();


    // update()
    @Transactional
    @Modifying
    Integer updateStatus(@Param("status") String status, @Param("id") Integer id);

    User getUserById(@Param("id") Integer id);

}
