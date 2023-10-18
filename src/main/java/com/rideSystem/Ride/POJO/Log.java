package com.rideSystem.Ride.POJO;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name="log")
public class Log implements Serializable {
    public static final Long serialVersionUid = 1345L;

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="log_id")
    private Integer logId;

    @Column(name="log_time")
    private LocalDateTime logTime;

    @Column(name="source_module")
    private String sourceModule;

    @Column(name="log_level")
    private LogLevel logLevel;

    @Column(name="log_content")
    private String logContent;

}
