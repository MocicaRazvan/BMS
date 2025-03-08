package com.mocicarazvan.cartservice.services.impl;

import com.mocicarazvan.cartservice.clients.OrderClient;
import com.mocicarazvan.cartservice.clients.PlanClient;
import com.mocicarazvan.cartservice.dtos.CartDeletedResponse;
import com.mocicarazvan.cartservice.dtos.UserCartBody;
import com.mocicarazvan.cartservice.dtos.UserCartResponse;
import com.mocicarazvan.cartservice.dtos.clients.PlanResponse;
import com.mocicarazvan.cartservice.dtos.clients.UserSubscriptionDto;
import com.mocicarazvan.cartservice.exceptions.UserCartNotFound;
import com.mocicarazvan.cartservice.mappers.UserCartMapper;
import com.mocicarazvan.cartservice.models.UserCart;
import com.mocicarazvan.cartservice.repositories.UserCartRepository;
import com.mocicarazvan.cartservice.services.UserCartService;
import com.mocicarazvan.templatemodule.clients.UserClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserCartServiceImpl implements UserCartService {
    private final UserClient userClient;
    private final PlanClient planClient;
    private final UserCartMapper userCartMapper;
    private final UserCartRepository userCartRepository;
    private final OrderClient orderClient;
    private final TransactionalOperator transactionalOperator;


    @Override
    public Mono<UserCartResponse> getOrCreate(Long userId) {
        return Mono.zip(
                        getUser(userId),
                        getUserSubscriptions(userId)
                )
                .map(Tuple2::getT2)
                .flatMap(subs -> userCartRepository.findByUserId(userId)
                        .flatMap(uc -> {

                            if (uc.getPlanIds().isEmpty()) {
                                return Mono.just(uc);
                            }

                            List<Long> originalIds = new ArrayList<>(uc.getPlanIds());
                            uc.getPlanIds().removeAll(subs);
                            return originalIds.size() != uc.getPlanIds().size() ? userCartRepository.save(uc) : Mono.just(uc);

                        })
                        .switchIfEmpty(createCartForUser(userId))
                        .flatMap(uc -> createResponse(userId, uc))
                        .as(transactionalOperator::transactional)
                );
    }


    @Override
    public Mono<UserCartResponse> addToCart(UserCartBody userCartBody, Long userId) {
        return
                Mono.zip(
                                getUser(userId),
                                verifyPlans(userCartBody, userId),
                                getUserSubscriptions(userId)
                        )
                        .map(Tuple3::getT3)
                        .flatMap(subs -> userCartRepository.findByUserId(userId)
                                .switchIfEmpty(createCartForUser(userId))
                                .flatMap(uc ->
                                        {
                                            userCartBody.getPlanIds().removeAll(subs);
                                            return userCartMapper.updateModelFromBody(userCartBody, uc);
                                        }

                                )
                                .flatMap(userCartRepository::save)
                                .flatMap(uc -> createResponse(userId, uc))

                        );
    }

    private Mono<UserCartResponse> createResponse(Long userId, UserCart uc) {
        UserCartResponse userCartResponse = userCartMapper.fromModelToResponse(uc);
        return getPlansForCart(uc.getPlanIds(), userId)
                .map(planResponses -> {
                    userCartResponse.setPlans(planResponses);
                    return userCartResponse;
                })
                .onErrorResume(e -> {
                    log.error("Error while getting plans for cart", e);
                    return userCartRepository.deleteByUserId(userId)
                            .then(createCartForUser(userId)
                                    .map(userCartMapper::fromModelToResponse)
                            ).as(transactionalOperator::transactional);
                });
    }


    @Override
    public Mono<UserCartResponse> removeFromCart(UserCartBody userCartBody, Long userId) {
        return
                getUser(userId)
                        .then(
                                userCartRepository.findByUserId(userId)
                                        .switchIfEmpty(Mono.error(new UserCartNotFound(userId)))
                                        .map(uc ->
                                        {
                                            uc.getPlanIds().removeAll(userCartBody.getPlanIds());
                                            return uc;
                                        })
                                        .flatMap(userCartRepository::save)
                                        .flatMap(uc -> createResponse(userId, uc))
                                        .as(transactionalOperator::transactional)
                        );
    }

    @Override
    public Mono<UserCartResponse> deleteCartWithNewCreated(Long userId) {
        return
                deleteUserCartBase(userId)
                        .then(createCartForUser(userId))
                        .flatMap(uc -> createResponse(userId, uc))
                        .as(transactionalOperator::transactional);
    }


    @Override
    public Mono<CartDeletedResponse> deleteCart(Long userId) {
        return
                deleteUserCartBase(userId)
                        .thenReturn(CartDeletedResponse.cartDeletedResponseSuccess());
    }

    @Override
    public Mono<Void> deleteOldCars(LocalDateTime updatedAtIsLessThan) {
        return userCartRepository.deleteAllByUpdatedAtLessThan(updatedAtIsLessThan);
    }


    @Override
    public Mono<Long> countAll() {
        return userCartRepository.countAllBy();
    }

    @Override
    public Mono<Long> clearOldCarts(Long clearOldCartsDaysCutoff) {
        log.info("Clearing old carts with cutoff days: {}", clearOldCartsDaysCutoff);
        return countAll()
                .flatMap(count -> {
                    log.info("Old carts count before delete: {}", count);
                    return deleteOldCars(LocalDateTime.now().minusDays(clearOldCartsDaysCutoff))
                            .then(countAll().map(
                                    newCount -> {
                                        log.info("Old carts count after delete: {}", newCount);
                                        return count - newCount;
                                    }
                            ));
                });
    }

    private Mono<Void> deleteUserCartBase(Long userId) {
        return userCartRepository.existsByUserId(userId)
                .filter(Boolean::booleanValue)
                .switchIfEmpty(Mono.error(new UserCartNotFound(userId)))
                .then(userCartRepository.deleteByUserId(userId));
    }

    private Mono<List<PlanResponse>> getPlansForCart(List<Long> planIds, Long userId) {
        if (planIds.isEmpty()) {
            return Mono.just(new ArrayList<>());
        }
        return planClient.getByIds(planIds
                        .stream()
                        .map(String::valueOf)
                        .toList()
                , String.valueOf(userId)).collectList();
    }

    private Mono<List<Long>> getUserSubscriptions(Long userId) {
        return orderClient.getUserSubscriptions(String.valueOf(userId)).map(UserSubscriptionDto::getPlanId)
                .collectList();
    }

    private Mono<UserCart> createCartForUser(Long userId) {
        return userCartRepository.save(
                UserCart.builder()
                        .userId(userId)
                        .planIds(new ArrayList<>())
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build()
        );
    }


    private Mono<Boolean> getUser(Long userId) {
        return userClient.existsUser("/exists", String.valueOf(userId))
                .thenReturn(true);
    }

    private Mono<Boolean> verifyPlans(UserCartBody userCartBody, Long userId) {
        return planClient.verifyIds(userCartBody.getPlanIds().stream()
                        .map(String::valueOf).toList()
                , String.valueOf(userId)).thenReturn(true);
    }
}
