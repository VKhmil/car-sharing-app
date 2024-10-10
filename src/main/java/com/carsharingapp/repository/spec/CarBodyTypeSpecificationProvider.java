package com.carsharingapp.repository.spec;

import com.carsharingapp.model.Car;
import com.carsharingapp.repository.SpecificationProvider;
import java.util.Arrays;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class CarBodyTypeSpecificationProvider implements SpecificationProvider<Car> {
    private static final String KEY_WORD = "carBodyType";

    @Override
    public String getKey() {
        return KEY_WORD;
    }

    @Override
    public Specification<Car> getSpecification(String[] params) {
        return (root, query, criteriaBuilder) -> {
            return root.get(KEY_WORD).in(Arrays.asList(params));
        };
    }
}
