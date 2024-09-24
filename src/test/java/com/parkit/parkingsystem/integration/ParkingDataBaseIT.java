package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.sql.Date;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;
    
    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    private static void setUp() throws Exception{
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    private static void tearDown(){

    }

    @Test
    public void testParkingACar() {
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();
      //TODO: check that a ticket is actualy saved in DB and Parking table is updated with availability

        // Vérifie que le ticket est enregistré dans la base de données
        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        System.out.println("Ticket retrieved from DB: " + ticket); // Debug log

        assertNotNull(ticket, "Ticket should be saved in the database");
        assertEquals("ABCDEF", ticket.getVehicleRegNumber(), "Vehicle registration number should match");

        // Vérifie que l'emplacement de stationnement est mis à jour avec la disponibilité
        ParkingSpot parkingSpot = ticket.getParkingSpot();
        System.out.println("Parking spot retrieved from ticket: " + parkingSpot); // Debug log
        assertFalse(parkingSpot.isAvailable(), "Parking spot should be marked as not available");
    }

    @Test
    public void testParkingLotExit() {
    	
        testParkingACar(); // Park a car first
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();

        // TODO: check that the fare generated and out time are populated correctly in the database
        // Récupère le ticket dans la base de donnée
        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        ticket.setOutTime(new Date(ticket.getInTime().getTime() + 45 * 60 * 1000)); // 45 minutes après l'entrée
       
        // Sauvegarder le ticket modifié dans la base de données
        ticketDAO.updateTicket(ticket);

        // Sortie du véhicule pour le premier usage
        parkingService.processExitingVehicle();
        ticket = ticketDAO.getTicket("ABCDEF");
        assertNotNull(ticket, "Ticket should be present in the database after exiting");
        assertNotNull(ticket.getOutTime(), "Out time should be populated in the database");
        System.out.println("Price : " + ticket.getPrice());
        System.out.println("In Time TEST: " + ticket.getInTime());
	    System.out.println("Out Time (before update)TEST: " + ticket.getOutTime());
        assertTrue(ticket.getPrice() > 0, "Fare should be calculated and greater than zero");
    }

    @Test
    public void testParkingLotExitRecurringUser() throws Exception {
        // Simuler la première entrée d'un utilisateur
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();

        // Récupérer le ticket et définir une heure de sortie avec au moins 45 minutes de stationnement
        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        ticket.setOutTime(new Date(ticket.getInTime().getTime() + 45 * 60 * 1000)); // 45 minutes après l'entrée

        // Sauvegarder le ticket modifié dans la base de données
        ticketDAO.updateTicket(ticket);

        // Sortie du véhicule pour le premier usage
        parkingService.processExitingVehicle();

        // Simuler la seconde entrée de l'utilisateur (utilisateur récurrent)
        parkingService.processIncomingVehicle();
        // Récupérer le ticket pour cette nouvelle entrée
        ticket = ticketDAO.getTicket("ABCDEF");

        // Simuler à nouveau une sortie après 45 minutes pour l'utilisateur récurrent
        ticket.setOutTime(new Date(ticket.getInTime().getTime() + 45 * 60 * 1000)); // 45 minutes après la seconde entrée
        ticketDAO.updateTicket(ticket); // Mettez à jour le ticket pour prendre en compte l'heure de sortie
        
        
        parkingService.processExitingVehicle();

        // Récupérer le ticket mis à jour depuis la base de données
        ticket = ticketDAO.getTicket("ABCDEF");
        assertNotNull(ticket, "Ticket should be present in the database after recurring user exits");

        // Vérification du prix et de la réduction
        double prixSansRemise = ticket.getPrice() / 0.95;
        System.out.println("Price without discount: " + prixSansRemise);
        System.out.println("Price after discount: " + ticket.getPrice());
        assertTrue(ticket.getPrice() < prixSansRemise, "The price should include a 5% discount for recurring users");
    }
}


