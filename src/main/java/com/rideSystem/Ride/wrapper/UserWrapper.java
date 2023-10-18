package com.rideSystem.Ride.wrapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserWrapper {
    private Integer id;
    private String userName;
    private String contactNumber;
    private String status;
    private String identity;

}
