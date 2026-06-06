# ⚡ Bidpulse

**Bidpulse** is a high-performance, real-time live auction platform designed to facilitate secure and synchronous bidding. Built with a robust **Spring Boot (Java 17)** backend, a reactive **React 19 + Tailwind CSS** frontend, and a **PostgreSQL** database, Bidpulse provides bidders and sellers with instant bid updates, dynamic auction status controls, automated state schedulers, and a virtual wallet system with automatic bid-reserve features.

---

## 🏗️ Architecture & Core Flows

Bidpulse operates on a client-server architecture powered by RESTful APIs for transactional data and WebSockets (STOMP protocol) for real-time bi-directional messaging:

```
                  ┌──────────────────────┐
                  │   React 19 Frontend  │
                  └──────────┬───────────┘
                             │
            REST (HTTP)      │      WebSockets (STOMP)
         (Auth, Wallet,      │     (Real-Time Bidding,
         Auctions, Admin)    │      Live Bid Feeds)
                             ▼
                  ┌──────────────────────┐
                  │ Spring Boot Backend  │
                  └──────────┬───────────┘
                             │
                Flyway DB    │      JPA / Hibernate
                Migrations   │      Entity Operations
                             ▼
                  ┌──────────────────────┐
                  │  PostgreSQL Database │
                  └──────────────────────┘
```

1. **JWT Silent Auth/Refresh Flow**: Authentication uses JWT Access Tokens (short-lived) and Refresh Tokens (long-lived, stored securely in the database). The frontend includes a global Axios response interceptor that transparently detects `401`/`403` errors, requests a new access token, and retries the failed request.
2. **Virtual Wallet with Hold-and-Release**: Placing a bid automatically verifies the user has sufficient funds, subtracts the bid amount from their `balance`, and adds it to their `reserved_amount`. If outbid, the system automatically releases the previous bidder's held funds. Upon auction closure, the winning bidder's reserved funds are charged (`balance` reduced, `reserved_amount` cleared) and transferred to the system/seller.
3. **Automated Auction Schedulers**: Periodic cron jobs run in the background on the Spring Boot server every 5 seconds to:
   - **Start Auctions**: Transition `DRAFT`/`SCHEDULED` auctions to `RUNNING` status once their start time passes.
   - **Finalize Auctions**: Automatically expire and transition `RUNNING` auctions to `ENDED` once their end time is reached, processing the final winning bid.

---

## 🛠️ Technology Stack

| Component | Technology | Version | Description |
| :--- | :--- | :--- | :--- |
| **Backend** | **Java & Spring Boot** | `17` / `3.5.10` | Core API, Security, JPA, and Scheduler engine |
| | **Spring Security & JJWT** | `0.11.5` | Stateful/stateless secure authentication |
| | **Spring WebSockets** | — | Real-time STOMP-based broker |
| | **Flyway** | — | Database schema versioning & migration manager |
| | **Lombok** | — | Boilerplate code reduction |
| **Database**| **PostgreSQL** | `15` | Relational storage for transactions, auctions, & user roles |
| **Frontend**| **React** | `19.2.0` | UI Component Framework |
| | **Vite** | `7.3.1` | Build tool & Dev server |
| | **Tailwind CSS** | `4.2.1` | Styling system |
| | **Axios** | `1.13.6` | HTTP request client with JWT interceptors |
| | **@stomp/stompjs & SockJS** | `7.3.0` | Client-side live websocket subscriptions |

---

## 🗃️ Database Schema

The database utilizes Flyway migrations (`db/migration/`) to manage the schema evolution:

### 1. Core Tables
* **`app_users`**: Stores registration info, hashed passwords, and audit timestamps.
* **`user_roles`**: Links users to multiple roles (`USER`, `SELLER`, `ADMIN`).
* **`auction`**: Manages auction items, title/description, seller association, starting price, minimum bid increments, current highest bid details, start/end times, and status (`DRAFT`, `RUNNING`, `ENDED`, etc.).
* **`bid`**: Logs all individual bids placed by bidders, amounts, and statuses.
* **`refresh_token`**: Stores hashed refresh tokens to allow secure silent logins.
* **`wallet`**: Keeps track of user financial state (`balance` and `reserved_amount`).
* **`notification`**: Logs user alerts (e.g., being outbid, winning an auction).
* **`payment_transaction`**: Simple billing transaction log (`DEPOSIT`, `HOLD`, `RELEASE`, `CHARGE`).
* **`audit_log`**: Records core system activities, actors, and payloads for security monitoring.

### 2. Indexes
* `idx_users_email` on `app_users(email)`
* `idx_auction_status_endtime` on `auction(status, end_time)`
* `idx_bid_auction_amount_desc` on `bid(auction_id, amount DESC)`
* `idx_wallet_user` on `wallet(user_id)`
* `idx_notification_user_seen` on `notification(user_id, seen)`
* `idx_payment_tx_user` on `payment_transaction(user_id)`

---

## 🔌 API & WebSocket Documentation

### 1. HTTP API Routes

All endpoints are prefixed with `/api`.

#### Authentication (`/api/auth`)
* `POST /auth/login` - Authenticates user.
* `POST /auth/token` - Requests token pair; returns `{accessToken, refreshToken, expiresIn}`.
* `POST /auth/refresh` - Refreshes expired access tokens.
* `POST /auth/logout` - Revokes refresh token and logs out.

#### User Profile (`/api/users`)
* `POST /users` - Registers a new user.
* `GET /users/me` - Fetches authenticated user info and roles.

#### Auctions (`/api/auctions`)
* `GET /auctions` - Retrieves a paginated list of auctions.
* `GET /auctions/{id}` - Gets details of a single auction.
* `POST /auctions` - *(Seller only)* Creates a new auction draft.
* `PUT /auctions/{id}` - *(Seller only)* Updates auction draft details.
* `POST /auctions/{id}/start` - *(Seller/Admin)* Starts the auction immediately.
* `POST /auctions/{id}/end` - *(Seller/Admin)* Ends the auction immediately.

#### Bids (`/api/auctions/{auctionId}/bids`)
* `POST /` - Places a new bid on an active auction.

#### Wallet (`/api/wallets`)
* `GET /me` - Fetches wallet balance and reserved amount.
* `POST /deposit` - Simulates a bank deposit (`?amount=X`).

#### Admin Operations (`/api/admin`)
* `POST /apply-seller` - Normal user applies to become a seller.
* `GET /applications` - *(Admin only)* Lists pending seller applications.
* `POST /applications/{id}/approve` - *(Admin only)* Approves a seller application.

---

### 2. WebSocket Events

WebSockets are handled via **STOMP** over a SockJS fallback stream.

* **Connection Endpoint**: `http://localhost:8080/ws`
* **Outgoing App Prefix**: `/app`
* **Real-time Live Room Feed**: Bidders subscribe to `/topic/auction.{auctionId}` to receive instant JSON updates when new bids are placed.
  * **Payload Structure (`WsEventPayload`)**:
    ```json
    {
      "type": "new_bid",
      "auctionId": 123,
      "bidId": 456,
      "amount": 1050.00,
      "bidderId": 7,
      "placedAt": "2026-06-06T13:21:06Z"
    }
    ```

---

## 🚀 Setup & Local Installation

### Prerequisites
* **Java 17 JDK**
* **Node.js (v18 or higher)** and **npm**
* **Docker** (recommended for running PostgreSQL)

### Step 1: Run the Database
Launch the preconfigured PostgreSQL 15 database instance using Docker Compose:
```bash
cd bidpulse-backend
docker-compose up -d
```
*This starts a database on port `5432` with user `bidpulse` and password `bidpulse`.*

### Step 2: Start the Backend Server
Run the Spring Boot application using the Maven wrapper:
```bash
# Windows
mvnw.cmd spring-boot:run

# Linux / macOS
chmod +x mvnw
./mvnw spring-boot:run
```
*The API server will launch on `http://localhost:8080` and execute database migrations automatically.*

### Step 3: Run the Frontend Application
Install dependencies and run the client-side development server:
```bash
cd ../bidpulse-frontend
npm install
npm run dev
```
*The React client will spin up on `http://localhost:5173`. Make sure the `.env` file points to your local backend server if developing locally (`VITE_API_BASE_URL=http://localhost:8080`).*

---

## 📂 Project Structure

```
Bidpulse/
├── bidpulse-backend/           # Spring Boot REST API & WebSocket Broker
│   ├── src/main/java/          # Source files (Controllers, Entities, Repos, Services)
│   ├── src/main/resources/     # Schema migrations (Flyway) & App config
│   ├── Dockerfile              # Docker container definition for build/run stages
│   └── docker-compose.yml      # DB configuration container setup
└── bidpulse-frontend/          # React Single Page App
    ├── src/                    # App files (Pages, Components, Context, API clients)
    ├── package.json            # Node configuration & scripts
    └── vite.config.js          # Vite server and compiler configurations
```
