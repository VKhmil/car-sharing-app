package com.carsharingapp.repository.rental;

import com.carsharingapp.model.Rental;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RentalRepository extends JpaRepository<Rental, Long> {
    @EntityGraph(attributePaths = "car")
    List<Rental> getAllByUserId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = "car")
    List<Rental> getAllByUserIdAndActualReturnDateIsNull(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = "car")
    List<Rental> getAllByUserIdAndActualReturnDateIsNotNull(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = "car")
    Optional<Rental> findRentalByIdAndUserId(Long rentalId, Long userId);
}
