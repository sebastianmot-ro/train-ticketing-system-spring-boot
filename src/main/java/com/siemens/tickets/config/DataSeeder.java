package com.siemens.tickets.config;

import com.siemens.tickets.model.RouteStop;
import com.siemens.tickets.model.Train;
import com.siemens.tickets.service.TrainService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final TrainService trainService;

    @Override
    public void run(String... args) {
        if (!trainService.allTrains().isEmpty()) return;

        trainService.saveTrain(new Train("ICE100", "Northern Arrow", 250, List.of(
                new RouteStop("Berlin", LocalTime.of(8, 0), LocalTime.of(8, 5)),
                new RouteStop("Leipzig", LocalTime.of(9, 20), LocalTime.of(9, 22)),
                new RouteStop("Munich", LocalTime.of(12, 10), LocalTime.of(12, 10))
        )));

        trainService.saveTrain(new Train("IC200", "West Connector", 180, List.of(
                new RouteStop("Leipzig", LocalTime.of(9, 35), LocalTime.of(9, 40)),
                new RouteStop("Frankfurt", LocalTime.of(11, 30), LocalTime.of(11, 35)),
                new RouteStop("Cologne", LocalTime.of(13, 0), LocalTime.of(13, 0))
        )));
    }
}