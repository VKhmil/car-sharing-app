🚗 Carsharing App
The Carsharing App is a web application that allows users to rent cars, manage inventory, payments, and receive notifications. The app supports both customer and admin roles, integrates with Stripe for payments, and uses Telegram for notifications.

📌 Current Functionality
User Roles
CUSTOMER – Default role for regular users.

MANAGER – Admin role with extended privileges.

🔑 Key Features
Registration and authentication using JWT.

Browse and rent cars.

Car inventory management.

Returning cars and calculating overdue fines.

Payments via Stripe for rentals.

Notifications via Telegram for rentals, overdue rentals, and payments.

🛠️ Technologies and Tools
Java 17

Spring Boot – Backend framework

Spring Security + JWT – Secure authentication and authorization

Spring Data JPA – Data persistence

MySQL – Relational database

Liquibase – Database schema management

Docker + docker-compose – Containerization

Stripe Java SDK – For payment processing

Telegram Bots API – For sending notifications

Swagger – Interactive API documentation

JUnit, Mockito, Testcontainers – Testing frameworks

📚 Data Models
Car
model, brand

type: SEDAN | SUV | HATCHBACK | UNIVERSAL

inventory

dailyFee

User
email, firstName, lastName, password

role: CUSTOMER | MANAGER

Rental
rentalDate, returnDate, actualReturnDate

carId, userId

Payment
type: PAYMENT | FINE

status: PENDING | PAID

rentalId, sessionUrl, sessionId, amountToPay

📍 API Endpoints
Authentication
POST /register – Register a new user

POST /login – Get JWT token

Users
GET /users/me – Get user profile

PUT /users/me – Update user profile

PUT /users/{id}/role – Update user role

Cars
GET /cars – Get a list of cars

POST /cars – Create a new car (MANAGER only)

PUT /cars/{id} – Update car

DELETE /cars/{id} – Delete car

Rentals
POST /rentals – Create a rental (decrease car inventory by 1)

GET /rentals – Get list of rentals (filter by is_active, user_id)

POST /rentals/{id}/return – Return a car (increase car inventory by 1)

Payments (Stripe)
GET /payments – Get list of payments

POST /payments – Create a payment session

GET /payments/success – Handle successful payment

GET /payments/cancel – Handle canceled payment

📣 Notifications (Telegram)
Notifications for rental creation, overdue rentals, and successful payments.

Daily overdue rental check (via @Scheduled).

A separate Telegram Bot service implements the NotificationService.

⚙️ Running the Project
Locally:
bash
Копіювати
Редагувати
git clone https://github.com/your-org/carsharing-app
cd carsharing-app
mvn clean install
mvn spring-boot:run
The app will be available at: http://localhost:8080

Docker:
bash
Копіювати
Редагувати
docker-compose up
⚠️ Create a .env file from .env.sample and add your confidential data (DB, Stripe keys, Telegram tokens).

📑 Swagger Documentation
Available at: http://localhost:8080/swagger-ui/index.html

✅ Code Quality Requirements
Checkstyle, CI, separate PR for each task

60% test coverage

Meaningful commit and branch names

No secrets in the public repository

🧩 Known Challenges and Solutions
Security: Implemented JWT and role-based authorization

Stripe Integration: Created sessions, validated payment status, and automated fine calculation

Telegram Notifications: Set up a separate service, automated notifications for new rentals, overdue rentals, and payments

📬 Contact
Please open an issue in the repository or contact the developers in your team.