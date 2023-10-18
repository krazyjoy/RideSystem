package com.rideSystem.Ride.JWT;

import com.rideSystem.Ride.DAO.UserDao;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@AllArgsConstructor
@Slf4j
@Service

public class CustomerUsersDetailsService implements UserDetailsService {

    UserDao userDao;
    private com.rideSystem.Ride.POJO.User userDetails;

    @Override
//    public UserDetails loadUserByUsername(String contactNumber) throws UsernameNotFoundException {
//        log.info("Inside LoadUserByContactNumber - {} ", contactNumber);
//        userDetails = userDao.findByContactNumber(contactNumber);

    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        log.info("Inside LoadUserByUsername - {} ", userName);
        userDetails = userDao.findByUserName(userName);
        log.info("customer users detail: {} ",userDetails);
        if (!Objects.isNull(userDetails)) {
            // List <GrantedAuthority> authorities = getAuthorities();
            // import org.springframework.security.core.userdetails.User; not com/rideSystem/Ride/POJO/User.java
            return new User(userDetails.getUserName(), userDetails.getPassword(), new ArrayList<>());

        } else {
            throw new UsernameNotFoundException("User not found");
        }

    }

    private List<GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        // Add any authorities/roles that the user has (e.g., ROLE_USER, ROLE_ADMIN)
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        return authorities;
    }
    public com.rideSystem.Ride.POJO.User getUserDetail() { // UserServiceImpl
        com.rideSystem.Ride.POJO.User user = userDetails;
        user.setPassword(null);
        return user;
    }
}

