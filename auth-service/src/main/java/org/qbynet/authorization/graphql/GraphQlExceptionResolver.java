package org.qbynet.authorization.graphql;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.jetbrains.annotations.NotNull;
import org.qbynet.shared.exception.BadRequest;
import org.qbynet.shared.exception.Forbidden;
import org.qbynet.shared.exception.NotFound;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Component;

@Component
public class GraphQlExceptionResolver extends DataFetcherExceptionResolverAdapter {
    @Override
    protected GraphQLError resolveToSingleError(@NotNull Throwable ex, @NotNull DataFetchingEnvironment env) {
        GraphqlErrorBuilder<?> builder = GraphqlErrorBuilder.newError()
                .errorType(ErrorType.INTERNAL_ERROR)
                .path(env.getExecutionStepInfo().getPath())
                .location(env.getField().getSourceLocation())
                .message(ex.getMessage());
        if (ex instanceof BadRequest || ex instanceof IllegalArgumentException) {
            builder.errorType(ErrorType.BAD_REQUEST);
        } else if (ex instanceof Forbidden) {
            builder.errorType(ErrorType.FORBIDDEN);
        } else if (ex instanceof NotFound) {
            builder.errorType(ErrorType.NOT_FOUND);
        }
        return builder.build();
    }
}
