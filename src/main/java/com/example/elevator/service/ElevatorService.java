package com.example.elevator.service;

import com.example.elevator.elevator.Elevator;
import com.example.elevator.elevator.ElevatorState;
import com.example.elevator.elevator.OrderType;
import com.example.elevator.exception.InvalidFloorException;
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

  public void addDestinationFloor(int floor, OrderType orderType) {
    validateFloor(floor);

    log.info("Adding order: floor {}, orderType {}", floor, orderType);

    switch (orderType) {
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

  public void doEmergencyBreak() {
    elevator.setState(ElevatorState.EMERGENCY_BREAK);
    elevator.getOrdersNeutral().clear();
    elevator.getOrdersUp().clear();
    elevator.getOrdersDown().clear();
    log.info("Emergency break triggered");
  }

  public ElevatorState getElevatorState() {
    return elevator.getState();
  }

  public int getEstimatedTimeToFloor(int targetFloor) {
    validateFloor(targetFloor);

    int currentFloor = elevator.getCurrentFloor();

    if (currentFloor == targetFloor) {
      return 0;
    }

    /* Some stops may be counted twice if there are orders of different types to the same floor,
    which will give a pessimistic estimate, but this is ok as boarding passengers will likely place
    new orders which will add new stops. */
    int floorsToMove;
    int stops;

    switch (elevator.getState()) {
      case GOING_DOWN:
        if (targetFloor < currentFloor) {
          floorsToMove = Math.abs(currentFloor - targetFloor);
          stops = findStopsInRange(elevator.getOrdersNeutral(), targetFloor, currentFloor)
              + findStopsInRange(elevator.getOrdersDown(), targetFloor, currentFloor);
        } else {
          int lowestOrderedStop = elevator.getLowestOrderedStop();
          floorsToMove = Math.abs(currentFloor - lowestOrderedStop)
              + Math.abs(targetFloor - lowestOrderedStop);
          stops = findStopsInRange(elevator.getOrdersNeutral(), lowestOrderedStop, targetFloor)
              + findStopsInRange(elevator.getOrdersUp(), lowestOrderedStop, targetFloor)
              + findStopsInRange(elevator.getOrdersDown(), lowestOrderedStop, currentFloor);
        }
        break;
      case GOING_UP:
        if (targetFloor > currentFloor) {
          floorsToMove = Math.abs(targetFloor - currentFloor);
          stops = findStopsInRange(elevator.getOrdersNeutral(), currentFloor, targetFloor)
              + findStopsInRange(elevator.getOrdersUp(), currentFloor, targetFloor);
        } else {
          int highestOrderedStop = elevator.getHighestOrderedStop();
          floorsToMove = Math.abs(highestOrderedStop - currentFloor)
              + Math.abs(highestOrderedStop - targetFloor);
          stops = findStopsInRange(elevator.getOrdersNeutral(), targetFloor, highestOrderedStop)
              + findStopsInRange(elevator.getOrdersUp(), currentFloor, highestOrderedStop)
              + findStopsInRange(elevator.getOrdersDown(), targetFloor, highestOrderedStop);
        }
        break;
      default: // IDLE or EMERGENCY_BREAK
        floorsToMove = Math.abs(targetFloor - currentFloor);
        stops = 0;
        break;
    }

    return floorsToMove * elevator.getFloorTimeInSeconds()
        + stops * elevator.getDoorOpeningTimeInSeconds();
  }

  private int findStopsInRange(Set<Integer> stops, int lowerFloor, int upperFloor) {
    return (int) stops.stream()
        .filter(stop -> stop >= lowerFloor && stop <= upperFloor)
        .count();
  }

  private void validateFloor(int floor) {
    if (floor <= 0 || floor > elevator.getTotalFloors()) {
      throw new InvalidFloorException("Floor must be between 1 and " + elevator.getTotalFloors());
    }
  }
}
