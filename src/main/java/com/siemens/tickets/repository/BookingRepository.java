package com.siemens.tickets.repository;

import com.siemens.tickets.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, String> {
    List<Booking> findByTrainIdAndTravelDate(String trainId, LocalDate travelDate);
    List<Booking> findByTrainId(String trainId);
}