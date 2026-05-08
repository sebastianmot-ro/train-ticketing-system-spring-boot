package com.siemens.tickets.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "bookings")
@Getter
@NoArgsConstructor
public class Booking {

    @Id
    @Column(nullable = false, unique = true)
    private String bookingId;

    @Column(nullable = false)
    private String trainId;

    @Column(nullable = false)
    private LocalDate travelDate;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private int seats;

    public Booking(String bookingId, String trainId, LocalDate travelDate, String email, int seats) {
        this.bookingId = bookingId;
        this.trainId = trainId;
        this.travelDate = travelDate;
        this.email = email;
        this.seats = seats;
    }

    @Override
    public String toString() {
        return bookingId + " | train " + trainId + " | " + travelDate + " | seats " + seats + " | " + email;
    }
}