package org.qbynet.shared.graphql;

import graphql.GraphQLContext;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Locale;

public class InstantScalar {

    public static final GraphQLScalarType INSTANCE = GraphQLScalarType.newScalar()
        .name("Instant")
        .description("The Java Instant type")
        .coercing(new Coercing<Instant, String>() {
            @Override
            public @Nullable String serialize(@NotNull Object dataFetcherResult, @NotNull GraphQLContext graphQLContext, @NotNull Locale locale) throws CoercingSerializeException {
                if (dataFetcherResult instanceof Instant) {
                    return ((Instant) dataFetcherResult).toString();
                }
                return null;
            }

            @Override
            public @Nullable Instant parseValue(@NotNull Object input, @NotNull GraphQLContext graphQLContext, @NotNull Locale locale) throws CoercingParseValueException {
                if (input instanceof String) {
                    try {
                        return Instant.parse((String) input);
                    } catch (DateTimeParseException e) {
                        throw new CoercingParseValueException("Invalid Instant format.");
                    }
                }
                return null;
            }
        })
        .build();
}
