package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
//import com.parkit.parkingsystem.service.FareCalculatorService;
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

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;
   // private static FareCalculatorService fareCalculatorService;
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

        // Vérifiez que le ticket est enregistré dans la base de données
        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        System.out.println("Ticket retrieved from DB: " + ticket); // Debug log

        assertNotNull(ticket, "Ticket should be saved in the database");
        assertEquals("ABCDEF", ticket.getVehicleRegNumber(), "Vehicle registration number should match");

        // Vérifiez que l'emplacement de stationnement est mis à jour avec la disponibilité
        ParkingSpot parkingSpot = ticket.getParkingSpot();
        System.out.println("Parking spot retrieved from ticket: " + parkingSpot); // Debug log
        assertFalse(parkingSpot.isAvailable(), "Parking spot should be marked as not available");
    }

    @Test
    public void testParkingLotExit() {
        testParkingACar(); // Park a car first
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processExitingVehicle();

        // TODO: check that the fare generated and out time are populated correctly in the database
        // Retrieve ticket from the database again to check updates
        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        assertNotNull(ticket, "Ticket should be present in the database after exiting");
        assertNotNull(ticket.getOutTime(), "Out time should be populated in the database");
        assertTrue(ticket.getPrice() > 0, "Fare should be calculated and greater than zero");
    }
}



