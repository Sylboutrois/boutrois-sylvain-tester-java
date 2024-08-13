package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket, boolean discount){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        long inHour = ticket.getInTime().getTime();
        long outHour = ticket.getOutTime().getTime();

     // Conversion de la durée de stationnement en heures (décimales)
        double duration = (outHour - inHour)/ (1000.0 * 60.0 * 60.0);
        
        
        if (duration < 0.5) {
            ticket.setPrice(0);
            return;
        }
        double price;

        switch (ticket.getParkingSpot().getParkingType()) {
            case CAR: {
                price = duration * Fare.CAR_RATE_PER_HOUR;
                break;
            }
            case BIKE: {
                price = duration * Fare.BIKE_RATE_PER_HOUR;
                break;
            }
            default:
                throw new IllegalArgumentException("Unknown Parking Type");
        }

        // Appliquer la réduction de 5% si le paramètre discount est à true
        if (discount) {
            price *= 0.95;
        }

        ticket.setPrice(price);}
}
    