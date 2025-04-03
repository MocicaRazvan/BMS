package com.mocicarazvan.rediscache.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.CodeSignature;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.util.Pair;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HexFormat;

@Slf4j
public class AspectUtils {

    private final ExpressionParser expressionParser = new SpelExpressionParser();
    private static final ThreadLocal<MessageDigest> MD5_DIGEST = ThreadLocal.withInitial(() -> {
        try {
            return MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            log.error("Error creating MD5 digest", e);
            throw new RuntimeException("Error creating MD5 digest", e);
        }
    });

    public String extractKeyFromAnnotation(String keyPath, ProceedingJoinPoint joinPoint) {
        if (keyPath == null || keyPath.isEmpty()) {
            throw new RuntimeException("RedisReactiveCache annotation missing key");
        }
        if (keyPath.contains("#") || keyPath.contains("'")) {
            return assertString(evaluateSpelExpressionForObject(keyPath, joinPoint.getTarget(), joinPoint));
        }
        return keyPath;
    }

    public String getKeyVal(JoinPoint joinPoint, String key, boolean useArgsHash) {
        String cacheKey = resolveKey(joinPoint, key);
        if (useArgsHash)
            return cacheKey + "_" + Arrays.hashCode(joinPoint.getArgs());
        else
            return cacheKey;
    }

    public String assertString(Object s) {
        if (s == null) {
            throw new RuntimeException("RedisReactiveCacheAdd: Annotated method has invalid key to return non null value");
        }
        return s.toString();
    }

    public Long assertLong(Object id, Long defaultValue) {
        if (id == null) {
            return defaultValue;
        }

        try {
            return switch (id) {
                case Long l -> l;
                case String s -> Long.parseLong(s);
                case Number number -> number.longValue();
                default -> Long.valueOf(String.valueOf(id));
            };
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public Long assertLong(Object id) {
        if (id == null) {
            throw new RuntimeException("RedisReactiveCache: Annotated method has invalid item, expected item to return non null value " + null);
        }
        try {
            return switch (id) {
                case Long l -> l;
                case String s -> Long.parseLong(s);
                case Number number -> number.longValue();
                default -> Long.valueOf(String.valueOf(id));
            };
        } catch (NumberFormatException e) {
            throw new RuntimeException("RedisReactiveCache: Annotated method has invalid item " + id);
        }

    }

    public String appendStars(String key) {
        return "*" + key + "*";
    }

    public Object evaluateSpelExpressionForObject(String spelExpression, Object rootObject, ProceedingJoinPoint joinPoint) {
        if (spelExpression == null || spelExpression.isEmpty()) {
            throw new IllegalArgumentException("SpEL expression cannot be null or empty");
        }

        StandardEvaluationContext context = new StandardEvaluationContext(rootObject);

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] parameterNames = signature.getParameterNames();
        Object[] parameterValues = joinPoint.getArgs();

        for (int i = 0; i < parameterNames.length; i++) {
            context.setVariable(parameterNames[i], parameterValues[i]);
        }

        return expressionParser.parseExpression(spelExpression).getValue(context);
    }

    public Object evaluateSpelExpression(String spelExpression, ProceedingJoinPoint joinPoint) {
        if (spelExpression == null || spelExpression.isEmpty()) {
            throw new IllegalArgumentException("SpEL expression cannot be null or empty");
        }
        if (!spelExpression.startsWith("#")) {
            return spelExpression;
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] parameterNames = signature.getParameterNames();
        Object[] parameterValues = joinPoint.getArgs();

        StandardEvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < parameterNames.length; i++) {
            context.setVariable(parameterNames[i], parameterValues[i]);
        }


        return expressionParser.parseExpression(spelExpression).getValue(context);
    }

    public String getHashString(JoinPoint joinPoint, String key, String methodName) {
        try {
            String argsString = Arrays.deepToString(joinPoint.getArgs()) + key + methodName;
            // simple algorithm no need for sha
//            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = MD5_DIGEST.get().digest(argsString.getBytes(StandardCharsets.UTF_8));


            return HexFormat.of().formatHex(hashBytes);

        } catch (Exception e) {
            throw new RuntimeException("Error generating hash", e);
        }
    }

    public Method getMethod(JoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        return methodSignature.getMethod();
    }

    public TypeReference<?> getTypeReference(Method method) {
        return new TypeReference<>() {
            @Override
            public Type getType() {
                return getMethodActualReturnType(method);
            }
        };
    }

    public Type getMethodActualReturnType(Method method) {
        return ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0];
    }

    private String resolveKey(JoinPoint joinPoint, String key) {
        if (StringUtils.hasText(key)) {
            if (key.contains("#") || key.contains("'")) {
                String[] parameterNames = getParamNames(joinPoint);
                Object[] args = joinPoint.getArgs();
                StandardEvaluationContext context = new StandardEvaluationContext();
                for (int i = 0; i < parameterNames.length; i++) {
                    context.setVariable(parameterNames[i], args[i]);
                }
                return (String) expressionParser.parseExpression(key).getValue(context);
            }
            return key;
        }
        throw new RuntimeException("RedisReactiveCache annotation missing key");
    }

    private String[] getParamNames(JoinPoint joinPoint) {
        CodeSignature codeSignature = (CodeSignature) joinPoint.getSignature();
        return codeSignature.getParameterNames();
    }

    public void validateReturnTypeIsMonoPairClass(Method method, Class<?> clazz) {
        Class<?> returnType = method.getReturnType();

        if (returnType.isAssignableFrom(Mono.class)) {
            Type genericReturnType = method.getGenericReturnType();

            if (genericReturnType instanceof ParameterizedType monoType) {
                Type[] monoTypeArguments = monoType.getActualTypeArguments();

                if (monoTypeArguments.length == 1 && monoTypeArguments[0] instanceof ParameterizedType pairType) {
                    if (pairType.getRawType().equals(Pair.class)) {
                        Type[] pairTypeArguments = pairType.getActualTypeArguments();

                        if (pairTypeArguments.length == 2 && pairTypeArguments[1].equals(clazz)) {
                            return;
                        }
                    }
                }
            }
        }
        throw new RuntimeException("RedisReactiveCache: Annotated method has invalid return type, expected return type to be Mono<Pair<?, " + clazz.getSimpleName() + ">>");
    }
}
