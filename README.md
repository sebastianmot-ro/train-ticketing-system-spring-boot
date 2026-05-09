# Train Ticketing System

A ticket booking platform for train routes, built as a solution to a technical problem proposed by Siemens. The application allows customers to search for routes between stations, find optimal paths with connections, and book seats on available trains. Administrators can manage the train network, handle delays, and notify affected passengers.

## Technology Stack

- **Backend:** Java 21, Spring Boot 3.4, Spring Data JPA, Spring Security, Spring Mail
- **Database:** PostgreSQL via Supabase (cloud-hosted)
- **Email:** Resend API
- **Frontend:** Vanilla HTML, CSS, JavaScript (single-page, served by Spring Boot)
- **Build:** Maven
- **Deployment:** Railway

---

## From Demo to Production

The project started as a standalone JavaFX desktop application — a GUI with text fields, buttons, and an in-memory data store. Everything ran locally: no database, no real email, no authentication beyond a hardcoded check, and no network layer. It was a functional prototype that demonstrated the core logic.

Given enough time to take it further, the decision was made to rebuild it properly. The JavaFX layer was replaced with a REST API and a static web frontend. The in-memory repositories were replaced with Spring Data JPA backed by a real PostgreSQL database on Supabase. The mock email service was replaced with a working integration through Resend. Spring Security was introduced for admin route protection. The result is a deployable web application accessible from any browser, with data that persists across restarts.

---

## Running the Project

### Live Version

The application is deployed and accessible at:

```
https://train-ticketing-system-spring-boot-production.up.railway.app
```

### Running Locally

**Prerequisites:**

- Java 21
- Maven
- A Supabase project (free tier is sufficient) — you will need the JDBC connection string, username, and password from **Settings → Database** in your Supabase dashboard
- A Resend account (free tier) with an API key from [resend.com](https://resend.com)

**Configuration:**

Create `src/main/resources/application.properties` with the following:

```properties
spring.datasource.url=jdbc:postgresql://<your-supabase-host>:5432/postgres
spring.datasource.username=<your-supabase-username>
spring.datasource.password=<your-supabase-password>
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

resend.api.key=<your-resend-api-key>

admin.username=admin
admin.password=admin
```

**Note:** Do not commit this file. Add it to `.gitignore`.

**Run:**

```bash
mvn spring-boot:run
```

The application will be available at `http://localhost:8080`. On first run, the database tables are created automatically and two sample trains are seeded.

---

## Email — Known Limitation

The deployed version uses Resend on the free plan. On this plan, the `from` address uses Resend's shared `onboarding@resend.dev` domain, which restricts delivery to only the email address registered on the Resend account. As a result, booking confirmations and delay notifications will only be reliably delivered to the account owner — not to arbitrary addresses entered by other users.

If you are running the project locally with your own Resend account and a verified custom domain, this restriction does not apply and emails will be delivered to any address.

To remove this restriction on the deployed version, a custom domain needs to be verified in Resend under **Domains**, and the `from` field in `ResendEmailService.java` updated accordingly.

---

## How to Use

### Customer — Find Path

*(screenshots here)*

Select an origin and destination from the dropdowns. Click **Find Path**. If a route exists, the legs of the journey are displayed. Clicking a leg automatically fills in the Train ID field in the booking form below.

### Customer — Book a Ticket

*(screenshots here)*

Fill in the Train ID (or click a path leg to auto-fill), travel date, email address, and number of seats. Click **Book**. The system blocks overbooking based on remaining capacity for that train and date. On success, a confirmation is shown in the UI and a confirmation email is sent to the provided address.

*(screenshots of confirmation email here)*

Note: due to the Resend free plan limitation described above, email delivery in the deployed version is restricted to the account owner's address.

### Admin — Login

*(screenshots here)*

Switch to the Admin tab and enter credentials. The admin panel unlocks on successful authentication.

### Admin — Manage Trains

*(screenshots here)*

Add a new train by providing an ID, name, capacity, and a list of stops in the format `StationName,HH:MM,HH:MM` (one per line, arrival and departure). Trains can also be deleted by ID. Any change to the train network is immediately reflected in the customer-facing dropdowns.

### Admin — Notify Delay

*(screenshots here)*

Enter a Train ID and a delay description. All passengers who have booked that train receive an email notification.

*(screenshots of delay email here)*

Note: same Resend free plan restriction applies here.

### Admin — Bookings

*(screenshots here)*

View all bookings across all trains and dates.

---

## Solution Design

### Path Finding — BFS

The route search uses **Breadth-First Search** over a graph of `ItineraryLeg` objects. Each leg represents a direct segment between two stations on a single train, derived from the stop list. BFS was chosen because it guarantees the path with the fewest connections, which is the most natural definition of "best path" for a passenger. The graph is built on each query from the current train data, so it always reflects the live state of the network without requiring a separate graph store.

Connection validity is enforced during traversal: a connecting leg is only considered if its departure time is not before the arrival time of the previous leg. Visited states are tracked as `station@time` pairs rather than just stations, which allows the algorithm to correctly handle cases where the same station is passed through at different times on different routes.

### Data Model

The core entities are `Train`, `RouteStop`, and `Booking`. A `Train` owns an ordered list of `RouteStop` objects, each carrying a station name and arrival/departure times. `ItineraryLeg` is not persisted — it is a derived structure used only during path computation. `Booking` is independent of the `Train` entity at the database level, linked only by `trainId`, which keeps the booking logic simple and avoids cascading issues on train deletion.

### Design Patterns

**Repository pattern:** Data access is fully abstracted behind `TrainRepository` and `BookingRepository`. The service layer never touches JPA directly, which keeps business logic decoupled from persistence.

**Dependency injection:** All dependencies — repositories, email service — are injected through constructors. This made the original JavaFX version easy to test with a mock email service and made the migration to Spring straightforward.

**Strategy pattern (implicit):** `EmailService` is an interface. The original implementation was a mock that logged to console. The production implementation uses Resend. Swapping between them requires no changes to the service layer.

**Layered architecture:** The application is split into distinct layers — model, repository, service, controller, config — with dependencies flowing in one direction. Controllers depend on services; services depend on repositories; nothing in the lower layers knows about HTTP or the web layer.

### Security

Spring Security protects admin endpoints with HTTP Basic authentication. Public endpoints (station list, path search, booking creation) require no authentication. The admin credential is externalized to environment variables and never hardcoded in the repository.

---

## Further Development

The current implementation covers the core requirements and is production-deployable, but several areas could be extended given more time.

**Authentication:** The admin login is HTTP Basic over HTTPS, which is functional but minimal. A proper session-based or token-based authentication flow (JWT or Spring Session) would be more appropriate for a multi-user admin setup, and would also open the door to customer accounts with booking history.

**Customer accounts:** Right now bookings are tied to an email address with no concept of identity. Introducing user registration and login would allow customers to view, manage, and cancel their own bookings.

**Cancellations and refunds:** There is no cancellation flow. Adding one would require tracking booking status and, if payments were involved, integrating a refund mechanism.

**Payments:** The booking flow completes without any payment step. Integrating a payment provider (Stripe being the most straightforward) would make the system closer to production-ready.

**Path finding improvements:** The current BFS finds the path with the fewest legs. A weighted search (Dijkstra or A*) could optimize for total travel time instead, which is more useful when connections have long waiting times.

**Date-aware routing:** The path search currently operates on times only, without considering dates. Overnight routes or routes spanning multiple days are not handled correctly.

**Admin dashboard:** The admin interface is functional but minimal. A more complete dashboard would include filtering and searching bookings, per-train occupancy statistics, and a visual timetable editor.

**Testing:** The project has no automated tests. Adding unit tests for `TrainService` (path finding logic, overbooking prevention) and integration tests for the REST endpoints would significantly improve reliability.

**Email domain:** Moving off Resend's shared `onboarding@resend.dev` sender to a verified custom domain would remove the recipient restriction on the free plan and allow emails to be delivered to any address.
