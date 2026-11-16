
package uk.gov.dwp.uc.pairtest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;

import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;
import uk.gov.dwp.uc.pairtest.validation.TicketEligibilityValidator;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class TicketServiceImplTest {

    private TicketPaymentService paymentService;
    private SeatReservationService reservationService;
    private TicketService ticketService;

    @BeforeEach
    void setUp() {
        paymentService = mock(TicketPaymentService.class);
        reservationService = mock(SeatReservationService.class);

        ticketService = new TicketServiceImpl(
                paymentService,
                reservationService,
                new TicketEligibilityValidator()
        );
    }

    // Scenario 1: One Adult ticket
    @Test
    void shouldPurchaseOneAdultTicket() {
        ticketService.purchaseTickets(
                1L,
                new TicketTypeRequest(Type.ADULT, 1)
        );

        verify(paymentService, times(1)).makePayment(1L, 25);
        verify(reservationService, times(1)).reserveSeat(1L, 1);
    }

    // Scenario 2: One Child + one Adult
    @Test
    void shouldPurchaseChildAndAdultTickets() {
        ticketService.purchaseTickets(
                1L,
                new TicketTypeRequest(Type.CHILD, 1),
                new TicketTypeRequest(Type.ADULT, 1)
        );

        verify(paymentService, times(1)).makePayment(1L, 40);
        verify(reservationService, times(1)).reserveSeat(1L, 2);
    }

    // Scenario 3: One Infant + one Adult
    @Test
    void shouldPurchaseInfantAndAdultTickets() {
        ticketService.purchaseTickets(
                1L,
                new TicketTypeRequest(Type.INFANT, 1),
                new TicketTypeRequest(Type.ADULT, 1)
        );

        verify(paymentService, times(1)).makePayment(1L, 25);
        verify(reservationService, times(1)).reserveSeat(1L, 1);
    }

    // Scenario 4: Multiple ticket types
    @Test
    void shouldCalculateCorrectPaymentAndSeatsForMultipleTicketTypes() {
        ticketService.purchaseTickets(
                1L,
                new TicketTypeRequest(Type.ADULT, 2),
                new TicketTypeRequest(Type.CHILD, 2),
                new TicketTypeRequest(Type.INFANT, 2)
        );

        verify(paymentService, times(1)).makePayment(1L, 80);
        verify(reservationService, times(1)).reserveSeat(1L, 4);
    }

    // Scenario 5: Multiple requests of the same type
    @Test
    void shouldAggregateMultipleRequestsOfSameType() {
        ticketService.purchaseTickets(
                1L,
                new TicketTypeRequest(Type.ADULT, 1),
                new TicketTypeRequest(Type.ADULT, 1)
        );

        verify(paymentService, times(1)).makePayment(1L, 50);
        verify(reservationService, times(1)).reserveSeat(1L, 2);
    }

    // Scenario 6: Exactly 25 tickets
    @Test
    void shouldAllowExactly25Tickets() {
        ticketService.purchaseTickets(
                1L,
                new TicketTypeRequest(Type.ADULT, 25)
        );

        verify(paymentService, times(1)).makePayment(1L, 625);
        verify(reservationService, times(1)).reserveSeat(1L, 25);
    }

    // Scenario 7: 26 tickets
    @Test
    void shouldRejectMoreThan25Tickets() {
        assertThrows(InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(
                        1L,
                        new TicketTypeRequest(Type.ADULT, 25),
                        new TicketTypeRequest(Type.CHILD, 1)
                ));

        verifyNoInteractions(paymentService, reservationService);
    }

    // Scenario 8: Zero tickets
    @Test
    void shouldRejectZeroTickets() {
        assertThrows(InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(
                        1L,
                        new TicketTypeRequest(Type.ADULT, 0)
                ));

        verifyNoInteractions(paymentService, reservationService);
    }

    // Scenario 9: Negative ticket quantity
    @Test
    void shouldRejectNegativeTicketQuantity() {
        assertThrows(InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(
                        1L,
                        new TicketTypeRequest(Type.ADULT, -1)
                ));

        verifyNoInteractions(paymentService, reservationService);
    }

    // Scenario 10: Negative account ID
    @Test
    void shouldRejectNegativeAccountId() {
        assertThrows(InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(
                        -1L,
                        new TicketTypeRequest(Type.ADULT, 1)
                ));

        verifyNoInteractions(paymentService, reservationService);
    }

    // Scenario 11: Null account ID
    @Test
    void shouldRejectNullAccountId() {
        assertThrows(InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(
                        null,
                        new TicketTypeRequest(Type.ADULT, 1)
                ));

        verifyNoInteractions(paymentService, reservationService);
    }

    // Scenario 12: Null ticket request array
    @Test
    void shouldRejectNullRequestsArray() {
        assertThrows(InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(
                        1L,
                        (TicketTypeRequest[]) null
                ));

        verifyNoInteractions(paymentService, reservationService);
    }

    // Scenario 13: Null request inside array
    @Test
    void shouldRejectNullRequestElement() {
        assertThrows(InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(
                        1L,
                        (TicketTypeRequest) null
                ));

        verifyNoInteractions(paymentService, reservationService);
    }

    // Scenario 14: Null ticket type
    @Test
    void shouldRejectNullTicketType() {
        assertThrows(InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(
                        1L,
                        new TicketTypeRequest(null, 1)
                ));

        verifyNoInteractions(paymentService, reservationService);
    }

    // Scenario 15: Child without Adult
    @Test
    void shouldRejectChildWithoutAdult() {
        assertThrows(InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(
                        1L,
                        new TicketTypeRequest(Type.CHILD, 1)
                ));

        verifyNoInteractions(paymentService, reservationService);
    }

    // Scenario 16: Infant without Adult
    @Test
    void shouldRejectInfantWithoutAdult() {
        assertThrows(InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(
                        1L,
                        new TicketTypeRequest(Type.INFANT, 1)
                ));

        verifyNoInteractions(paymentService, reservationService);
    }

    // Scenario 17: Multiple Infants with one Adult
    // Accepted under the current policy: at least one Adult is required.
    @Test
    void shouldAllowMultipleInfantsWithOneAdult() {
        ticketService.purchaseTickets(
                1L,
                new TicketTypeRequest(Type.ADULT, 1),
                new TicketTypeRequest(Type.INFANT, 3)
        );

        verify(paymentService, times(1)).makePayment(1L, 25);
        verify(reservationService, times(1)).reserveSeat(1L, 1);
    }

    // Scenario 18: Adult quantity split across requests
    @Test
    void shouldAggregateAdultTicketsAcrossRequests() {
        ticketService.purchaseTickets(
                1L,
                new TicketTypeRequest(Type.ADULT, 1),
                new TicketTypeRequest(Type.CHILD, 1),
                new TicketTypeRequest(Type.ADULT, 2)
        );

        verify(paymentService, times(1)).makePayment(1L, 90);
        verify(reservationService, times(1)).reserveSeat(1L, 4);
    }

    // Scenario 19: Zero Child tickets with valid Adult tickets
    @Test
    void shouldAllowZeroChildTicketsWithAdultTickets() {
        ticketService.purchaseTickets(
                1L,
                new TicketTypeRequest(Type.ADULT, 1),
                new TicketTypeRequest(Type.CHILD, 0)
        );

        verify(paymentService, times(1)).makePayment(1L, 25);
        verify(reservationService, times(1)).reserveSeat(1L, 1);
    }

    // Scenario 20: External services called exactly once
    @Test
    void shouldCallExternalServicesExactlyOnce() {
        ticketService.purchaseTickets(
                1L,
                new TicketTypeRequest(Type.ADULT, 2),
                new TicketTypeRequest(Type.CHILD, 1),
                new TicketTypeRequest(Type.INFANT, 1)
        );

        verify(paymentService, times(1)).makePayment(1L, 65);
        verify(reservationService, times(1)).reserveSeat(1L, 3);

        verifyNoMoreInteractions(paymentService, reservationService);
    }

    @Test
    void shouldRejectEmptyRequestsArray() {
        assertThrows(InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(1L));

        verifyNoInteractions(paymentService, reservationService);
    }
}
