package com.mocicarazvan.orderservice.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.orderservice.clients.BoughtWebSocketClient;
import com.mocicarazvan.orderservice.clients.PlanClient;
import com.mocicarazvan.orderservice.dtos.*;
import com.mocicarazvan.orderservice.dtos.notifications.InternalBoughtBody;
import com.mocicarazvan.orderservice.dtos.summaries.CountAmount;
import com.mocicarazvan.orderservice.dtos.summaries.CountryOrderSummary;
import com.mocicarazvan.orderservice.dtos.summaries.DailyOrderSummary;
import com.mocicarazvan.orderservice.dtos.summaries.MonthlyOrderSummary;
import com.mocicarazvan.orderservice.dtos.summaries.trainer.MonthlyTrainerOrderSummary;
import com.mocicarazvan.orderservice.email.EmailTemplates;
import com.mocicarazvan.orderservice.enums.CountrySummaryType;
import com.mocicarazvan.orderservice.enums.DietType;
import com.mocicarazvan.orderservice.mappers.OrderMapper;
import com.mocicarazvan.orderservice.models.Order;
import com.mocicarazvan.orderservice.repositories.ExtendedOrderWithAddressRepository;
import com.mocicarazvan.orderservice.repositories.OrderRepository;
import com.mocicarazvan.orderservice.services.CustomAddressService;
import com.mocicarazvan.orderservice.services.OrderService;
import com.mocicarazvan.orderservice.stripe.CustomerUtil;
import com.mocicarazvan.templatemodule.clients.UserClient;
import com.mocicarazvan.templatemodule.dtos.PageableBody;
import com.mocicarazvan.templatemodule.dtos.generic.IdGenerateDto;
import com.mocicarazvan.templatemodule.dtos.response.EntityCount;
import com.mocicarazvan.templatemodule.dtos.response.MonthlyEntityGroup;
import com.mocicarazvan.templatemodule.dtos.response.PageableResponse;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithUserDtoEntity;
import com.mocicarazvan.templatemodule.email.EmailUtils;
import com.mocicarazvan.templatemodule.enums.Role;
import com.mocicarazvan.templatemodule.exceptions.action.IllegalActionException;
import com.mocicarazvan.templatemodule.exceptions.action.PrivateRouteException;
import com.mocicarazvan.templatemodule.exceptions.notFound.NotFoundEntity;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
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


    private final ObjectMapper objectMapper;

    private final BoughtWebSocketClient boughtWebSocketClient;

    private final EntitiesUtils entitiesUtils;
    private final OrderMapper orderMapper;

    private final EmailUtils emailUtils;


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
                        String plansJson = objectMapper.writeValueAsString(checkoutRequestBody.getPlans());
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
            // Deserialization failed, probably due to an API version mismatch.
            // Refer to the Javadoc documentation on `EventDataObjectDeserializer` for
            // instructions on how to handle this case, or return an error here.
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
            // ... handle other event types
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
    public Flux<PageableResponse<ResponseWithUserDtoEntity<PlanResponse>>> getPlansForUser(String title, DietType dietType, PageableBody pageableBody, String userId) {
        return orderRepository.findUserPlanIds(Long.valueOf(userId))
                .collectList()
                .flatMapMany(planIds -> planClient.getPlansForUser(title, dietType, planIds, pageableBody, userId));
    }

    @Override
    public Flux<PageableResponse<ResponseWithUserDtoEntity<PlanResponse>>> getPlansByOrder(Long orderId, String userId) {
        return userClient.getUser("", userId)
                .flatMapMany(userDto -> getOrderById(orderId)
                        .flatMapMany(order -> entitiesUtils.checkEntityOwnerOrAdmin(order, userDto)
                                .thenMany(planClient.getPlansForUser("", null, order.getPlanIds(), PageableBody.builder()
                                        .page(0)
                                        .size(order.getPlanIds().size())
                                        .sortingCriteria(Map.of())
                                        .build(), userId)
                                )))
                ;
    }

    @Override
    public Flux<MonthlyEntityGroup<OrderDto>> getOrdersGroupedByMonth(int month, String userId) {
        return userClient.getUser("", userId)
                .flatMapMany(userDto -> {
                    if (!userDto.getRole().equals(Role.ROLE_ADMIN)) {
                        return Mono.error(new PrivateRouteException());
                    }
                    LocalDateTime now = LocalDateTime.now();
                    int year = now.getYear();
                    if (month == 1 && now.getMonthValue() == 1) {
                        year -= 1;
                    }
                    return orderRepository.findModelByMonth(month, year)
                            .map(m -> {
                                        YearMonth ym = YearMonth.from(m.getCreatedAt());
                                        return MonthlyEntityGroup.<OrderDto>builder()
                                                .month(ym.getMonthValue())
                                                .year(ym.getYear())
                                                .entity(orderMapper.fromModelToDto(m))
                                                .build();
                                    }
                            );
                });
    }

    @Override
    public Flux<MonthlyOrderSummary> getOrdersSummaryByMonth(LocalDate from, LocalDate to, String userId) {
        Pair<LocalDateTime, LocalDateTime> intervalDates = getIntervalDates(from, to);
        return orderRepository.getOrdersSummaryByDateRangeGroupedByMonth(intervalDates.getFirst(), intervalDates.getSecond());
    }

    @Override
    public Flux<MonthlyOrderSummary> getTrainerOrdersSummaryByMonth(LocalDate from, LocalDate to, Long trainerId, String userId) {
        Pair<LocalDateTime, LocalDateTime> intervalDates = getIntervalDates(from, to);

        return userClient.existsTrainerOrAdmin("/exists", trainerId)
                .thenMany(planClient.getTrainersPlans(String.valueOf(trainerId), userId)
                        .collectList()
                        .flatMapMany(plans -> orderRepository.getTrainerOrdersSummaryByDateRangeGroupedByMonth(intervalDates.getFirst(), intervalDates.getSecond())
                                .map(e -> fromTrainerSummaryToSummary(plans, e, (p) -> MonthlyOrderSummary.builder()
                                        .year(e.getYear())
                                        .month(e.getMonth())
                                        .totalAmount(p.getFirst())
                                        .count(p.getSecond())
                                        .build()))));

    }


    @Override
    public Flux<DailyOrderSummary> getOrdersSummaryByDay(LocalDate from, LocalDate to, String userId) {

        Pair<LocalDateTime, LocalDateTime> intervalDates = getIntervalDates(from, to);

        return orderRepository.getOrdersSummaryByDateRangeGroupedByDay(intervalDates.getFirst(), intervalDates.getSecond());
    }

    @Override
    public Flux<DailyOrderSummary> getTrainerOrdersSummaryByDay(LocalDate from, LocalDate to, Long trainerId, String userId) {
        Pair<LocalDateTime, LocalDateTime> intervalDates = getIntervalDates(from, to);
        return
                userClient.existsTrainerOrAdmin("/exists", trainerId)
                        .thenMany(
                                planClient.getTrainersPlans(String.valueOf(trainerId), userId)
                                        .collectList()
                                        .flatMapMany(plans -> orderRepository.getTrainerOrdersSummaryByDateRangeGroupedByDay(intervalDates.getFirst(), intervalDates.getSecond())
                                                .map(e -> fromTrainerSummaryToSummary(plans, e, (p) -> DailyOrderSummary.builder()
                                                        .year(e.getYear())
                                                        .month(e.getMonth())
                                                        .day(e.getDay())
                                                        .totalAmount(p.getFirst())
                                                        .count(p.getSecond())
                                                        .build()))));
    }

    private Pair<LocalDateTime, LocalDateTime> getIntervalDates(LocalDate from, LocalDate to) {
        LocalDateTime startDate = from.atStartOfDay();
        LocalDateTime endDate = to.atStartOfDay().plusDays(1);
        return Pair.of(startDate, endDate);
    }

    @Override
    public Flux<CountryOrderSummary> getOrdersSummaryByCountry(CountrySummaryType type) {
        return (type.equals(CountrySummaryType.COUNT) ?
                orderRepository.getOrdersCountByCountry() :
                orderRepository.getOrdersTotalByCountry())
                .map(e -> {
                    e.setId(
                            new Locale.Builder().setRegion(e.getId().toUpperCase()).build().getISO3Country()
                    );
                    return e;
                });
    }

    @Override
    public Mono<ResponseWithUserDtoEntity<RecipeResponse>> getRecipeByPlanForUser(Long id, Long recipeId, String userId) {
        return orderRepository.existsByUserIdAndPlanId(Long.valueOf(userId), id)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new IllegalActionException("User does not own the plan"));
                    }
                    return planClient.getRecipeByPlanForUser(id.toString(), recipeId.toString(), userId);
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

    private <E extends MonthlyTrainerOrderSummary, T extends MonthlyOrderSummary> T fromTrainerSummaryToSummary(
            List<PlanResponse> plans, E e, Function<Pair<Double, Integer>, T> mapper) {

        List<Long> planIds = plans.stream().map(PlanResponse::getId).toList();

        Pair<Double, Integer> totalAndCount = e.getPlanIds().parallelStream()
                .reduce(Pair.of(0.0, 0),
                        (acc, cur) -> {
                            if (planIds.contains(cur)) {
                                double price = plans.stream()
                                        .filter(p -> p.getId().equals(cur))
                                        .findFirst()
                                        .orElseThrow(() -> new IllegalArgumentException("Plan not found in mapping with id " + cur))
                                        .getPrice();
                                return Pair.of(acc.getFirst() + price, acc.getSecond() + 1);
                            } else {
                                return acc;
                            }
                        },
                        (acc1, acc2) -> Pair.of(acc1.getFirst() + acc2.getFirst(), acc1.getSecond() + acc2.getSecond())
                );

        return mapper.apply(totalAndCount);
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
        System.out.println("Session object: " + session);

        // Retrieve metadata
        Map<String, String> metadata = session.getMetadata();
        String customerEmail = metadata.get("customer_email");
//        String lineItemIds = metadata.get("line_item_ids");
        String userId = metadata.get("user_id");
        double total = Double.parseDouble(metadata.get("total"));

        List<PlanResponse> plans;
        try {
            String plansJson = metadata.get("plans");
            plans = objectMapper.readValue(plansJson, new TypeReference<List<PlanResponse>>() {
            });
        } catch (JsonProcessingException e) {
            return Mono.error(new RuntimeException("Failed to deserialize plans", e));
        }

        List<Long> lineItemIdsList = plans.stream().map(PlanResponse::getId).toList();
        // Handle the event
        System.out.println("Checkout Session completed for customer: " + customerEmail);
//        System.out.println("Line Item IDs: " + lineItemIds);

        Session.CustomerDetails customerDetails = session.getCustomerDetails();

//        List<Long> lineItemIdsList = convertLineItemIds(lineItemIds);


        if (customerDetails != null && customerDetails.getAddress() != null && userId != null && !lineItemIdsList.isEmpty()) {
            System.out.println("Customer Address: " + customerDetails.getAddress());
            return customAddressService.saveAddress(customerDetails.getAddress())
                    .flatMap(a -> orderRepository.save(
                            Order.builder()
                                    .addressId(a.getId())
                                    .planIds(lineItemIdsList)
                                    .total(total)
                                    .userId(Long.valueOf(userId))
                                    .createdAt(LocalDateTime.now())
                                    .updatedAt(LocalDateTime.now())
                                    .stripeInvoiceId(session.getInvoice())
                                    .build()
                    ))
                    .flatMap(order -> sendOrderEmail(order, customerEmail))
                    .then(sendNotifications(plans, customerEmail))
                    .thenReturn("Success");
        } else {
            return Mono.error(new IllegalActionException("Customer details are missing"));
        }
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
        return boughtWebSocketClient.saveNotifications(internalBoughtBody);
    }

    private Mono<Order> getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .switchIfEmpty(Mono.error(new NotFoundEntity("order", orderId)));
    }
}
