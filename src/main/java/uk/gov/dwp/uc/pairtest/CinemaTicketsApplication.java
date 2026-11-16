
package uk.gov.dwp.uc.pairtest;

import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type;

public class CinemaTicketsApplication {

    public static void main(String[] args) {

        TicketService ticketService = new TicketServiceImpl();

        TicketTypeRequest adult =
                new TicketTypeRequest(Type.ADULT, 1);

        TicketTypeRequest child =
                new TicketTypeRequest(Type.CHILD, 0);

        TicketTypeRequest infant =
                new TicketTypeRequest(Type.INFANT, 0);

        ticketService.purchaseTickets(
                1L, adult, child, infant);

        System.out.println("Ticket purchase completed!");
    }
}
