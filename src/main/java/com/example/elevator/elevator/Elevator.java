package com.example.elevator.elevator;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Getter
@Setter
@Component
public class Elevator {

  @Value("${elevator.totalFloors}")
  private int totalFloors;
  @Value("${elevator.floorTimeInSeconds}")
  private int floorTimeInSeconds;
  @Value("${elevator.doorOpeningTimeInSeconds}")
  private int doorOpeningTimeInSeconds;

  private int currentFloor;
  private ElevatorState state;

  private Set<Integer> ordersUp; // Ordered by up button on floor
  private Set<Integer> ordersDown; // Ordered by down button on floor
  private Set<Integer> ordersNeutral; // Ordered by elevator buttons

  public Elevator() {
    this.currentFloor = 1;
    this.ordersUp = new HashSet<>();
    this.ordersDown = new HashSet<>();
    this.ordersNeutral = new HashSet<>();
    this.state = ElevatorState.IDLE;
  }

  public boolean shouldMove() {
    return ElevatorState.IDLE.equals(this.state) && streamAllOrders()
        .anyMatch(o -> o != this.currentFloor);
  }

  public void move() throws InterruptedException {
    if (ElevatorState.IDLE.equals(state)) {
      this.state = findNewState();
      log.info("Elevator state changed to {}", state);
    }

    // Check if door should open on current floor
    checkCurrentFloor();

    while (!ElevatorState.IDLE.equals(this.state)) {
      // Go in one direction until orders exhausted
      if (ElevatorState.GOING_UP.equals(this.state)) {
        goUp();
      }
      if (ElevatorState.GOING_DOWN.equals(this.state)) {
        goDown();
      }
      // Set state to IDLE when orders in one direction are exhausted.
      // This will trigger a new run in the scheduler.
      resetElevatorStateIfApplicable();
    }
  }

  public int getHighestOrderedStop() {
    return streamAllOrders().reduce(Integer::max).orElse(currentFloor);
  }

  public int getLowestOrderedStop() {
    return streamAllOrders().reduce(Integer::min).orElse(currentFloor);
  }

  private Stream<Integer> streamAllOrders() {
    return Stream.of(ordersUp, ordersDown, ordersNeutral)
        .flatMap(Set::stream);
  }

  private ElevatorState findNewState() {
    return streamAllOrders()
        .filter(o -> o != currentFloor)
        .findAny()
        .map(o -> o > currentFloor ? ElevatorState.GOING_UP : ElevatorState.GOING_DOWN)
        .orElse(ElevatorState.IDLE);
  }

  private void goUp() throws InterruptedException {
    Thread.sleep(floorTimeInSeconds * 1000L);
    currentFloor++;
    checkCurrentFloor();
  }

  private void goDown() throws InterruptedException {
    Thread.sleep(floorTimeInSeconds * 1000L);
    currentFloor--;
    checkCurrentFloor();
  }

  private void checkCurrentFloor() throws InterruptedException {
    log.info("Current floor: {}", currentFloor);
    boolean shouldOpenDoor = false;

    if (ordersNeutral.contains(currentFloor)) {
      shouldOpenDoor = true;
      ordersNeutral.remove(currentFloor);
    }
    if (shouldStopForOrderUp()) {
      shouldOpenDoor = true;
      ordersUp.remove(currentFloor);
    }
    if (shouldStopForOrderDown()) {
      shouldOpenDoor = true;
      ordersDown.remove(currentFloor);
    }

    if (shouldOpenDoor) {
      log.info("Opening door");
      Thread.sleep(doorOpeningTimeInSeconds * 1000L);
    }
  }

  private boolean shouldStopForOrderUp() {
    return ordersUp.contains(currentFloor)
        && (ElevatorState.GOING_UP.equals(state)
        || (ElevatorState.GOING_DOWN.equals(state) && currentFloor == getLowestOrderedStop()));
  }

  private boolean shouldStopForOrderDown() {
    return ordersDown.contains(currentFloor)
        && (ElevatorState.GOING_DOWN.equals(state)
        || (ElevatorState.GOING_UP.equals(state) && currentFloor == getHighestOrderedStop()));
  }

  private void resetElevatorStateIfApplicable() {
    if (ElevatorState.GOING_DOWN.equals(state) && currentFloor <= getLowestOrderedStop()
        || ElevatorState.GOING_UP.equals(state) && currentFloor >= getHighestOrderedStop()
        || streamAllOrders().findAny().isEmpty()) {
      this.state = ElevatorState.IDLE;
      log.info("Elevator state changed to {}", state);
    }
  }
}