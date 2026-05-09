package com.siemens.tickets.service;

import com.siemens.tickets.model.*;
import com.siemens.tickets.repository.BookingRepository;
import com.siemens.tickets.repository.TrainRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class TrainService {

    private final TrainRepository trainRepository;
    private final BookingRepository bookingRepository;
    private final EmailService emailService;

    public List<Train> allTrains() { return trainRepository.findAll(); }
    public List<Booking> allBookings() { return bookingRepository.findAll(); }
    public void saveTrain(Train train) { trainRepository.save(train); }
    public void deleteTrain(String id) { trainRepository.deleteById(id); }

    public List<String> allStations() {
        return trainRepository.findAll().stream()
                .flatMap(t -> t.getStops().stream())
                .map(RouteStop::getStationName)
                .distinct()
                .sorted()
                .toList();
    }

    public Booking book(String trainId, LocalDate date, String email, int seats) {
        if (date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Travel date cannot be in the past.");
        }
        if (seats < 1 || seats > 20) {
            throw new IllegalArgumentException("Number of seats must be between 1 and 20.");
        }

        Train train = trainRepository.findById(trainId)
                .orElseThrow(() -> new IllegalArgumentException("Train not found: " + trainId));

        int usedSeats = bookingRepository.findByTrainIdAndTravelDate(trainId, date)
                .stream().mapToInt(Booking::getSeats).sum();

        if (usedSeats + seats > train.getCapacity())
            throw new IllegalStateException("Overbooking blocked. Remaining seats: " + (train.getCapacity() - usedSeats));

        Booking booking = new Booking(
                UUID.randomUUID().toString().substring(0, 8),
                trainId, date, email, seats
        );
        bookingRepository.save(booking);
        try {
            emailService.send(email, "Booking confirmation",
                    "Booking " + booking.getBookingId() + " confirmed for train " + trainId + " on " + date);
        } catch (Exception e) {
            System.err.println("Email failed: " + e.getMessage());
        }
        return booking;
    }

    public List<ItineraryLeg> findBestPath(String from, String to) {
        List<ItineraryLeg> edges = buildEdges();
        Map<String, List<ItineraryLeg>> bySource = edges.stream()
                .collect(Collectors.groupingBy(ItineraryLeg::fromStation));

        Queue<List<ItineraryLeg>> q = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();

        for (ItineraryLeg leg : bySource.getOrDefault(from, List.of()))
            q.add(new ArrayList<>(List.of(leg)));

        while (!q.isEmpty()) {
            List<ItineraryLeg> path = q.poll();
            ItineraryLeg last = path.get(path.size() - 1);

            if (last.toStation().equals(to)) return path;

            String state = last.toStation() + "@" + last.arrive();
            if (!visited.add(state)) continue;

            for (ItineraryLeg next : bySource.getOrDefault(last.toStation(), List.of())) {
                if (!next.depart().isBefore(last.arrive())) {
                    List<ItineraryLeg> extended = new ArrayList<>(path);
                    extended.add(next);
                    q.add(extended);
                }
            }
        }
        throw new IllegalArgumentException("No path found between " + from + " and " + to);
    }

    private List<ItineraryLeg> buildEdges() {
        List<ItineraryLeg> edges = new ArrayList<>();
        for (Train train : trainRepository.findAll()) {
            List<RouteStop> s = train.getStops();
            for (int i = 0; i < s.size() - 1; i++) {
                for (int j = i + 1; j < s.size(); j++) {
                    LocalTime dep = s.get(i).getDeparture();
                    LocalTime arr = s.get(j).getArrival();
                    if (arr.isAfter(dep)) {
                        edges.add(new ItineraryLeg(
                                train.getId(),
                                s.get(i).getStationName(),
                                s.get(j).getStationName(),
                                dep, arr
                        ));
                    }
                }
            }
        }
        return edges;
    }

    public void notifyDelay(String trainId, String delayText) {
        bookingRepository.findByTrainId(trainId).stream()
                .map(Booking::getEmail)
                .distinct()
                .forEach(email -> emailService.send(email, "Train delay notice",
                        "Train " + trainId + " is delayed: " + delayText));
    }
}