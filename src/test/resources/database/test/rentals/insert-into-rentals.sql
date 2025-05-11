SET FOREIGN_KEY_CHECKS = 0;

INSERT INTO rentals (id, rental_date_time, return_date, actual_return_date, car_id, user_id, is_deleted)
VALUES
    (1, '2024-11-05 15:30:00', '2024-11-12 15:30:00', '2024-11-11 16:00:00', 1, 2, 0),
    (2, '2024-11-15 14:00:00', '2024-11-22 14:00:00', '2024-11-19 17:30:00', 2, 2, 0),
    (3, '2024-11-12 08:00:00', '2024-11-20 08:00:00', NULL, 1, 2, 0),
    (4, '2024-11-01 10:00:00', '2024-11-10 10:00:00', NULL, 3, 1, 0);

SET FOREIGN_KEY_CHECKS = 1;