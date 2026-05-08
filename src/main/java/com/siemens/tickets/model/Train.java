package com.siemens.tickets.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trains")
@Getter
@Setter
@NoArgsConstructor
public class Train {

    @Id
    @Column(nullable = false, unique = true)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int capacity;

    @OneToMany(mappedBy = "train", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("id ASC")
    private List<RouteStop> stops = new ArrayList<>();

    public Train(String id, String name, int capacity, List<RouteStop> stops) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        setStops(stops);
    }

    public void setStops(List<RouteStop> stops) {
        this.stops.clear();
        stops.forEach(s -> s.setTrain(this));
        this.stops.addAll(stops);
    }

    @Override
    public String toString() {
        return id + " - " + name + " (cap " + capacity + ")";
    }
}