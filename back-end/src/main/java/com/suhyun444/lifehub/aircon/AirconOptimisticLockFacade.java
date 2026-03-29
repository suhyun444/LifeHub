package com.suhyun444.lifehub.aircon;

import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

@Component
public class AirconOptimisticLockFacade {

    private final AirconService airconService;

    public AirconOptimisticLockFacade(AirconService airconService) {
        this.airconService = airconService;
    }

    public AirconDto increaseTemperatureWithRetry() throws InterruptedException {
        while (true) {
            try {
                return airconService.increaseTemperature();
            } catch (ObjectOptimisticLockingFailureException e) {
                Thread.sleep(50);
            } catch (Exception e) {
                Thread.sleep(50);
            }
        }
    }
    public AirconDto decreaseTemperatureWithRetry() throws InterruptedException {
        while (true) {
            try {
                return airconService.decreaseTemperature();
            } catch (ObjectOptimisticLockingFailureException e) {
                Thread.sleep(50);
            } catch (Exception e) {
                Thread.sleep(50);
            }
        }
    }
}