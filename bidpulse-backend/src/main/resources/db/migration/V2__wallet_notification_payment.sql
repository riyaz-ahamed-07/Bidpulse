-- wallet table (primary key equals user_id)
CREATE TABLE IF NOT EXISTS wallet (
  user_id BIGINT PRIMARY KEY,
  balance NUMERIC(18,2) NOT NULL DEFAULT 0,
  reserved_amount NUMERIC(18,2) NOT NULL DEFAULT 0,
  CONSTRAINT fk_wallet_user FOREIGN KEY (user_id) REFERENCES app_users (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_wallet_user ON wallet(user_id);

-- notification table
CREATE TABLE IF NOT EXISTS notification (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
  type VARCHAR(30) NOT NULL,
  message TEXT,
  data TEXT,
  seen BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_notification_user_seen ON notification(user_id, seen);

-- payment_transaction table (simple)
CREATE TABLE IF NOT EXISTS payment_transaction (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT REFERENCES app_users(id),
  auction_id BIGINT REFERENCES auction(id),
  type VARCHAR(20),
  amount NUMERIC(18,2) NOT NULL,
  status VARCHAR(20),
  gateway_ref VARCHAR(255),
  metadata TEXT,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_payment_tx_user ON payment_transaction(user_id);

-- ... (keep your existing wallet, notification, and payment code) ...

-- user_roles table (required by Hibernate for User roles)
CREATE TABLE IF NOT EXISTS user_roles (
  user_id BIGINT NOT NULL,
  role VARCHAR(255),
  FOREIGN KEY (user_id) REFERENCES app_users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_user_roles_user ON user_roles(user_id);