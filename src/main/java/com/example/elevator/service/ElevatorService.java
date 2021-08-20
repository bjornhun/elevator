package com.example.elevator.service;

import static com.example.elevator.elevator.Elevator.DOOR_OPENING_TIME_IN_MILLISECONDS;
import static com.example.elevator.elevator.Elevator.FLOOR_TIME_IN_MILLISECONDS;
import static com.example.elevator.elevator.Elevator.TOTAL_FLOORS;

import com.example.elevator.elevator.Elevator;
import com.example.elevator.elevator.ElevatorState;
import com.example.elevator.elevator.FloorState;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ElevatorService {

  private final Elevator elevator;

  @Autowired
  public ElevatorService(Elevator elevator) {
    this.elevator = elevator;
  }

  public void doEmergencyBreak() {
    elevator.setState(ElevatorState.EMERGENCY_BREAK);
    elevator.getOrdersNeutral().clear();
    elevator.getOrdersUp().clear();
    elevator.getOrdersDown().clear();
    log.info("Current floor: {}, emergency break triggered", elevator.getCurrentFloor());
  }

  public void addDestinationFloor(int floor, FloorState state) {
    validateFloor(floor);

    switch (state) {
      case NEUTRAL:
        elevator.getOrdersNeutral().add(floor);
        break;
      case UP:
        elevator.getOrdersUp().add(floor);
        break;
      case DOWN:
        elevator.getOrdersDown().add(floor);
        break;
      default:
        break;
    }
  }

  public ElevatorState getElevatorState() {
    return elevator.getState();
  }

  public int getEstimatedTimeToFloorInMiliseconds(int targetFloor) {
    validateFloor(targetFloor);

    int currentFloor = elevator.getCurrentFloor();
    ElevatorState currentState = elevator.getState();

    int floorsToMove = 0;
    int stops = 0;

    if (ElevatorState.IDLE.equals(currentState) || ElevatorState.EMERGENCY_BREAK
        .equals(currentState)) {
      floorsToMove = Math.abs(targetFloor - currentFloor);
    } else if (targetFloor > currentFloor) {
      if (ElevatorState.GOING_DOWN.equals(currentState)) {
        int lowestOrderedStop = elevator.getLowestOrderedStop();
        floorsToMove =
            Math.abs(currentFloor - lowestOrderedStop) + Math.abs(targetFloor - lowestOrderedStop);
        stops += findStopsInRange(elevator.getOrdersNeutral(), lowestOrderedStop, targetFloor);
        stops += findStopsInRange(elevator.getOrdersUp(), lowestOrderedStop, targetFloor);
        stops += findStopsInRange(elevator.getOrdersDown(), lowestOrderedStop, targetFloor);
      } else if (ElevatorState.GOING_UP.equals(currentState)) {
        floorsToMove = Math.abs(targetFloor - currentFloor);
        stops += findStopsInRange(elevator.getOrdersNeutral(), currentFloor, targetFloor);
        stops += findStopsInRange(elevator.getOrdersUp(), currentFloor, targetFloor);
      }
    } else if (targetFloor < currentFloor) {
      if (ElevatorState.GOING_UP.equals(currentState)) {
        int highestOrderedStop = elevator.getHighestOrderedStop();
        floorsToMove = Math.abs(highestOrderedStop - currentFloor) + Math
            .abs(highestOrderedStop - targetFloor);
        stops += findStopsInRange(elevator.getOrdersNeutral(), highestOrderedStop, targetFloor);
        stops += findStopsInRange(elevator.getOrdersUp(), highestOrderedStop, targetFloor);
        stops += findStopsInRange(elevator.getOrdersDown(), highestOrderedStop, targetFloor);
      } else if (ElevatorState.GOING_DOWN.equals(currentState)) {
        floorsToMove = Math.abs(currentFloor - targetFloor);
        stops += findStopsInRange(elevator.getOrdersNeutral(), currentFloor, targetFloor);
        stops += findStopsInRange(elevator.getOrdersDown(), currentFloor, targetFloor);
      }
    }

    return floorsToMove * FLOOR_TIME_IN_MILLISECONDS + stops * DOOR_OPENING_TIME_IN_MILLISECONDS;
  }

  private int findStopsInRange(Set<Integer> stops, int lowerFloor, int upperFloor) {
    return (int) stops.stream()
        .filter(stop -> stop > lowerFloor && stop < upperFloor)
        .count();
  }

  private void validateFloor(int floor) {
    if (floor <= 0 || floor > TOTAL_FLOORS) {
      throw new IllegalArgumentException("Floor must be between 1 and " + TOTAL_FLOORS);
    }
  }
}
