INSERT INTO payments (id, rental_id, session_url, session_id, price, status, type, is_deleted)
VALUES
(1, 1, 'http://example.com/session1', 'session_123', 150.00, 'PAID', 'CREDIT_CARD', 0),
(2, 2, 'http://example.com/session2', 'session_124', 200.00, 'CANCELED', 'PAYPAL', 0);
