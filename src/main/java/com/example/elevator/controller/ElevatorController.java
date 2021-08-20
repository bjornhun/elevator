package com.example.elevator.controller;

import com.example.elevator.elevator.ElevatorState;
import com.example.elevator.elevator.FloorState;
import com.example.elevator.service.ElevatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ElevatorController {

  private final ElevatorService service;

  @Autowired
  public ElevatorController(ElevatorService service) {
    this.service = service;
  }

  @PostMapping(value = "/elevator/{floorState}/{floor}")
  public String addOrder(@PathVariable FloorState floorState, @PathVariable Integer floor) {
    service.addDestinationFloor(floor, floorState);
    return "Success";
  }

  @PostMapping(value = "/elevator/emergencybreak")
  public String doEmergencyBreak() {
    service.doEmergencyBreak();
    return "Success";
  }

  @GetMapping(value = "/elevator/estimatedtime/{floor}")
  public String getEstimatedTimeToFloor(@PathVariable Integer floor) {
    int estimatedTimeInMilliseconds = service.getEstimatedTimeToFloorInMiliseconds(floor);
    return String.format("Estimated time to floor %s in seconds: %d", floor,
        estimatedTimeInMilliseconds / 1000);
  }

  @GetMapping(value = "/elevator/state")
  public ElevatorState getElevatorState() {
    return service.getElevatorState();
  }
}
