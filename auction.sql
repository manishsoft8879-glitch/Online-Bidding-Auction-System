-- ====================================================================
-- SmallAuction Database Schema & Initial Data
-- Compatible with SQLite, MySQL, and PostgreSQL
-- ====================================================================

-- 1. Create Person Table (Auction participants: Bidders and Sellers)
CREATE TABLE IF NOT EXISTS person (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username VARCHAR(60) NOT NULL UNIQUE,
    email VARCHAR(120) NOT NULL UNIQUE,
    balance DECIMAL(12, 2) NOT NULL DEFAULT 0.00
);

-- 2. Create Item Table (Auctions listed by sellers)
CREATE TABLE IF NOT EXISTS item (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title VARCHAR(150) NOT NULL,
    description TEXT,
    starting_price DECIMAL(12, 2) NOT NULL,
    current_price DECIMAL(12, 2) NOT NULL,
    seller_id INTEGER NOT NULL,
    highest_bidder_id INTEGER DEFAULT -1,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- 'ACTIVE', 'SOLD', 'EXPIRED'
    end_time TIMESTAMP NOT NULL,
    FOREIGN KEY (seller_id) REFERENCES person(id),
    FOREIGN KEY (highest_bidder_id) REFERENCES person(id)
);

-- 3. Create Bid Table (Historical log of bids placed)
CREATE TABLE IF NOT EXISTS bid (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    item_id INTEGER NOT NULL,
    bidder_id INTEGER NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    bid_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (item_id) REFERENCES item(id) ON DELETE CASCADE,
    FOREIGN KEY (bidder_id) REFERENCES person(id)
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_item_status ON item(status);
CREATE INDEX IF NOT EXISTS idx_item_seller ON item(seller_id);
CREATE INDEX IF NOT EXISTS idx_bid_item ON bid(item_id);
CREATE INDEX IF NOT EXISTS idx_bid_bidder ON bid(bidder_id);

-- ====================================================================
-- Seed Initial Participants
-- ====================================================================
INSERT INTO person (id, username, email, balance) VALUES
(1, 'alice', 'alice@auction.org', 5000.00),
(2, 'bob', 'bob@auction.org', 7500.00),
(3, 'charlie', 'charlie@auction.org', 3200.00),
(4, 'dana', 'dana@auction.org', 12000.00),
(5, 'edward', 'edward@auction.org', 1500.00);

-- ====================================================================
-- Seed Initial Auction Items
-- ====================================================================
INSERT INTO item (id, title, description, starting_price, current_price, seller_id, highest_bidder_id, status, end_time) VALUES
(1, '1968 Omega Speedmaster Professional', 'Vintage chronograph in pristine condition with original box & papers.', 2500.00, 3100.00, 1, 2, 'ACTIVE', datetime('now', '+15 minutes')),
(2, 'Apple MacBook Pro M3 Max (64GB)', 'Factory sealed Space Black 16-inch edition. Full AppleCare warranty.', 1800.00, 2250.00, 2, 4, 'ACTIVE', datetime('now', '+30 minutes')),
(3, 'Original Signed Acrylic: Cosmic Drift', 'Contemporary abstract artwork by M. Durand (2023), 36x48 inches.', 600.00, 950.00, 3, 1, 'ACTIVE', datetime('now', '+45 minutes')),
(4, 'First Edition The Hobbit (1937)', 'Extremely rare J.R.R. Tolkien first impression with original dust jacket.', 4000.00, 5200.00, 4, 3, 'ACTIVE', datetime('now', '+60 minutes')),
(5, 'Gibson 1959 Les Paul Standard Reissue', 'Custom Shop Murphy Lab Heavy Aged electric guitar with hardcase.', 3200.00, 3200.00, 5, -1, 'ACTIVE', datetime('now', '+120 minutes'));

-- ====================================================================
-- Seed Sample Bids
-- ====================================================================
INSERT INTO bid (id, item_id, bidder_id, amount, bid_time) VALUES
(1, 1, 2, 2800.00, datetime('now', '-10 minutes')),
(2, 1, 3, 3100.00, datetime('now', '-5 minutes')),
(3, 2, 4, 2000.00, datetime('now', '-8 minutes')),
(4, 2, 1, 2250.00, datetime('now', '-2 minutes')),
(5, 3, 1, 800.00, datetime('now', '-12 minutes')),
(6, 3, 5, 950.00, datetime('now', '-4 minutes'));