package com.siemens.tickets.controller;

import com.siemens.tickets.model.*;
import com.siemens.tickets.service.TrainService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {

    private final TrainService trainService;

    // ── Stations ──────────────────────────────────────────────────────────────

    @GetMapping("/stations")
    public List<String> allStations() {
        return trainService.allStations();
    }

    // ── Trains ────────────────────────────────────────────────────────────────

    @GetMapping("/trains")
    public List<Train> allTrains() {
        return trainService.allTrains();
    }

    @PostMapping("/trains")
    public ResponseEntity<?> saveTrain(@RequestBody TrainRequest req) {
        try {
            List<RouteStop> stops = req.stops().stream()
                    .map(s -> new RouteStop(s.stationName(), s.arrival(), s.departure()))
                    .toList();
            trainService.saveTrain(new Train(req.id(), req.name(), req.capacity(), stops));
            return ResponseEntity.ok(Map.of("message", "Train saved: " + req.id()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/trains/{id}")
    public ResponseEntity<?> deleteTrain(@PathVariable String id) {
        trainService.deleteTrain(id);
        return ResponseEntity.ok(Map.of("message", "Deleted: " + id));
    }

    // ── Bookings ──────────────────────────────────────────────────────────────

    @GetMapping("/bookings")
    public List<Booking> allBookings() {
        return trainService.allBookings();
    }

    @PostMapping("/bookings")
    public ResponseEntity<?> book(@RequestBody BookingRequest req) {
        try {
            return ResponseEntity.ok(trainService.book(req.trainId(), req.date(), req.email(), req.seats()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── Path ──────────────────────────────────────────────────────────────────

    @GetMapping("/path")
    public ResponseEntity<?> findPath(@RequestParam String from, @RequestParam String to) {
        try {
            return ResponseEntity.ok(trainService.findBestPath(from, to));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── Delays ────────────────────────────────────────────────────────────────

    @PostMapping("/trains/{id}/delay")
    public ResponseEntity<?> notifyDelay(@PathVariable String id, @RequestBody Map<String, String> body) {
        trainService.notifyDelay(id, body.get("delayText"));
        return ResponseEntity.ok(Map.of("message", "Delay notifications sent."));
    }

    // ── DTOs ──────────────────────────────────────────────────────────────────

    record TrainRequest(String id, String name, int capacity, List<RouteStopRequest> stops) {}
    record RouteStopRequest(String stationName, LocalTime arrival, LocalTime departure) {}
    record BookingRequest(String trainId, LocalDate date, String email, int seats) {}
}