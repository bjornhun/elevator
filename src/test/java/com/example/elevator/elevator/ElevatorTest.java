package com.example.elevator.elevator;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import org.assertj.core.api.SoftAssertions;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ElevatorTest {

  @Autowired
  Elevator elevator;

  @BeforeEach
  void setUp() {
    elevator.setState(ElevatorState.IDLE);
    elevator.setOrdersUp(new HashSet<>(Set.of(1, 5, 8)));
    elevator.setOrdersDown(new HashSet<>(Set.of(3, 5, 10)));
    elevator.setOrdersNeutral(new HashSet<>(Set.of(2, 3, 8)));
  }

  @Test
  void moveFromFirstFloorWithManyStops() throws InterruptedException {
    elevator.setCurrentFloor(1);

    int lowestOrderGoingDown = elevator.getOrdersDown().stream()
        .reduce(Integer::min)
        .orElseThrow(RuntimeException::new);

    elevator.move();

    Awaitility.await().atMost(Duration.ofSeconds(1))
        .pollDelay(Duration.ofMillis(100))
        .until(() -> ElevatorState.IDLE.equals(elevator.getState()));

    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(elevator.getCurrentFloor()).isEqualTo(lowestOrderGoingDown);
    softly.assertThat(elevator.getOrdersDown()).isEmpty();
    softly.assertThat(elevator.getOrdersUp()).isEmpty();
    softly.assertThat(elevator.getOrdersNeutral()).isEmpty();
    softly.assertAll();
  }

  @Test
  void moveFromTopFloorWithManyStops() throws InterruptedException {
    elevator.setCurrentFloor(10);

    int highestOrderGoingUp = elevator.getOrdersUp().stream()
        .reduce(Integer::max)
        .orElseThrow(RuntimeException::new);

    elevator.move();

    Awaitility.await().atMost(Duration.ofSeconds(highestOrderGoingUp))
        .pollDelay(Duration.ofMillis(100))
        .until(() -> ElevatorState.IDLE.equals(elevator.getState()));

    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(elevator.getCurrentFloor()).isEqualTo(highestOrderGoingUp);
    softly.assertThat(elevator.getOrdersDown()).isEmpty();
    softly.assertThat(elevator.getOrdersUp()).isEmpty();
    softly.assertThat(elevator.getOrdersNeutral()).isEmpty();
    softly.assertAll();
  }

  @Test
  void moveFromMiddleFloorWithManyStopsAndGoingUp() throws InterruptedException {
    elevator.setCurrentFloor(5);
    elevator.setState(ElevatorState.GOING_UP);

    int highestOrderUpUnderCurrentFloor = elevator.getOrdersUp().stream()
        .filter(o -> o < elevator.getCurrentFloor())
        .reduce(Integer::max)
        .orElseThrow(RuntimeException::new);

    elevator.move();

    Awaitility.await().atMost(Duration.ofSeconds(1))
        .pollDelay(Duration.ofMillis(100))
        .until(() -> ElevatorState.IDLE.equals(elevator.getState()));

    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(elevator.getCurrentFloor()).isEqualTo(highestOrderUpUnderCurrentFloor);
    softly.assertThat(elevator.getOrdersDown()).isEmpty();
    softly.assertThat(elevator.getOrdersUp()).isEmpty();
    softly.assertThat(elevator.getOrdersNeutral()).isEmpty();
    softly.assertAll();
  }

  @Test
  void moveFromMiddleFloorWithManyStopsAndGoingDown() throws InterruptedException {
    elevator.setCurrentFloor(5);
    elevator.setState(ElevatorState.GOING_DOWN);

    int lowestOrderGoingDownOverCurrentFloor = elevator.getOrdersDown().stream()
        .filter(o -> o > elevator.getCurrentFloor())
        .reduce(Integer::min)
        .orElseThrow(RuntimeException::new);

    elevator.move();

    Awaitility.await().atMost(Duration.ofSeconds(1))
        .pollDelay(Duration.ofMillis(100))
        .until(() -> ElevatorState.IDLE.equals(elevator.getState()));

    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(elevator.getCurrentFloor()).isEqualTo(lowestOrderGoingDownOverCurrentFloor);
    softly.assertThat(elevator.getOrdersDown()).isEmpty();
    softly.assertThat(elevator.getOrdersUp()).isEmpty();
    softly.assertThat(elevator.getOrdersNeutral()).isEmpty();
    softly.assertAll();
  }
}