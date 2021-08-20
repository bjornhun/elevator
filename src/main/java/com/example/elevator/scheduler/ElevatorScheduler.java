package com.example.elevator.scheduler;

import com.example.elevator.elevator.Elevator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ElevatorScheduler {

  private final Elevator elevator;

  @Autowired
  public ElevatorScheduler(Elevator elevator) {
    this.elevator = elevator;
  }

  @Scheduled(fixedDelay = 100)
  public void scheduleElevator() throws InterruptedException {
    if (elevator.shouldMove()) {
      elevator.move();
    }
  }
}
