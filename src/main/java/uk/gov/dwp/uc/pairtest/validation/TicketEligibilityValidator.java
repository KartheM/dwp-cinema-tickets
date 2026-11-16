
package uk.gov.dwp.uc.pairtest.validation;

import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

public class TicketEligibilityValidator {

    private static final int MAX_TICKETS = 25;

    public void validate(Long accountId, TicketTypeRequest... requests) {

        if (accountId == null || accountId <= 0) {
            throw new InvalidPurchaseException("Invalid account ID.");
        }

        if (requests == null || requests.length == 0) {
            throw new InvalidPurchaseException("Ticket requests cannot be empty.");
        }

        int totalTickets = 0;
        int adults = 0;
        int children = 0;
        int infants = 0;

        for (TicketTypeRequest request : requests) {

            if (request == null) {
                throw new InvalidPurchaseException("Ticket request cannot be null.");
            }

            if (request.getTicketType() == null) {
                throw new InvalidPurchaseException("Ticket type cannot be null.");
            }

            int quantity = request.getNoOfTickets();

            if (quantity < 0) {
                throw new InvalidPurchaseException("Ticket quantity cannot be negative.");
            }

            totalTickets = Math.addExact(totalTickets, quantity);

            switch (request.getTicketType()) {
                case ADULT -> adults = Math.addExact(adults, quantity);
                case CHILD -> children = Math.addExact(children, quantity);
                case INFANT -> infants = Math.addExact(infants, quantity);
            }
        }

        if (totalTickets == 0) {
            throw new InvalidPurchaseException(
                    "At least one ticket must be purchased."
            );
        }

        if (totalTickets > MAX_TICKETS) {
            throw new InvalidPurchaseException(
                    "Maximum 25 tickets can be purchased."
            );
        }

        if (adults == 0 && (children > 0 || infants > 0)) {
            throw new InvalidPurchaseException(
                    "Child and Infant tickets require an Adult ticket."
            );
        }
    }
}
