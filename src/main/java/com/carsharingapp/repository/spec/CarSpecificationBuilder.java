package com.carsharingapp.repository.spec;

import com.carsharingapp.dto.car.CarFilterDto;
import com.carsharingapp.model.Car;
import com.carsharingapp.repository.SpecificationBuilder;
import com.carsharingapp.repository.SpecificationProviderManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CarSpecificationBuilder implements SpecificationBuilder<Car> {

    private final SpecificationProviderManager<Car> carSpecificationProviderManager;

    private enum SearchCriteria {
        BRAND("brand"),
        MODEL("model"),
        DAILYFEE("dailyFee"),
        INVENTORY("inventory"),
        CARBODYTYPE("carBodyType");

        private final String value;

        SearchCriteria(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    @Override
    public Specification<Car> build(CarFilterDto carFilterDto) {

        Specification<Car> specification = Specification.where(null);
        specification = addSpecification(specification, carFilterDto.brands(),
                SearchCriteria.BRAND);
        specification = addSpecification(specification, carFilterDto.models(),
                SearchCriteria.MODEL);
        specification = addSpecification(specification, carFilterDto.carBodyTypes(),
                SearchCriteria.CARBODYTYPE);
        specification = addSpecification(specification, carFilterDto.dailyFees(),
                SearchCriteria.DAILYFEE);
        specification = addSpecification(specification, carFilterDto.inventories(),
                SearchCriteria.INVENTORY);

        return specification;
    }

    private Specification<Car> addSpecification(Specification<Car> specification,
                                                String[] values,
                                                SearchCriteria searchCriteria) {
        if (values != null && values.length > 0) {
            Specification<Car> newSpec = carSpecificationProviderManager
                    .getSpecificationProvider(searchCriteria.getValue())
                    .getSpecification(values);
            if (newSpec != null) {
                specification = specification.and(newSpec);
            }
        }
        return specification;
    }
}
