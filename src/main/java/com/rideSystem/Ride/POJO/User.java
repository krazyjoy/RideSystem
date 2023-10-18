package com.rideSystem.Ride.POJO;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;


@NamedQuery(name = "User.findByContactNumber",query = "select u from User u where u.contactNumber=:contactNumber")
@NamedQuery(name="User.findByUserName", query="select u from User u where u.userName=:userName")
@NamedQuery(name="User.getAllUsers", query="select new com.rideSystem.Ride.wrapper.UserWrapper(u.id,u.userName, u.contactNumber, u.status, u.identity) from User u where u.role='user'")
@NamedQuery(name="User.getAllAdmin", query="select u.userName from User u where u.role='admin'" )
@NamedQuery(name="User.updateStatus", query="update User u set u.status=:status where u.id=:id")
@NamedQuery(name="User.getUserById", query = "select u from User u where u.id=:id")
@Component
@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "user")
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer uid;

    @Column(name = "contact_number")
    private String contactNumber;

    @Column(name = "identity")
    private String identity;

    @Column(name = "password")
    private String password;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "license_plate_pumber")
    private String licensePlateNumber;

    @Column(name="vehicle_type")
    private String vehicleType;

    @Column(name="mileage")
    private Long mileage;

    @Column(name="state")
    private String state;

    @Column(name="city")
    private String city;

    @Column(name="status")
    private String status;


    @Column(name="role")
    private String role;
}
