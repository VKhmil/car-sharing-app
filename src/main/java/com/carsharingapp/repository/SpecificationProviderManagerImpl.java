package com.carsharingapp.repository;

import com.carsharingapp.exception.SpecificationProviderException;
import com.carsharingapp.model.Car;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpecificationProviderManagerImpl implements SpecificationProviderManager<Car> {

    private final List<SpecificationProvider<Car>> carSpecificationProviders;

    @Override
    public SpecificationProvider<Car> getSpecificationProvider(String key) {
        return carSpecificationProviders.stream()
                .filter(c -> c.getKey().equals(key))
                .findFirst()
                .orElseThrow(
                        () -> new SpecificationProviderException("Can't find correct specification"
                                + "specification provider for key " + key)
                );
    }
}
