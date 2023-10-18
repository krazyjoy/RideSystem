package com.rideSystem.Ride.POJO;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.time.LocalDateTime;
@Component
@Data
@DynamicInsert
@DynamicUpdate
@Entity
@Table(name = "Orders")
public class Order implements Serializable {
    public static final Long serialVersionUid = 1345L;

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="order_id")
    private Integer orderId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="ride_id_fk", nullable = false)
    private Ride rideId;

    @Column(name="order_created_time")
    private LocalDateTime orderCreatedTime;

    @Column(name="total_price")
    private Float totalPrice;

    @Column(name="starting_price")
    private Float startingPrice;

    @Column(name="travel_n_gas_price")
    private Float travelGasFee;

    @Column(name="time_price")
    private Float timePrice;

    @Column(name="special_location_fee")
    private Float specialLocationFee;

    @Column(name="dynamic_price")
    private Float dynamicPrice;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @Column(name="order_platform")
    private String orderPlatform;

    @Column(name="order_platform_id")
    private String platformId;

    @Column(name="platform_payment_result")
    private String platformPaymentResult;
}
