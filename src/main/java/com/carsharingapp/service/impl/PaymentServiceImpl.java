package com.carsharingapp.service.impl;

import com.carsharingapp.dto.payment.PaymentRequestDto;
import com.carsharingapp.dto.payment.PaymentResponseDto;
import com.carsharingapp.exception.EntityNotFoundException;
import com.carsharingapp.exception.PaymentNotFoundException;
import com.carsharingapp.mapper.payment.PaymentMapper;
import com.carsharingapp.model.Car;
import com.carsharingapp.model.Payment;
import com.carsharingapp.model.Rental;
import com.carsharingapp.repository.payment.PaymentRepository;
import com.carsharingapp.repository.rental.RentalRepository;
import com.carsharingapp.service.NotificationService;
import com.carsharingapp.service.PaymentService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private static final String DOMAIN = "http://localhost:8080";
    private static final double FINE_MULTIPLIER = 1.5;
    private static final String SUCCESS_URL = "/payments/success?sessionId={CHECKOUT_SESSION_ID}";
    private static final String CANCEL_URL = "/payments/cancel?sessionId={CHECKOUT_SESSION_ID}";

    private final PaymentRepository paymentRepository;
    private final RentalRepository rentalRepository;
    private final PaymentMapper paymentMapper;
    private final NotificationService notificationService;

    @Value("${stripe.secret-key}")
    private String stripeKey;

    @Value("${payment.success.url}")
    private String successUrl;

    @Value("${payment.cancel.url}")
    private String cancelUrl;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeKey;
    }

    @Override
    public List<PaymentResponseDto> getPayments(Long userId, Pageable pageable) {
        return paymentRepository.findAllByRentalUserId(userId, pageable).stream()
                .map(paymentMapper::toDto)
                .toList();
    }

    @Override
    public List<PaymentResponseDto> getPaymentsByStatus(Long userId,
                                                        String status,
                                                        Pageable pageable) {
        return paymentRepository
                .findAllByStatus(Payment
                        .PaymentStatus
                        .valueOf(status.toUpperCase()))
                .stream()
                .map(paymentMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public PaymentResponseDto createPaymentSession(PaymentRequestDto request) {
        Payment payment = getPaymentIfExists(request)
                .orElseGet(() -> createNewPayment(request));

        checkout(payment.getRental().getCar(), payment);
        return paymentMapper.toDto(paymentRepository.save(payment));
    }

    @Transactional
    @Override
    public PaymentResponseDto getSuccessfulPayment(String sessionId) {
        Payment payment = findPaymentBySessionId(sessionId);
        payment.setStatus(Payment.PaymentStatus.PAID);
        notificationService.notifyUserAboutSuccessfulPayment(
                payment,
                payment.getRental().getCar());
        return paymentMapper.toDto(paymentRepository.save(payment));
    }

    @Override
    @Transactional
    public PaymentResponseDto getCancelledPayment(String sessionId) {
        Payment payment = findPaymentBySessionId(sessionId);
        payment.setStatus(Payment.PaymentStatus.CANCELED);
        notificationService.notifyUserAboutCanceledPayment(payment,
                payment.getRental().getCar());
        return paymentMapper.toDto(paymentRepository.save(payment));
    }

    private Optional<Payment> getPaymentIfExists(PaymentRequestDto request) {
        return paymentRepository.findAllByRentalId(request.rentalId()).stream()
                .filter(p -> p.getType() == request.paymentType()
                        && p.getStatus() != Payment.PaymentStatus.CANCELED)
                .findFirst()
                .map(payment -> {
                    if (payment.getStatus() == Payment.PaymentStatus.PAID) {
                        throw new EntityNotFoundException("This rental has been paid");
                    }
                    return payment;
                });
    }

    private Payment createNewPayment(PaymentRequestDto request) {
        Rental rental = rentalRepository
                .findById(request.rentalId())
                .orElseThrow(() -> new EntityNotFoundException("Can't find rental by id "
                        + request.rentalId()));

        Payment payment = new Payment();
        payment.setRental(rental);
        payment.setType(request.paymentType());

        Car car = rental.getCar();
        BigDecimal amountToPay = calculateAmountToPay(rental,
                car, request.paymentType());

        payment.setAmountToPay(amountToPay);
        payment.setStatus(Payment.PaymentStatus.PENDING);
        return payment;
    }

    private BigDecimal calculateAmountToPay(Rental rental,
                                            Car car,
                                            Payment.PaymentType paymentType) {
        BigDecimal dailyFee = car.getDailyFee();
        long days = calculateRentalDays(rental, paymentType);

        if (paymentType != Payment.PaymentType.PAYMENT) {
            dailyFee = dailyFee
                    .multiply(BigDecimal.valueOf(FINE_MULTIPLIER));
        }

        return dailyFee.multiply(BigDecimal.valueOf(days));
    }

    private long calculateRentalDays(Rental rental, Payment.PaymentType paymentType) {
        LocalDateTime rentalDateTime = rental.getRentalDateTime();
        LocalDateTime returnDateTime = rental.getReturnDateTime();

        if (paymentType == Payment.PaymentType.PAYMENT) {
            Duration duration = Duration.between(rentalDateTime, returnDateTime);
            return duration.toDays() + 1;
        } else {
            LocalDateTime actualReturnDateTime = rental.getActualReturnDate();
            Duration duration = Duration.between(returnDateTime, actualReturnDateTime);
            return duration.toDays() + 1;
        }
    }

    private Payment findPaymentBySessionId(String sessionId) {
        return paymentRepository.findBySessionId(sessionId)
                .orElseThrow(
                        () -> new PaymentNotFoundException("There is no session by id "
                                + sessionId));
    }

    private Payment checkout(Car car, Payment payment) {
        SessionCreateParams.Builder builder = new SessionCreateParams.Builder()
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .setExpiresAt(Instant.now().plus(31, ChronoUnit.MINUTES).getEpochSecond())
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("usd")
                                .setUnitAmount(payment.getAmountToPay().longValue() * 100L)
                                .setProductData(SessionCreateParams
                                        .LineItem
                                        .PriceData
                                        .ProductData
                                        .builder()
                                        .setName("Renting " + car.getBrand() + " " + car.getModel())
                                        .build())
                                .build())
                        .setQuantity(1L)
                        .build())
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(DOMAIN + SUCCESS_URL)
                .setCancelUrl(DOMAIN + CANCEL_URL);

        try {
            Session session = Session.create(builder.build());
            payment.setSessionId(session.getId());
            payment.setSessionUrl(new URL(session.getUrl()));
        } catch (StripeException | MalformedURLException e) {
            throw new RuntimeException("Failed to create Stripe session", e);
        }
        return payment;
    }
}
