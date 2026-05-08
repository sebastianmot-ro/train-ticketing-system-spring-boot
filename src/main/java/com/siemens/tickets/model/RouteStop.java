package com.siemens.tickets.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Entity
@Table(name = "route_stops")
@Getter
@Setter
@NoArgsConstructor
public class RouteStop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "train_id", nullable = false)
    private Train train;

    @Column(nullable = false)
    private String stationName;

    @Column(nullable = false)
    private LocalTime arrival;

    @Column(nullable = false)
    private LocalTime departure;

    public RouteStop(String stationName, LocalTime arrival, LocalTime departure) {
        this.stationName = stationName;
        this.arrival = arrival;
        this.departure = departure;
    }
}