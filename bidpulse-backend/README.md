# ☕ Bidpulse Backend

This is the backend API engine for Bidpulse, constructed as a **Spring Boot** application using Java 17, Spring Web MVC, Spring Security, Spring Data JPA, Hibernate, and Spring WebSockets.

---

## 🛠️ Tech Stack & Key Libraries

- **Spring Boot 3.5.10**: Framework core container, autoconfiguration, and embeddable Web server.
- **Spring Security & JJWT (Java JWT)**: Custom filters to parse and validate stateless JWT Bearer authorization headers and track active user authentication.
- **Spring Data JPA & Hibernate**: Object-relational mapping to easily interact with PostgreSQL.
- **Flyway Database Migrations**: Relational schema migrations managed in SQL scripts to guarantee schema parity across deployments.
- **Spring WebSocket with STOMP Broker**: Facilitates persistent, bi-directional connections over WebSockets to broadcast live auction bidding updates instantly.
- **Lombok**: Annotation-driven reduction of boilerplate Java code (Getters, Setters, Builders, Loggers).

---

## ⚙️ Application Configuration

Backend configuration details are defined in `src/main/resources/application.properties`:

- **Server Port**: Configured to run on port `8080`.
- **Database URL**: Dynamic connection string falling back to localhost:
  `jdbc:postgresql://localhost:5432/bidpulse`
- **JWT Key Properties**:
  - `app.jwt.secret`: 256-bit SHA secret key for signing JWTs.
  - `app.jwt.access-expiry-seconds`: Access token longevity (default: `900`s / 15 mins).
  - `app.jwt.refresh-expiry-seconds`: Refresh token longevity (default: `1209600`s / 14 days).

---

## 🗃️ Database & Migrations

Database structures are versioned under `src/main/resources/db/migration/`:

1. **`V1__init.sql`**: Initializes the base tables for users (`app_users`), auctions (`auction`), bids (`bid`), refresh tokens (`refresh_token`), and audit logs (`audit_log`).
2. **`V2__wallet_notification_payment.sql`**: Adds wallet capabilities (`wallet`) with transaction logging (`payment_transaction`), notifications (`notification`), and user role relationships (`user_roles`).
3. **`V3__seed_data.sql`**: Configures local seed files for development, containing a dummy test user (`test@local`) and preloads their wallet with a `$1000.00` virtual balance.

---

## 🔌 REST API Reference

All requests and responses use JSON formatted payloads.

### 🔑 Authentication (`/api/auth`)

* **`POST /api/auth/login`**: Simple session login.
  - Request: `{ "email": "user@local", "password": "password" }`
  - Response: `{ "status": "ok" }`
* **`POST /api/auth/token`**: Logs in and returns a bearer token pair.
  - Request: `{ "email": "user@local", "password": "password" }`
  - Response: `{ "accessToken": "...", "refreshToken": "...", "expiresIn": 900 }`
* **`POST /api/auth/refresh`**: Generates a new access token using a valid refresh token.
  - Request: `{ "refreshToken": "..." }`
  - Response: `{ "accessToken": "..." }`
* **`POST /api/auth/logout`**: Revokes the supplied refresh token.
  - Request: `{ "refreshToken": "..." }`
  - Response: `{ "status": "logged_out" }`

### 👤 User Profiles (`/api/users`)

* **`POST /api/users`**: Registers a new user account.
  - Request: `{ "email": "user@local", "name": "User Name", "password": "password", "roles": ["USER"] }`
  - Response: `{ "id": 1, "email": "user@local" }`
* **`GET /api/users/me`**: Retrieves current logged-in user profile, email, name, and roles.

### 🔨 Auctions (`/api/auctions`)

* **`GET /api/auctions`**: Paginated retrieval of all active auctions.
  - Query parameters: `page` (default 0), `size` (default 20).
* **`GET /api/auctions/{id}`**: Returns the detailed representation of an auction, including the current highest bid details.
* **`POST /api/auctions`**: *(Seller only)* Creates a new auction item.
  - Request: `{ "title": "...", "description": "...", "startingPrice": 100.00, "minIncrement": 5.00, "startTime": "...", "endTime": "..." }`
* **`PUT /api/auctions/{id}`**: *(Seller only)* Updates auction details before start.
* **`POST /api/auctions/{id}/start`**: *(Seller/Admin)* Moves auction to `RUNNING` status.
* **`POST /api/auctions/{id}/end`**: *(Seller/Admin)* Immediately ends the auction and processes the winner.

### 💸 Wallets (`/api/wallets`)

* **`GET /api/wallets/me`**: Fetches the authenticated user's wallet balances.
  - Response: `{ "balance": 850.00, "reserved": 150.00 }`
* **`POST /api/wallets/deposit`**: Deposits paper funds into the user's wallet.
  - Query parameter: `?amount=100.00`

### 🛡️ Admin Actions (`/api/admin`)

* **`POST /api/admin/apply-seller`**: Submits a pending seller application.
  - Request: `{ "reason": "I want to list digital assets." }`
* **`GET /api/admin/applications`**: *(Admin only)* Lists all pending seller applications.
* **`POST /api/admin/applications/{id}/approve`**: *(Admin only)* Approves the application and automatically adds the `SELLER` role to the applicant.

---

## ⚡ Live WebSockets (STOMP)

Live bidding is managed via persistent connections over `/ws` endpoints fallback-supported by SockJS.

### Connecting
Clients establish a WebSocket connection via SockJS:
`ws://localhost:8080/ws`

### Subscription
Clients subscribe to receive new bids on an auction:
`/topic/auction.{auctionId}`

### Real-time Event Payload
When a bid is successfully accepted, the server broadcasts:
```json
{
  "type": "new_bid",
  "auctionId": 1,
  "bidId": 12,
  "amount": 250.00,
  "bidderId": 3,
  "placedAt": "2026-06-06T13:21:06Z"
}
```

---

## 🚀 Running the Backend Locally

Build and start the application using the Maven wrapper:

```bash
# Windows
mvnw.cmd clean spring-boot:run

# Unix/macOS
./mvnw clean spring-boot:run
```
