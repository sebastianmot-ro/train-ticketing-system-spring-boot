package com.siemens.tickets.model;

import java.time.LocalTime;

public record ItineraryLeg(String trainId, String fromStation, String toStation, LocalTime depart, LocalTime arrive) {
    @Override
    public String toString() {
        return trainId + ": " + fromStation + " " + depart + " -> " + toStation + " " + arrive;
    }
}