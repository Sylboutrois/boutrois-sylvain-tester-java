
package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import com.parkit.parkingsystem.service.ParkingService;

import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

	private static ParkingService parkingService;

	@Mock
	private static InputReaderUtil inputReaderUtil;
	@Mock
	private static ParkingSpotDAO parkingSpotDAO;
	@Mock
	private static TicketDAO ticketDAO;
	// @Mock
	//  private FareCalculatorService fareCalculatorService;

	@BeforeEach
	private void setUpPerTest() {
		try {
			lenient().when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");

			ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
			Ticket ticket = new Ticket();
			ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));
			ticket.setParkingSpot(parkingSpot);
			ticket.setVehicleRegNumber("ABCDEF");


			lenient().when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
			lenient().when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
			lenient().when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

			// Initialiser le service de parking avec des mocks
			parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);


		} catch (Exception e) {
			e.printStackTrace();
			throw  new RuntimeException("Failed to set up test mock objects");
		}
	}

	@Test
	public void processExitingVehicleTest() {
		
		// Arrange: Configuration des mocks nécessaires
		when(ticketDAO.getNbTicket("ABCDEF")).thenReturn(2);  // Indique un véhicule récurrent

		// Act: Appel de la méthode sous test
		parkingService.processExitingVehicle(); // Exécuter la méthode à tester

		// Assert: Vérification des interactions avec les mocks
		verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));// Vérifier que la méthode updateParking a été appelée une fois
		verify(ticketDAO, times(1)).updateTicket(any(Ticket.class)); // Vérifier que la méthode updateTicket a été appelée une fois
		verify(ticketDAO, times(1)).getNbTicket("ABCDEF"); // Vérifier que la méthode getNbTicket a été appelée une fois
	}
	
	
	@Test
	public void testGetNextParkingNumberIfAvailable() {
		// Arrange: Configuration des mocks nécessaires
		when(inputReaderUtil.readSelection()).thenReturn(1);
		when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
		
		// Act: Appel de la méthode sous test
		parkingService.getNextParkingNumberIfAvailable();
		
		// Assert: Vérification des interactions avec les mocks
		verify(inputReaderUtil, Mockito.times(1)).readSelection();
		verify(parkingSpotDAO, Mockito.times(1)).getNextAvailableSlot(any(ParkingType.class));
	};
/*	@Test

	public void testGetNextParkingNumberIfAvailableException() {
		// Arrange: Configuration des mocks nécessaires
		when(inputReaderUtil.readSelection()).thenReturn();
		when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn();
		
		// Act: Appel de la méthode sous test
		
		// Assert: Vérification des interactions avec les mocks
		 assertThrows(Exception.class, () -> {
	            parkingService.getNextParkingNumberIfAvailable();
	        });}
	*/

@Test
	public void testProcessIncomingVehicle() {
	    try {
	        // Arrange: Configuration des mocks nécessaires
	        when(inputReaderUtil.readSelection()).thenReturn(1);  // Simule la sélection de type de véhicule (1 = CAR)
	        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF"); // Simule l'entrée du numéro d'immatriculation du véhicule
	        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1); // Simule la disponibilité d'un emplacement de stationnement
	        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true); // Simule la mise à jour de l'emplacement de stationnement
	        when(ticketDAO.getNbTicket(anyString())).thenReturn(1); // Simule que l'utilisateur est un utilisateur pour la première fois

	        // Act: Appel de la méthode sous test
	        parkingService.processIncomingVehicle();

	        // Assert: Vérification des interactions avec les mocks
	        verify(inputReaderUtil, times(1)).readSelection(); // Vérifie que readSelection() est appelé une fois
	        verify(inputReaderUtil, times(1)).readVehicleRegistrationNumber(); // Vérifie que readVehicleRegistrationNumber() est appelé une fois
	        verify(parkingSpotDAO, times(1)).getNextAvailableSlot(any(ParkingType.class)); // Vérifie que getNextAvailableSlot() est appelé une fois
	        verify(parkingSpotDAO, times(1)).updateParking(any(ParkingSpot.class)); // Vérifie que updateParking() est appelé une fois
	        verify(ticketDAO, times(1)).saveTicket(any(Ticket.class)); // Vérifie que saveTicket() est appelé une fois

	    } catch (Exception e) {
	        e.printStackTrace();
	        fail("Test failed due to unexpected exception: " + e.getMessage());
	    }
	}
	@Test
	public void processExitingVehicleTestUnableUpdate() {
	    try {
	        // Arrange: Configuration des mocks nécessaires
	    	ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false); // Crée un emplacement de stationnement pour le test
	    	Ticket ticket = new Ticket();
	    	ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000))); // Ticket avec une heure d'entrée passée (1 heure avant maintenant)
	    	ticket.setParkingSpot(parkingSpot);
	    	ticket.setVehicleRegNumber("ABCDEF");
	    	ticket.setPrice(0.0);
	        when(ticketDAO.getTicket(anyString())).thenReturn(ticket); // Simule la récupération d'un ticket existant
	        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false); // Simule l'échec de la mise à jour du ticket

	        // Act: Appel de la méthode sous test
	        parkingService.processExitingVehicle();

	        // Assert: Vérification des interactions avec les mocks
	        verify(ticketDAO, times(1)).getTicket(anyString()); // Vérifie que getTicket() est appelé une fois
	        verify(ticketDAO, times(1)).updateTicket(any(Ticket.class)); // Vérifie que updateTicket() est appelé une fois
	        verify(parkingSpotDAO, never()).updateParking(any(ParkingSpot.class)); // Vérifie que updateParking() n'est pas appelé en raison de l'échec de updateTicket()

	    } catch (Exception e) {
	        e.printStackTrace();
	        fail("Test failed due to unexpected exception: " + e.getMessage());
	    }
	}
	@Test
	public void testGetNextParkingNumberIfAvailableParkingNumberNotFound() {
	    // Arrange: Configuration des mocks nécessaires
	    when(inputReaderUtil.readSelection()).thenReturn(1); // Simule la sélection de l'utilisateur pour le type de véhicule (1 pour CAR)
	    when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(-1); // Simule l'absence de place de stationnement disponible

	    // Act: Appel de la méthode sous test
	    ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();

	    // Assert: Vérification des résultats et des interactions
	    assertNull(parkingSpot, "ParkingSpot doit être nul si aucune place n'est disponible"); // Vérifie que la méthode renvoie null lorsque aucune place n'est disponible
	    verify(inputReaderUtil, times(1)).readSelection(); // Vérifie que readSelection() est appelé une fois
	    verify(parkingSpotDAO, times(1)).getNextAvailableSlot(any(ParkingType.class)); // Vérifie que getNextAvailableSlot() est appelé une fois
	}
	@Test
	public void testGetNextParkingNumberIfAvailableParkingNumberFound() {
	    // Arrange: Configuration des mocks nécessaires
	    when(inputReaderUtil.readSelection()).thenReturn(1); // Simule la sélection de l'utilisateur pour le type de véhicule (1 pour CAR)
	    when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1); // Simule l'absence de place de stationnement disponible

	    // Act: Appel de la méthode sous test
	    ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();

	    // Assert: Vérification des résultats et des interactions
	    //assertNull(parkingSpot, "ParkingSpot doit être nul si aucune place n'est disponible"); // Vérifie que la méthode renvoie null lorsque aucune place n'est disponible
	    verify(inputReaderUtil, times(1)).readSelection(); // Vérifie que readSelection() est appelé une fois
	    verify(parkingSpotDAO, times(1)).getNextAvailableSlot(any(ParkingType.class)); // Vérifie que getNextAvailableSlot() est appelé une fois
	}
	@Test
	public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument() {
	    // Arrange: Configuration des mocks nécessaires
	    when(inputReaderUtil.readSelection()).thenReturn(3); // Simule une saisie incorrecte de l'utilisateur (3, qui n'est ni 1 ni 2)

	    // Act: Appel de la méthode sous test
	    ParkingSpot parkingSpot = null;
	    try {
	        parkingSpot = parkingService.getNextParkingNumberIfAvailable();
	    } catch (IllegalArgumentException e) {
	        // Assert: Vérification de l'exception levée
	        assertEquals("la valeur entrée est invalide", e.getMessage());
	    }

	    // Assert: Vérification des résultats et des interactions
	    assertNull(parkingSpot, "ParkingSpot doit être nul quand l'utilisateur saisi une valeur éronnée"); // Vérifie que la méthode renvoie null lorsque l'entrée de l'utilisateur est incorrecte
	    verify(inputReaderUtil, times(1)).readSelection(); // Vérifie que readSelection() est appelé une fois
	    verify(parkingSpotDAO, never()).getNextAvailableSlot(any(ParkingType.class)); // Vérifie que getNextAvailableSlot() n'est jamais appelé lorsque l'entrée est incorrecte
	}
}
