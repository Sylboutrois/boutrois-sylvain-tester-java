package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

public class FareCalculatorServiceTest {

    private static FareCalculatorService fareCalculatorService;
    private Ticket ticket;

    @BeforeAll
    private static void setUp() {
        fareCalculatorService = new FareCalculatorService();
    }

    @BeforeEach
    private void setUpPerTest() {
        ticket = new Ticket();
    }

    @Test
    public void calculateFareCar(){
    	
    	 // Initialisation des dates
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  60 * 60 * 1000) );
        Date outTime = new Date();
        
        // Création et configuration d'une place de stationnement
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        
        // Calcul du tarif et vérification
        fareCalculatorService.calculateFare(ticket, false);
        assertEquals(ticket.getPrice(), Fare.CAR_RATE_PER_HOUR);
    }

    @Test
    public void calculateFareBike(){
    	
    	 // Initialisation des dates
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  60 * 60 * 1000) );
        Date outTime = new Date();
        
        // Configuration de la place de stationnement pour un vélo
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        
        // Calcul du tarif et vérification
        fareCalculatorService.calculateFare(ticket, false);
        assertEquals(ticket.getPrice(), Fare.BIKE_RATE_PER_HOUR);
    }

    @Test
    /*
     *  Teste le comportement du calculateur de tarifs lorsqu'un type de
     *  véhicule inconnu est passé (null). On attend une exception NullPointerException.
     */
    public void calculateFareUnkownType(){
    	// Initialisation des dates
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  60 * 60 * 1000) );
        Date outTime = new Date();
        
        // Création d'une place de stationnement avec un type inconnu
        ParkingSpot parkingSpot = new ParkingSpot(1, null,false);
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        
        // Vérification que l'exception est lancée
        assertThrows(NullPointerException.class, () -> fareCalculatorService.calculateFare(ticket, false));
    }

    @Test
    /* Ce test vérifie que l'exception IllegalArgumentException 
     * est levée si l'heure d'entrée est dans le futur. */
    
    public void calculateFareBikeWithFutureInTime(){
    	
    	// Initialisation des dates
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() + (  60 * 60 * 1000) );
        Date outTime = new Date();
        
        // Configuration de la place de stationnement pour un vélo
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        
        // Vérification que l'exception est lancée
        assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFare(ticket, false));
    }

    @Test
    /* Teste le calcul du tarif pour un vélo garé pendant 45 minutes. 
     * On s'attend à 75% du tarif horaire pour les vélos
     */
    public void calculateFareBikeWithLessThanOneHourParkingTime(){
    	
    	// Initialisation des dates
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  45 * 60 * 1000) );//45 minutes parking time should give 3/4th parking fare
        Date outTime = new Date();
        
        // Configuration de la place de stationnement pour un vélo	
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        
        // Calcul du tarif et vérification
        fareCalculatorService.calculateFare(ticket, false);
        assertEquals((0.75 * Fare.BIKE_RATE_PER_HOUR), ticket.getPrice() );
    }

    @Test
    /*
     * Teste le calcul du tarif pour une voiture garée pendant 45 minutes.
     */
    public void calculateFareCarWithLessThanOneHourParkingTime(){
    	
    	// Initialisation des dates
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  45 * 60 * 1000) );//45 minutes parking time should give 3/4th parking fare
        Date outTime = new Date();
        
        // Configuration de la place de stationnement pour une voiture
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        
        // Calcul du tarif et vérification
        fareCalculatorService.calculateFare(ticket, false);
        assertEquals( (0.75 * Fare.CAR_RATE_PER_HOUR) , ticket.getPrice());
    }

    @Test
    /*
     * Teste le calcul du tarif pour une voiture garée pendant 24 heures.
     *  Le tarif total doit être 24 fois le tarif horaire pour les voitures.
     */
    public void calculateFareCarWithMoreThanADayParkingTime(){
    	
    	// Initialisation des dates
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  24 * 60 * 60 * 1000) );//24 hours parking time should give 24 * parking fare per hour
        Date outTime = new Date();
        
        // Configuration de la place de stationnement pour une voiture
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        
        // Calcul du tarif et vérification
        fareCalculatorService.calculateFare(ticket, false);
        assertEquals( (24 * Fare.CAR_RATE_PER_HOUR) , ticket.getPrice());
    }
    @Test
    public void calculateFareCarWithLessThan30minutesParkingTime() {
        // Initialisation des dates d'entrée et de sortie
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (20 * 60 * 1000)); // 20 minutes avant maintenant
        Date outTime = new Date();

        // Création d'un parking spot pour une voiture
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        // Configuration du ticket
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        // Calcul des frais
        fareCalculatorService.calculateFare(ticket, false);

        // Vérification que le prix est de 0
        assertEquals(0, ticket.getPrice());
    }
    @Test
    public void calculateFareBikeWithLessThan30minutesParkingTime() {
        // Initialisation des dates d'entrée et de sortie
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (20 * 60 * 1000)); // 20 minutes avant maintenant
        Date outTime = new Date();

        // Création d'un parking spot pour une moto
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

        // Configuration du ticket
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        // Calcul des frais
        fareCalculatorService.calculateFare(ticket, false);

        // Vérification que le prix est de 0
        assertEquals(0, ticket.getPrice());
    }


@Test
public void calculateFareCarWithDiscount() {
    // Initialisation des dates d'entrée et de sortie
    Date inTime = new Date();
    inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000)); // 1 heure avant maintenant
    Date outTime = new Date();

    // Création d'un parking spot pour une voiture
    ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

    // Configuration du ticket
    ticket.setInTime(inTime);
    ticket.setOutTime(outTime);
    ticket.setParkingSpot(parkingSpot);

    // Appliquer une réduction
    boolean discount = true;

    // Calcul des frais
    fareCalculatorService.calculateFare(ticket, discount);

    // Vérification que le prix est de 95% du tarif plein
    double expectedPrice = Fare.CAR_RATE_PER_HOUR * 0.95;
    assertEquals(expectedPrice, ticket.getPrice());
}
@Test
public void calculateFareBikeWithDiscount() {
    // Initialisation des dates d'entrée et de sortie
    Date inTime = new Date();
    inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000)); // 1 heure avant maintenant
    Date outTime = new Date();

    // Création d'un parking spot pour une moto
    ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

    // Configuration du ticket
    ticket.setInTime(inTime);
    ticket.setOutTime(outTime);
    ticket.setParkingSpot(parkingSpot);

    // Appliquer une réduction
    boolean discount = true;

    // Calcul des frais
    fareCalculatorService.calculateFare(ticket, discount);

    // Vérification que le prix est de 95% du tarif plein
    double expectedPrice = Fare.BIKE_RATE_PER_HOUR * 0.95;
    assertEquals(expectedPrice, ticket.getPrice());
}

}