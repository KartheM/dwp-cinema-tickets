
package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.paymentgateway.TicketPaymentServiceImpl;
import thirdparty.seatbooking.SeatReservationService;
import thirdparty.seatbooking.SeatReservationServiceImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.validation.TicketEligibilityValidator;

public class TicketServiceImpl implements TicketService {

    private final TicketPaymentService paymentService;
    private final SeatReservationService reservationService;
    private final TicketEligibilityValidator eligibilityValidator;

    public TicketServiceImpl() {
        this(
                new TicketPaymentServiceImpl(),
                new SeatReservationServiceImpl(),
                new TicketEligibilityValidator()
        );
    }

    public TicketServiceImpl(
            TicketPaymentService paymentService,
            SeatReservationService reservationService,
            TicketEligibilityValidator eligibilityValidator) {

        this.paymentService = paymentService;
        this.reservationService = reservationService;
        this.eligibilityValidator = eligibilityValidator;
    }

    @Override
    public void purchaseTickets(
            Long accountId,
            TicketTypeRequest... ticketTypeRequests) {

        // Validate account and ticket eligibility before processing payment.
        eligibilityValidator.validate(accountId, ticketTypeRequests);

        int totalAmount = 0;
        int totalSeats = 0;

        for (TicketTypeRequest request : ticketTypeRequests) {

            int quantity = request.getNoOfTickets();
            int price = request.getTicketType().getPrice();

            totalAmount = Math.addExact(
                    totalAmount,
                    Math.multiplyExact(quantity, price)
            );

            // Infants do not require a separate seat.
            if (request.getTicketType() != TicketTypeRequest.Type.INFANT) {
                totalSeats = Math.addExact(totalSeats, quantity);
            }
        }

        paymentService.makePayment(accountId, totalAmount);

        reservationService.reserveSeat(accountId, totalSeats);
    }
}
