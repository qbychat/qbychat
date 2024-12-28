package org.qbynet.chat.dgs;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsQuery;
import graphql.GraphQLContext;
import graphql.schema.DataFetchingEnvironment;
import org.jetbrains.annotations.NotNull;
import org.qbychat.graphql.types.GraphQlStatus;
import org.qbychat.graphql.types.GraphQlUser;
import org.qbynet.chat.entity.User;
import org.springframework.web.server.ServerWebExchange;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@DgsComponent
public class Query {
    @DgsQuery
    public GraphQlUser myself(@NotNull DataFetchingEnvironment dfe) {
        GraphQLContext graphQlContext = dfe.getGraphQlContext();
        User user = (User) ((ServerWebExchange) graphQlContext.get(ServerWebExchange.class)).getAttributes().get("user");
        assert user != null;
        GraphQlUser.Builder builder = GraphQlUser.newBuilder();
        if (user.getStatus() != null) {
            builder.status(GraphQlStatus.newBuilder()
                .text(user.getStatus().getText())
                .build());
        }
        if (user.getLastLoginTime() != null) {
            builder.lastLoginTime(ZonedDateTime.ofInstant(user.getLastLoginTime(), ZoneId.systemDefault()).toLocalDate());
        }
        return builder
            .id(user.getId())
            .bio(user.getBio())
            .username(user.getUsername())
            .nickname(user.getNickname())
            .registerTime(ZonedDateTime.ofInstant(user.getRegisterTime(), ZoneId.systemDefault()).toLocalDate())
            .build();
    }
}
