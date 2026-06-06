# ⚡ Bidpulse Frontend

This is the client-side single page application (SPA) for the Bidpulse live bidding platform. It is developed using **React 19**, compiled and bundled with **Vite**, and styled using **Tailwind CSS**.

---

## 🛠️ Tech Stack & Dependencies

- **React 19.2.0**: Component architecture, custom hooks, and context state management.
- **Vite 7.3.1**: Ultra-fast hot-reloading bundler and local web server.
- **Tailwind CSS 4.2.1**: Utility-first CSS classes for fully responsive layouts.
- **Axios 1.13.6**: Promise-based HTTP client for API requests.
- **React Router Dom 7.13.1**: Client-side declarative routing and protected navigation.
- **@stomp/stompjs 7.3.0 & SockJS Client 1.6.1**: Real-time message streaming over WebSockets.
- **React Toastify**: In-app popups and alerts for outbids and actions.

---

## 🛣️ Pages & Client Routes

All routes are declared in [App.jsx](file:///c:/Users/thahs/Bidpulse/bidpulse-frontend/src/App.jsx):

| Path | Component | Auth Requirement | Description |
| :--- | :--- | :--- | :--- |
| `/` | `LandingPage` | None (Public) | Homepage displaying current and upcoming active auctions, search, and category filters. |
| `/login` | `LoginPage` | None (Public) | Log in form (signs in and populates JWT tokens in localStorage). |
| `/register` | `RegisterPage` | None (Public) | New user sign-up form. |
| `/dashboard` | `DashboardPage` | Authenticated | Bidder dashboard displaying active bids, watchlist, won auctions, and profile information. |
| `/wallet` | `WalletPage` | Authenticated | Virtual bank integration where users can view balance, reserved funds, deposit money, and check transaction logs. |
| `/auction/:id` | `AuctionRoomPage` | Authenticated | Live bidding interface with WebSockets providing real-time bidding updates and bid histories. |
| `/seller` | `SellerDashboard` | Authenticated + `SELLER` role | Dashboard allowing sellers to create, review, start, and manually end their items. |
| `/admin` | `AdminDashboard` | Authenticated + `ADMIN` role | System admin panel to review and approve seller applications and inspect audit trails. |

---

## 🔑 Key Implementations

### 1. Axios Interceptor with Silent JWT Refresh
Located in [axiosConfig.jsx](file:///c:/Users/thahs/Bidpulse/bidpulse-frontend/src/api/axiosConfig.jsx):
* **Request Interceptor**: Automatically appends the current `accessToken` from `localStorage` under `Authorization: Bearer <token>` for all API requests.
* **Response Interceptor**: Listens for any `401 Unauthorized` or `403 Forbidden` responses. If caught, it locks outbids, requests a new token pair using the `refreshToken` from `/api/auth/refresh`, updates `localStorage`, modifies the initial failed request, and retries it silently without interrupting the user.

### 2. Protected Routes
The `ProtectedRoute` wrapper component inspects the active user context. It redirects unauthenticated users to `/login` and alerts users who lack the required roles (e.g. attempting to visit `/admin` or `/seller` without permissions).

### 3. Real-Time STOMP over SockJS client
The `AuctionRoomPage` connects to the `/ws` endpoint when mounted. It subscribes to `/topic/auction.{id}` to receive live bid broadcasts and automatically updates the active bidder layout, highest bid amount, and live bid log feed.

---

## ⚙️ Environment Configuration

Create a `.env` file in the frontend root directory to configure the target server base URL:
```env
VITE_API_BASE_URL=http://localhost:8080
```

---

## 🚀 Local Development Setup

1. **Install Node modules**:
   ```bash
   npm install
   ```

2. **Run in local development mode**:
   ```bash
   npm run dev
   ```
   *This starts the client web server at `http://localhost:5173`.*

3. **Build the production bundle**:
   ```bash
   npm run build
   ```
