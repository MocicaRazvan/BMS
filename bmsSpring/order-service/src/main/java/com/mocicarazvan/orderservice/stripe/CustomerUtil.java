package com.mocicarazvan.orderservice.stripe;

import com.mocicarazvan.orderservice.exceptions.CustomerStripeException;
import com.mocicarazvan.templatemodule.clients.UserClient;
import com.mocicarazvan.templatemodule.dtos.UserDto;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.CustomerSearchResult;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.CustomerSearchParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class CustomerUtil {
    private final UserClient userClient;

    public Mono<Customer> findCustomerByEmail(String userId) {
        return userClient.getUser("", userId).flatMap(userDto -> {
            CustomerSearchParams params = CustomerSearchParams
                    .builder()
                    .setQuery("email:'" + userDto.getEmail() + "'")
                    .build();

            try {
                CustomerSearchResult result = Customer.search(params);
                if (!result.getData().isEmpty()) {
                    return Mono.just(result.getData().get(0));
                } else {
                    return Mono.empty();
                }
            } catch (StripeException e) {
                return Mono.error(new CustomerStripeException(userDto.getEmail()));
            }
        });
    }

    public Mono<Customer> findCustomerByEmail(UserDto userDto) {
        return Mono.fromCallable(() -> {
            CustomerSearchParams params = CustomerSearchParams
                    .builder()
                    .setQuery("email:'" + userDto.getEmail() + "'")
                    .build();

            try {
                CustomerSearchResult result = Customer.search(params);
                if (!result.getData().isEmpty()) {
                    return result.getData().get(0);
                } else {
                    return null;
                }
            } catch (StripeException e) {
                throw new CustomerStripeException(userDto.getEmail());
            }
        }).flatMap(customer -> {
            if (customer != null) {
                return Mono.just(customer);
            } else {
                return Mono.empty();
            }
        });
    }

    public Mono<Customer> findOrCreateCustomer(String userId) {
        return userClient.getUser("", userId)
                .flatMap(userDto -> {
                    CustomerSearchParams params =
                            CustomerSearchParams
                                    .builder()
                                    .setQuery("email:'" + userDto.getEmail() + "'")
                                    .build();

                    try {

                        CustomerSearchResult result = Customer.search(params);

                        Customer customer;
                        if (result.getData().isEmpty()) {

                            CustomerCreateParams customerCreateParams = CustomerCreateParams.builder()
                                    .setName(userDto.getLastName() + " " + userDto.getFirstName())
                                    .setEmail(userDto.getEmail())
                                    .build();

                            customer = Customer.create(customerCreateParams);
                        } else {
                            customer = result.getData().get(0);
                        }
                        return Mono.just(customer);
                    } catch (StripeException e) {
                        return Mono.error(new CustomerStripeException(userDto.getEmail()));
                    }

                });
    }
}