package com.carsharingapp.servicetest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.carsharingapp.dto.payment.PaymentRequestDto;
import com.carsharingapp.dto.payment.PaymentResponseDto;
import com.carsharingapp.exception.EntityNotFoundException;
import com.carsharingapp.mapper.payment.PaymentMapper;
import com.carsharingapp.model.Car;
import com.carsharingapp.model.Payment;
import com.carsharingapp.model.Rental;
import com.carsharingapp.repository.payment.PaymentRepository;
import com.carsharingapp.repository.rental.RentalRepository;
import com.carsharingapp.service.NotificationService;
import com.carsharingapp.service.impl.PaymentServiceImpl;
import com.stripe.Stripe;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private NotificationService notificationService;
    private String stripeTestApi =
            "sk_test_51QD6L4I1LufT4rQ0RNeVwOk1xXUG21iQRYLovWQiQO"
            + "GnSWH4POMR39qA2vLC9KMEx9gmJlI0QxT74hCaLt4Riou000H8X5DICb";

    @BeforeEach
    void setUp() {
        Stripe.apiKey = stripeTestApi;
    }

    @Test
    void shouldCreatePaymentSessionWhenPaymentDoesNotExist() {
        PaymentRequestDto request = createPaymentRequestDto();
        Rental rental = createRental();
        Payment payment = createPayment();

        when(paymentRepository
                .findAllByRentalId(request.rentalId())).thenReturn(List.of());
        when(rentalRepository
                .findById(request.rentalId())).thenReturn(Optional.of(rental));
        when(paymentRepository
                .save(any(Payment.class))).thenReturn(payment);
        when(paymentMapper
                .toDto(payment)).thenReturn(createPaymentResponseDto());

        PaymentResponseDto response = paymentService
                .createPaymentSession(request);

        assertNotNull(response);
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenRentalDoesNotExist() {
        PaymentRequestDto request = createPaymentRequestDto();
        when(paymentRepository
                .findAllByRentalId(request.rentalId())).thenReturn(List.of());
        when(rentalRepository
                .findById(request.rentalId())).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class,
                () -> paymentService.createPaymentSession(request));
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenPaymentAlreadyPaid() {
        PaymentRequestDto request = createPaymentRequestDto();
        Payment existingPayment = createPayment();
        existingPayment.setStatus(Payment.PaymentStatus.PAID);

        when(paymentRepository.findAllByRentalId(request.rentalId()))
                .thenReturn(List.of(existingPayment));

        assertThrows(EntityNotFoundException.class,
                () -> paymentService.createPaymentSession(request));
    }

    @Test
    void shouldReturnSuccessfulPayment() {
        String sessionId = "session_123";
        Payment payment = createPayment();
        payment.setSessionId(sessionId);
        payment.setStatus(Payment.PaymentStatus.PAID);

        PaymentResponseDto paymentResponseDto = createPaymentResponseDto();

        when(paymentRepository
                .findBySessionId(sessionId)).thenReturn(Optional.of(payment));
        when(paymentRepository
                .save(any(Payment.class))).thenReturn(payment);
        when(paymentMapper
                .toDto(payment)).thenReturn(paymentResponseDto);

        PaymentResponseDto response = paymentService
                .getSuccessfulPayment(sessionId);

        assertNotNull(response);
        assertEquals(Payment.PaymentStatus.PAID, response.status());
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void shouldReturnCancelledPayment() {
        String sessionId = "session_123";
        Payment payment = createPayment();
        payment.setSessionId(sessionId);

        when(paymentRepository
                .findBySessionId(sessionId)).thenReturn(Optional.of(payment));
        when(paymentRepository
                .save(any(Payment.class))).thenReturn(payment);
        when(paymentMapper
                .toDto(any(Payment.class))).thenReturn(createPaymentResponseDto());

        PaymentResponseDto response = paymentService.getCancelledPayment(sessionId);

        assertNotNull(response);
        verify(paymentRepository).save(any(Payment.class));
        assertEquals(Payment.PaymentStatus.CANCELED, payment.getStatus());
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenRentalNotFound() {
        PaymentRequestDto request = createPaymentRequestDto();

        when(paymentRepository
                .findAllByRentalId(request.rentalId())).thenReturn(List.of());
        when(rentalRepository
                .findById(request.rentalId())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> paymentService.createPaymentSession(request));
    }

    @Test
    void shouldCreateNewPaymentSessionWhenPaymentDoesNotExist() {
        PaymentRequestDto request = createPaymentRequestDto();
        Rental rental = createRental();
        Payment payment = createPayment();

        when(paymentRepository.findAllByRentalId(request.rentalId()))
                .thenReturn(List.of());
        when(rentalRepository.findById(request.rentalId()))
                .thenReturn(Optional.of(rental));
        when(paymentRepository.save(any(Payment.class)))
                .thenReturn(payment);
        when(paymentMapper.toDto(payment))
                .thenReturn(createPaymentResponseDto());

        PaymentResponseDto response = paymentService.createPaymentSession(request);

        assertNotNull(response);
        verify(paymentRepository).save(any(Payment.class));
        verify(paymentMapper).toDto(payment);
    }

    @Test
    void shouldNotifyUserAfterSuccessfulPayment() {
        String sessionId = "session_123";
        Payment payment = createPayment();
        payment.setSessionId(sessionId);

        when(paymentRepository.findBySessionId(sessionId))
                .thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class)))
                .thenReturn(payment);

        PaymentResponseDto response = paymentService
                .getSuccessfulPayment(sessionId);

        verify(notificationService).notifyUserAboutSuccessfulPayment(payment,
                payment.getRental().getCar());
    }

    @Test
    void shouldNotifyUserAfterCanceledPayment() {
        String sessionId = "session_123";
        Payment payment = createPayment();
        payment.setSessionId(sessionId);

        when(paymentRepository.findBySessionId(sessionId)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        PaymentResponseDto response = paymentService.getCancelledPayment(sessionId);

        verify(notificationService)
                .notifyUserAboutCanceledPayment(payment,
                        payment.getRental().getCar());
    }

    private Rental createRental() {
        Car car = new Car();
        car.setModel("Model S");
        car.setBrand("Tesla");
        car.setDailyFee(new BigDecimal("100.00"));

        Rental rental = new Rental();
        rental.setCar(car);
        rental.setRentalDateTime(LocalDateTime.now());
        rental.setReturnDateTime(LocalDateTime.now().plusDays(3));
        rental.setActualReturnDate(LocalDateTime.now().plusDays(2));

        return rental;
    }

    private Payment createPayment() {
        Payment payment = new Payment();
        payment.setRental(createRental());
        payment.setStatus(Payment.PaymentStatus.PENDING);
        payment.setAmountToPay(new BigDecimal("300.00"));
        return payment;
    }

    private PaymentRequestDto createPaymentRequestDto() {
        return new PaymentRequestDto(1L, Payment.PaymentType.PAYMENT);
    }

    private PaymentResponseDto createPaymentResponseDto() {
        return new PaymentResponseDto(
                1L,
                Payment.PaymentStatus.PAID,
                Payment.PaymentType.PAYMENT,
                null,
                null,
                BigDecimal.valueOf(100.00)
        );
    }
}
