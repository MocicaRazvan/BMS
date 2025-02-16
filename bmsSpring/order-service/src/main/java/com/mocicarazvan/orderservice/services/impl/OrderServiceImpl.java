package com.mocicarazvan.orderservice.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.orderservice.cache.OrderWithAddressCacheHandler;
import com.mocicarazvan.orderservice.clients.BoughtWebSocketClient;
import com.mocicarazvan.orderservice.clients.PlanClient;
import com.mocicarazvan.orderservice.dtos.*;
import com.mocicarazvan.orderservice.dtos.clients.DayResponse;
import com.mocicarazvan.orderservice.dtos.clients.MealResponse;
import com.mocicarazvan.orderservice.dtos.clients.PlanResponse;
import com.mocicarazvan.orderservice.dtos.clients.RecipeResponse;
import com.mocicarazvan.orderservice.dtos.clients.collect.FullDayResponse;
import com.mocicarazvan.orderservice.dtos.notifications.InternalBoughtBody;
import com.mocicarazvan.orderservice.email.EmailTemplates;
import com.mocicarazvan.orderservice.enums.DietType;
import com.mocicarazvan.orderservice.enums.ObjectiveType;
import com.mocicarazvan.orderservice.mappers.OrderMapper;
import com.mocicarazvan.orderservice.models.CustomAddress;
import com.mocicarazvan.orderservice.models.Order;
import com.mocicarazvan.orderservice.repositories.OrderRepository;
import com.mocicarazvan.orderservice.services.CustomAddressService;
import com.mocicarazvan.orderservice.services.OrderService;
import com.mocicarazvan.orderservice.services.PlanOrderService;
import com.mocicarazvan.orderservice.stripe.CustomerUtil;
import com.mocicarazvan.orderservice.utils.OrderCacheKeys;
import com.mocicarazvan.rediscache.annotation.RedisReactiveChildCacheEvict;
import com.mocicarazvan.templatemodule.clients.UserClient;
import com.mocicarazvan.templatemodule.dtos.PageableBody;
import com.mocicarazvan.templatemodule.dtos.generic.IdGenerateDto;
import com.mocicarazvan.templatemodule.dtos.response.EntityCount;
import com.mocicarazvan.templatemodule.dtos.response.PageableResponse;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithUserDtoEntity;
import com.mocicarazvan.templatemodule.email.EmailUtils;
import com.mocicarazvan.templatemodule.enums.Role;
import com.mocicarazvan.templatemodule.exceptions.action.IllegalActionException;
import com.mocicarazvan.templatemodule.exceptions.action.PrivateRouteException;
import com.mocicarazvan.templatemodule.exceptions.notFound.NotFoundEntity;
import com.mocicarazvan.templatemodule.hateos.CustomEntityModel;
import com.mocicarazvan.templatemodule.services.RabbitMqSender;
import com.mocicarazvan.templatemodule.utils.EntitiesUtils;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.InvoiceListParams;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Getter
public class OrderServiceImpl implements OrderService {

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Value("${stripe.webhook.secret}")
    private String stripeWebhookSecret;

    @Value("${front.url}")
    private String frontendUrl;

    private final OrderRepository orderRepository;
    private final CustomAddressService customAddressService;
    private final CustomerUtil customerUtil;
    private final UserClient userClient;
    private final PlanClient planClient;
    private final String modelName = OrderCacheKeys.ORDER_NAME;
    private final ObjectMapper objectMapper;
    private final BoughtWebSocketClient boughtWebSocketClient;
    private final RabbitMqSender rabbitMqSender;
    private final EntitiesUtils entitiesUtils;
    private final OrderMapper orderMapper;
    private final EmailUtils emailUtils;
    private final OrderWithAddressCacheHandler orderWithAddressCacheHandler;
    private final OrderServiceRedisCacheWrapper self;
    private final OrderWithAddressServiceImpl.OrderWithAddRedisCacheWrapper orderWithAddRedisCacheWrapper;
    private final PlanOrderService planOrderService;
    private final TransactionalOperator transactionalOperator;


    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    @Override
    public Mono<SessionResponse> checkoutHosted(CheckoutRequestBody checkoutRequestBody, String userId) {
        return planClient.verifyIds(checkoutRequestBody.getPlans().stream().map(o -> o.getId().toString()).toList(), userId)
                .log()
                .then(checkIfUserOwnsAPlan(userId, checkoutRequestBody.getPlans().stream().map(IdGenerateDto::getId).toList()))
                .then(checkTotalPrice(checkoutRequestBody.getPlans(), checkoutRequestBody.getTotal()))
                .then(customerUtil.findOrCreateCustomer(userId))
                .flatMap(customer -> {
                    Map<String, String> metadata = new HashMap<>();
                    metadata.put("user_id", userId);
                    metadata.put("customer_email", customer.getEmail());
//                    metadata.put("line_item_ids", checkoutRequestBody.getPlans().stream().map(IdGenerateDto::getId).map(Object::toString).toList().toString());
                    metadata.put("total", String.valueOf(checkoutRequestBody.getTotal()));

                    try {
                        List<Long> planIds = checkoutRequestBody.getPlans().stream().map(IdGenerateDto::getId).toList();
                        String plansJson = objectMapper.writeValueAsString(planIds);
                        metadata.put("plans", plansJson);
                    } catch (JsonProcessingException e) {
                        return Mono.error(new RuntimeException("Failed to serialize plans", e));
                    }


                    String clientUrl = frontendUrl.split(",")[0];

                    SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                            .setMode(SessionCreateParams.Mode.PAYMENT)
                            .setCustomer(customer.getId())
//                            .setSuccessUrl(clientUrl + "/success?session_id={CHECKOUT_SESSION_ID}")
                            .setSuccessUrl(clientUrl + "/" + checkoutRequestBody.getLocale() + "/orderComplete?session_id={CHECKOUT_SESSION_ID}")
                            .setCancelUrl(clientUrl + "/" + checkoutRequestBody.getLocale() + "/failure")
                            .setClientReferenceId(customer.getEmail())
                            .setBillingAddressCollection(SessionCreateParams.BillingAddressCollection.REQUIRED)
                            .putAllMetadata(metadata)

                            .setInvoiceCreation(SessionCreateParams.InvoiceCreation.builder().setEnabled(true)
                                    .build());


                    checkoutRequestBody.getPlans().forEach(item -> paramsBuilder.addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName(item.getTitle())
                                                                    .putMetadata("app_id", String.valueOf(item.getId()))
                                                                    .putMetadata("app_title", item.getTitle())
                                                                    .build()
                                                    )
                                                    .setCurrency("EUR")
                                                    .setUnitAmount(BigDecimal.valueOf(item.getPrice()).movePointRight(2).longValue())
                                                    .build()
                                    )
                                    .build()
                    ));

                    return Mono.fromCallable(() -> {
                        try {
                            Session session = Session.create(paramsBuilder.build());
                            return SessionResponse.builder()
                                    .sessionId(session.getId())
                                    .url(session.getUrl())
                                    .build();
                        } catch (StripeException e) {
                            throw new RuntimeException(e);
                        }
                    }).onErrorResume(e -> {
                        if (e instanceof RuntimeException && e.getCause() instanceof StripeException) {
                            return Mono.error(e.getCause());
                        }
                        return Mono.error(e);
                    });


                });
    }

    @Override
    public Mono<String> handleWebhook(String payload, String sigHeader) {
        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, stripeWebhookSecret);
        } catch (SignatureVerificationException e) {
            log.error("Signature verification failed", e);
            return Mono.just("Signature verification failed");
        }

        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        StripeObject stripeObject = null;

        if (dataObjectDeserializer.getObject().isPresent()) {
            stripeObject = dataObjectDeserializer.getObject().get();
        } else {
//             Deserialization failed, probably due to an API version mismatch.
//             Refer to the Javadoc documentation on `EventDataObjectDeserializer` for
//             instructions on how to handle this case, or return an error here.
            log.error("""
                        Deserialization failed, probably due to an API version mismatch.
                        Refer to the Javadoc documentation on `EventDataObjectDeserializer` for
                        instructions on how to handle this case, or return an error here.
                    """);
            return Mono.just("Deserialization failed");
        }
        // Handle the event
        switch (event.getType()) {
            case "checkout.session.completed":
//                assert stripeObject != null;
                System.out.println(stripeObject.toString());

                if (stripeObject instanceof Session session) {

                    return handleSessionCompleted(session);

                }
                break;
            // todo maybe handle other event types
            default:
                System.out.println("Unhandled event type: " + event.getType());
        }

        return Mono.just("No action taken");
    }

    @Override
    public Mono<InvoicesResponse> getInvoices(String userId, PageableBody pageableBody) {
        return guardInvoice(userId)
                .flatMap(customer -> {
                    if (customer == null) {
                        return Mono.just(InvoicesResponse.builder().invoices(new ArrayList<>()).nextPageHasItems(false).build());
                    }
                    return getStripeInvoices(customer.getId(), pageableBody);

                });

    }

    @Override
    public Flux<UserSubscriptionDto> getSubscriptions(String userId) {
        return orderRepository.findUserPlanIds(Long.valueOf(userId))
                .map(planId -> UserSubscriptionDto.builder().planId(planId).build());
    }

    @Override
    public Flux<PageableResponse<ResponseWithUserDtoEntity<PlanResponse>>> getPlansForUser(String title, DietType dietType, ObjectiveType objective,
                                                                                           LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                                                                           LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound,
                                                                                           PageableBody pageableBody, String userId) {
        return orderRepository.findUserPlanIds(Long.valueOf(userId))
                .collectList()
                .flatMapMany(planIds -> planClient.getPlansForUser(title, dietType, objective, planIds,
                        createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound,
                        pageableBody, userId));
    }

    @Override
    public Flux<PageableResponse<ResponseWithUserDtoEntity<PlanResponse>>> getPlansByOrder(Long orderId, String userId) {
        return userClient.getUser("", userId)
                .flatMapMany(userDto -> getOrderById(orderId)
                        .flatMapMany(order -> entitiesUtils.checkEntityOwnerOrAdmin(order, userDto)
                                .thenMany(planClient.getPlansForUser("", null, null, order.getPlanIds(),
                                        null, null, null, null,
                                        PageableBody.builder()
                                                .page(0)
                                                .size(order.getPlanIds().size())
                                                .sortingCriteria(Map.of())
                                                .build(), userId)
                                )))
                ;
    }


    @Override
    public Mono<ResponseWithUserDtoEntity<RecipeResponse>> getRecipeByIdWithUser(Long planId, Long dayId, Long recipeId, String userId) {
        return orderRepository.existsByUserIdAndPlanId(Long.valueOf(userId), planId)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new IllegalActionException("User does not own the plan"));
                    }
                    return planClient.getRecipeByIdWithUser(planId.toString(), dayId.toString(), recipeId.toString(), userId);
                });
    }

    @Override
    public Mono<ResponseWithUserDtoEntity<DayResponse>> getDayByIdWithUser(Long planId, Long dayId, String userId) {
        return orderRepository.existsByUserIdAndPlanId(Long.valueOf(userId), planId)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new IllegalActionException("User does not own the plan"));
                    }
                    return planClient.getDayByIdWithUser(planId.toString(), dayId.toString(), userId);
                });
    }

    @Override
    public Flux<CustomEntityModel<MealResponse>> getMealsByDayInternal(Long planId, Long dayId, String userId) {
        return orderRepository.existsByUserIdAndPlanId(Long.valueOf(userId), planId)
                .flatMapMany(exists -> {
                    if (!exists) {
                        return Flux.error(new IllegalActionException("User does not own the plan"));
                    }
                    return planClient.getMealsByDayEntity(planId.toString(), dayId.toString(), userId);
                });
    }

    @Override
    public Mono<FullDayResponse> getDayByPlanForUser(Long id, Long dayId, String userId) {
        return orderRepository.existsByUserIdAndPlanId(Long.valueOf(userId), id)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new IllegalActionException("User does not own the plan"));
                    }
                    return planClient.getFullDayByPlanForUser(id.toString(), dayId.toString(), userId);
                });
    }

    @Override
    public Mono<ResponseWithUserDtoEntity<PlanResponse>> getPlanByIdForUser(Long id, String userId) {
        return orderRepository.existsByUserIdAndPlanId(Long.valueOf(userId), id)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new IllegalActionException("User does not own the plan"));
                    }
                    return planClient.getPlanById(id.toString(), userId);
                });
    }

    @Override
    public Mono<CustomInvoiceDto> getInvoiceById(String id, String userId) {
        return guardInvoice(userId)
                .flatMap(customer -> {
                    if (customer == null) {
                        return Mono.error(new IllegalActionException("Customer not found"));
                    }
                    return Mono.fromCallable(() -> {
                        try {
                            return Invoice.retrieve(id);
                        } catch (StripeException e) {
                            throw new RuntimeException("Failed to retrieve invoice", e);
                        }
                    });

                }).map(this::fromStripeInvoiceToCustom);
    }


    private Mono<Customer> guardInvoice(String userId) {
        return userClient.getUser("", userId)
                .flatMap(userDto -> {
                    if (!userDto.getRole().equals(Role.ROLE_ADMIN) && !userDto.getId().equals(Long.valueOf(userId))) {
                        return Mono.error(new PrivateRouteException());
                    }
                    return customerUtil.findCustomerByEmail(userDto);
                });
    }

    private Mono<String> handleSessionCompleted(Session session) {
        log.info("Session object: {}", session);

        // Retrieve metadata
        Map<String, String> metadata = session.getMetadata();
        String customerEmail = metadata.get("customer_email");
//        String lineItemIds = metadata.get("line_item_ids");
        String userId = metadata.get("user_id");
        double total = Double.parseDouble(metadata.get("total"));

        List<Long> planIds;
        try {
            String plansJson = metadata.get("plans");
            planIds = objectMapper.readValue(plansJson, new TypeReference<List<Long>>() {
            });
        } catch (JsonProcessingException e) {
            return Mono.error(new RuntimeException("Failed to deserialize plans", e));
        }

        // Handle the event
        System.out.println("Checkout Session completed for customer: " + customerEmail);
//        System.out.println("Line Item IDs: " + lineItemIds);

        Session.CustomerDetails customerDetails = session.getCustomerDetails();

//        List<Long> lineItemIdsList = convertLineItemIds(lineItemIds);


        if (customerDetails != null && customerDetails.getAddress() != null && userId != null && !planIds.isEmpty()) {
            System.out.println("Customer Address: " + customerDetails.getAddress());

            return
                    planClient.getByIds(planIds.stream().map(Object::toString).toList(), userId).collectList()
                            .flatMap(plans ->
                                    customAddressService.saveAddress(customerDetails.getAddress())
                                            .flatMap(a -> saveOrder(session, a, planIds, total, userId)
                                                    .flatMap(o -> planOrderService.savePlansForOrder(plans, o.getId())
                                                            .thenReturn(o))
                                            ).as(transactionalOperator::transactional)
                                            .flatMap(order -> sendOrderEmail(order, customerEmail))
                                            .then(sendNotifications(plans, customerEmail))
                                            .then(self.invalidateForUser("userIn", userId)
                                                    .flatMapMany(msg ->
                                                            Flux.fromIterable(plans)
                                                                    .flatMap(self::invalidateForTrainer)
                                                    )
                                                    .then(orderWithAddRedisCacheWrapper.invalidateForMaster(userId))
                                            )
                            )
                            .thenReturn("Success");
        } else {
            return Mono.error(new IllegalActionException("Customer details are missing"));
        }
    }

    private Mono<Order> saveOrder(Session session, CustomAddress a, List<Long> planIds, double total, String userId) {
        return orderRepository.save(
                Order.builder()
                        .addressId(a.getId())
                        .planIds(planIds)
                        .total(total)
                        .userId(Long.valueOf(userId))
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .stripeInvoiceId(session.getInvoice())
                        .build()
        );
    }

    private Mono<Void> sendOrderEmail(Order order, String customerEmail) {
        String clientUrl = frontendUrl.split(",")[0];
        String orderUrl = STR."\{clientUrl}/orders/single/\{order.getId()}";


        return getInvoiceById(order.getStripeInvoiceId(), order.getUserId().toString())
                .flatMap(invoice -> {
                    String emailContent = EmailTemplates.orderEmail(clientUrl, orderUrl, invoice.getUrl());
                    return emailUtils.sendEmail(customerEmail, "Order confirmation", emailContent);
                });

    }

    private Mono<Void> checkIfUserOwnsAPlan(String userId, List<Long> planIds) {
        return orderRepository.existsUserWithPlan(Long.valueOf(userId), planIds.toArray(new Long[0]))
                .flatMap(exists -> {
                    log.error("User already owns a plan: " + exists);
                    if (exists) {
                        return Mono.error(new IllegalActionException("User already owns a plan"));
                    }
                    return Mono.empty();
                });
    }

    private Mono<Void> checkTotalPrice(List<PlanResponse> plans, double totalPrice) {
        double sum = plans.stream().mapToDouble(PlanResponse::getPrice).sum();
        if (sum != totalPrice) {
            return Mono.error(new IllegalActionException("Total price is not correct"));
        }
        return Mono.empty();
    }

    public List<Long> convertLineItemIds(String lineItemIds) {
        lineItemIds = lineItemIds.replaceAll("[\\[\\]]", "");

        return Arrays.stream(lineItemIds.split(","))
                .map(String::trim)
                .map(Long::valueOf)
                .toList();
    }

    @Override
    public Mono<EntityCount> countInParent(Long childId) {
        return orderRepository.countInParent(childId)
                .collectList()
                .map(EntityCount::new);
    }

    public Mono<InvoicesResponse> getStripeInvoices(String customerId, PageableBody pageableBody) {
        InvoiceListParams.Builder paramsBuilder = InvoiceListParams.builder()
                .setCustomer(customerId)
                .setLimit((long) pageableBody.getSize() + 1);

        if (pageableBody.getPage() > 0) {
            int offset = pageableBody.getPage() * pageableBody.getSize();
            paramsBuilder.setStartingAfter(String.valueOf(offset));
        }

        return Mono.fromCallable(() -> {
            try {
                return Invoice.list(paramsBuilder.build());
            } catch (StripeException e) {
                throw new RuntimeException(e);
            }
        }).flatMap(invoiceCollection -> {
            List<CustomInvoiceDto> invoiceDtos = new ArrayList<>();
            boolean nextPageHasItems = false;

            List<Invoice> invoices = invoiceCollection.getData();
            if (invoices.size() > pageableBody.getSize()) {
                nextPageHasItems = true;
                invoices = invoices.subList(0, pageableBody.getSize());
            }

            invoices.forEach(invoice -> {
                invoiceDtos.add(fromStripeInvoiceToCustom(invoice));
            });

            return Mono.just(InvoicesResponse.builder()
                    .invoices(invoiceDtos)
                    .nextPageHasItems(nextPageHasItems)
                    .build());
        });
    }

    private CustomInvoiceDto fromStripeInvoiceToCustom(Invoice invoice) {
        return CustomInvoiceDto.builder()
                .number(invoice.getNumber())
                .currency(invoice.getCurrency())
                .amount(invoice.getTotal() / 100.0)
                .url(invoice.getHostedInvoiceUrl())
                .creationDate(String.valueOf(invoice.getCreated()))
                .build();
    }

    private Mono<Void> sendNotifications(List<PlanResponse> plans, String senderEmail) {
        InternalBoughtBody internalBoughtBody = InternalBoughtBody.builder()
                .senderEmail(senderEmail)
                .plans(plans.stream().map(p -> new InternalBoughtBody.InnerPlanResponse(p.getId().toString(), p.getTitle())).toList())
                .build();
//        return boughtWebSocketClient.saveNotifications(internalBoughtBody);
        return Mono.fromCallable(() -> {
            rabbitMqSender.sendMessage(internalBoughtBody);
            return null;
        });
    }

    private Mono<Order> getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .switchIfEmpty(Mono.error(new NotFoundEntity("order", orderId)));
    }

    @Override
    public Mono<String> seedPlanOrders(String userId) {
        return orderRepository.findAll()
                .buffer(20)
                .flatMap(os -> Flux.fromIterable(os)
                        .flatMap(o -> planClient.getByIds(o.getPlanIds().stream().map(Object::toString).toList(), userId).collectList()
                                .flatMap(plans ->
                                        planOrderService.deleteAllByOrderId(o.getId())
                                                .then(planOrderService.savePlansForOrder(plans
                                                                .stream()
                                                                .peek(p -> {
                                                                    p.setCreatedAt(o.getCreatedAt());
                                                                    p.setUpdatedAt(o.getUpdatedAt());
                                                                }).toList()
                                                        , o.getId())
                                                )
                                )
                        )).as(transactionalOperator::transactional)
                .then(Mono.just("Success"));
    }


    @Getter
    @Component
    public static class OrderServiceRedisCacheWrapper {
        private final String modelName = OrderCacheKeys.ORDER_NAME;
        private final OrderRepository orderRepository;
        private final OrderMapper orderMapper;
        private final ObjectMapper objectMapper;

        public OrderServiceRedisCacheWrapper(OrderRepository orderRepository, OrderMapper orderMapper, ObjectMapper objectMapper) {
            this.orderRepository = orderRepository;
            this.orderMapper = orderMapper;
            this.objectMapper = objectMapper;
        }


        @RedisReactiveChildCacheEvict(key = OrderCacheKeys.CACHE_KEY_PATH, masterId = "#userId")
        public <T> Mono<T> invalidateForUser(T t, String userId) {
            log.info("Invalidating cache for user: {}", userId);
            return Mono.just(t);
        }

        @RedisReactiveChildCacheEvict(key = OrderCacheKeys.CACHE_KEY_PATH, masterPath = "userId")
        public Mono<PlanResponse> invalidateForTrainer(PlanResponse t) {
            log.info("Invalidating cache for trainer: {}", t.getUserId());
            return Mono.just(t);
        }


    }
}
