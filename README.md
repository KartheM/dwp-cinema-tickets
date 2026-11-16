# Cinema Tickets

Java 21 implementation of the DWP Cinema Tickets coding exercise.

## Technologies

- Java 21
- Maven
- JUnit 5
- Mockito

## Business Rules

| Ticket | Price | Seats |
|---|---:|---:|
| Adult | £25 | 1 |
| Child | £15 | 1 |
| Infant | £0 | 0 |

- Maximum of 25 tickets per purchase, including infants.
- Child and Infant tickets require at least one Adult ticket.
- Account ID must be greater than zero.
- Negative ticket quantities and invalid requests are rejected.
- At least one ticket must be purchased.
- Infants do not require separate seats.
- Payment and seat reservation are handled through the supplied services.

## Design Decisions

- `TicketTypeRequest` is implemented as an immutable class.
- Ticket prices are centralised in the `TicketTypeRequest.Type` enum.
- Constructor injection is used for external services and the validation component.
- `TicketEligibilityValidator` handles account and ticket eligibility validation.
- Validation is completed before any external service calls.
- Multiple requests of the same ticket type are aggregated.
- Payment and seat calculations are handled separately from validation.
- Payment and seat reservation are each called once per valid purchase.

## Assumptions

The requirements do not explicitly specify a maximum number of infants per adult. This implementation requires at least one adult ticket when infant tickets are purchased, but does not enforce a one-infant-per-adult restriction.

## Testing

Unit tests cover:

- Valid purchases and payment calculations.
- Seat reservation calculations.
- Maximum ticket limit.
- Invalid account IDs and ticket requests.
- Adult, Child and Infant eligibility rules.
- Multiple requests of the same ticket type.
- External service invocation counts.

## Run Tests

```bash
mvn clean test
```

## Build

```bash
mvn clean package
```
 
 
