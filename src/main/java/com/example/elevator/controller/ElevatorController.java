package com.example.elevator.controller;

import com.example.elevator.elevator.OrderType;
import com.example.elevator.service.ElevatorService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("elevator")
public class ElevatorController {

  private final ElevatorService service;

  @Autowired
  public ElevatorController(ElevatorService service) {
    this.service = service;
  }

  @PostMapping(value = "/{orderType}/{floor}")
  @ApiOperation(value = "Add floor to list of destinations")
  public String addOrder(@PathVariable @ApiParam(value = "Type of order") OrderType orderType,
      @PathVariable @ApiParam(value = "Chosen floor") Integer floor) {
    service.addDestinationFloor(floor, orderType);

    if (List.of(OrderType.UP, OrderType.DOWN).contains(orderType)) {
      return String.format("%s button pushed on floor %d", orderType, floor);
    }

    int estimatedTime = service.getEstimatedTimeToFloor(floor);
    return String.format("Going to floor %d, estimated time to destination: %d seconds", floor,
        estimatedTime);
  }

  @PostMapping(value = "/emergencybreak")
  @ApiOperation(value = "Activate emergency break")
  public String doEmergencyBreak() {
    service.doEmergencyBreak();
    return "Emergency break activated";
  }

  @GetMapping(value = "/state")
  @ApiOperation(value = "Get current state of elevator")
  public String getElevatorState() {
    return String.format("Current state: %s", service.getElevatorState());
  }

  @GetMapping(value = "/estimatedtime/{floor}")
  @ApiOperation(value = "Get estimated time to given floor")
  public String getEstimatedTimeToFloor(
      @PathVariable @ApiParam(value = "Target floor") Integer floor) {
    int estimatedTime = service.getEstimatedTimeToFloor(floor);
    return String.format("Estimated time to floor %s: %d seconds", floor, estimatedTime);
  }
}
