package org.qbynet.chat.graphql;

import graphql.GraphQLContext;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.qbynet.chat.entity.MemberPermission;

import java.util.Locale;

public class MemberPermissionScalar {
    public static final GraphQLScalarType INSTANCE = GraphQLScalarType.newScalar()
        .name("MemberPermission")
        .description("The MemberPermission enum")// 标量的描述
        .coercing(new Coercing<MemberPermission, String>() {
            @Override
            public @Nullable String serialize(@NotNull Object dataFetcherResult, @NotNull GraphQLContext graphQLContext, @NotNull Locale locale) throws CoercingSerializeException {
                if (dataFetcherResult instanceof MemberPermission) {
                    return dataFetcherResult.toString();
                }
                return null;
            }

            @Override
            public @Nullable MemberPermission parseValue(@NotNull Object input, @NotNull GraphQLContext graphQLContext, @NotNull Locale locale) throws CoercingParseValueException {
                if (input instanceof String) {
                    try {
                        return MemberPermission.valueOf((String) input);
                    } catch (IllegalArgumentException e) {
                        throw new CoercingParseValueException("Invalid value.");
                    }
                }
                return null;
            }
        })
        .build();
}
