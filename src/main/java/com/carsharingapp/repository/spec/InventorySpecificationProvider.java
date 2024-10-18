package com.carsharingapp.repository.spec;

import com.carsharingapp.model.Car;
import com.carsharingapp.repository.SpecificationProvider;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class InventorySpecificationProvider implements SpecificationProvider<Car> {
    private static final String KEY_WORD = "inventory";

    @Override
    public String getKey() {
        return KEY_WORD;
    }

    @Override
    public Specification<Car> getSpecification(String[] params) {
        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder
                    .lessThanOrEqualTo(root.get(KEY_WORD), params[0]);
            return predicate;
        };
    }
}
