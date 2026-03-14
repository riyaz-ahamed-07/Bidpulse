-- V1__init.sql: core schema (users, auction, bid, refresh token, audit_log)
CREATE TABLE IF NOT EXISTS app_users (
  id BIGSERIAL PRIMARY KEY,
  email VARCHAR(255) NOT NULL UNIQUE,
  name VARCHAR(255),
  password_hash VARCHAR(255) NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  last_login TIMESTAMP WITH TIME ZONE
);

CREATE TABLE IF NOT EXISTS auction (
  id BIGSERIAL PRIMARY KEY,
  title TEXT NOT NULL,
  description TEXT,
  seller_id BIGINT NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
  starting_price NUMERIC(18,2) NOT NULL,
  min_increment NUMERIC(18,2) NOT NULL,
  reserve_price NUMERIC(18,2),
  start_time TIMESTAMP WITH TIME ZONE,
  end_time TIMESTAMP WITH TIME ZONE,
  status VARCHAR(20) NOT NULL,
  highest_bid_amount NUMERIC(18,2),
  highest_bidder_id BIGINT,
  version BIGINT DEFAULT 0,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE TABLE IF NOT EXISTS bid (
  id BIGSERIAL PRIMARY KEY,
  auction_id BIGINT NOT NULL REFERENCES auction(id) ON DELETE CASCADE,
  bidder_id BIGINT NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
  amount NUMERIC(18,2) NOT NULL,
  placed_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  status VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS refresh_token (
  id BIGSERIAL PRIMARY KEY,
  token_hash VARCHAR(512) NOT NULL,
  user_id BIGINT NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
  expiry TIMESTAMP WITH TIME ZONE NOT NULL,
  revoked BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE TABLE IF NOT EXISTS audit_log (
  id BIGSERIAL PRIMARY KEY,
  entity_name VARCHAR(150),
  action VARCHAR(100),
  payload TEXT,
  user_id BIGINT,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_users_email ON app_users(email);
CREATE INDEX IF NOT EXISTS idx_auction_status_endtime ON auction(status, end_time);
CREATE INDEX IF NOT EXISTS idx_bid_auction_amount_desc ON bid(auction_id, amount DESC);