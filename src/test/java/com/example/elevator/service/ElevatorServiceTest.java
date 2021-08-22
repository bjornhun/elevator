package com.example.elevator.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.elevator.elevator.Elevator;
import com.example.elevator.elevator.ElevatorState;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ElevatorServiceTest {

  @Mock
  Elevator elevator;

  @InjectMocks
  ElevatorService service;

  private static final int FLOOR_TIME = 1;
  private static final int DOOR_OPENING_TIME = 3;

  @BeforeEach
  void setUp() {
    Mockito.when(elevator.getFloorTimeInSeconds()).thenReturn(FLOOR_TIME);
    Mockito.when(elevator.getDoorOpeningTimeInSeconds()).thenReturn(DOOR_OPENING_TIME);
    Mockito.when(elevator.getTotalFloors()).thenReturn(10);
  }

  @Test
  void getEstimatedFloorTimeInSeconds_givenIdle() {
    Mockito.when(elevator.getCurrentFloor()).thenReturn(1);
    Mockito.when(elevator.getState()).thenReturn(ElevatorState.IDLE);

    int estimatedTime = service.getEstimatedTimeToFloor(10);

    assertThat(estimatedTime).isEqualTo(9 * FLOOR_TIME);

  }

  @Test
  void getEstimatedFloorTimeInSeconds_givenGoingUp() {
    Mockito.when(elevator.getCurrentFloor()).thenReturn(1);
    Mockito.when(elevator.getState()).thenReturn(ElevatorState.GOING_UP);
    Mockito.when(elevator.getOrdersUp()).thenReturn(new HashSet<>());
    Mockito.when(elevator.getOrdersNeutral()).thenReturn(new HashSet<>());

    int estimatedTime = service.getEstimatedTimeToFloor(10);

    assertThat(estimatedTime).isEqualTo(9 * FLOOR_TIME);
  }

  @Test
  void getEstimatedFloorTimeInSeconds_givenGoingUpThenDown() {
    Mockito.when(elevator.getCurrentFloor()).thenReturn(6);
    Mockito.when(elevator.getState()).thenReturn(ElevatorState.GOING_UP);
    Mockito.when(elevator.getOrdersUp()).thenReturn(new HashSet<>(Set.of(2,3,7)));
    Mockito.when(elevator.getOrdersDown()).thenReturn(new HashSet<>(Set.of(3,5)));
    Mockito.when(elevator.getOrdersNeutral()).thenReturn(new HashSet<>(Set.of(3,4,5)));
    Mockito.when(elevator.getHighestOrderedStop()).thenReturn(7);


    int estimatedTime = service.getEstimatedTimeToFloor(2);

    // Will move one floor up stopping once, and then five floors down stopping five times
    assertThat(estimatedTime).isEqualTo(6 * FLOOR_TIME + 6 * DOOR_OPENING_TIME);
  }

  @Test
  void getEstimatedFloorTimeInSeconds_givenGoingDown() {
    Mockito.when(elevator.getCurrentFloor()).thenReturn(10);
    Mockito.when(elevator.getState()).thenReturn(ElevatorState.GOING_DOWN);
    Mockito.when(elevator.getOrdersDown()).thenReturn(new HashSet<>());
    Mockito.when(elevator.getOrdersNeutral()).thenReturn(new HashSet<>());

    int estimatedTime = service.getEstimatedTimeToFloor(1);

    assertThat(estimatedTime).isEqualTo(9 * FLOOR_TIME);
  }

  @Test
  void getEstimatedFloorTimeInSeconds_givenGoingDownThenUp() {
    Mockito.when(elevator.getCurrentFloor()).thenReturn(6);
    Mockito.when(elevator.getState()).thenReturn(ElevatorState.GOING_DOWN);
    Mockito.when(elevator.getOrdersUp()).thenReturn(new HashSet<>(Set.of(2,3,5)));
    Mockito.when(elevator.getOrdersDown()).thenReturn(new HashSet<>(Set.of(3,7)));
    Mockito.when(elevator.getOrdersNeutral()).thenReturn(new HashSet<>(Set.of(3,4,5)));
    Mockito.when(elevator.getLowestOrderedStop()).thenReturn(2);


    int estimatedTime = service.getEstimatedTimeToFloor(8);

    // Will move four floors down stopping four times, and then six floors up stopping three times
    assertThat(estimatedTime).isEqualTo(10 * FLOOR_TIME + 7 * DOOR_OPENING_TIME);
  }

}