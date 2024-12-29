package org.qbynet.authorization.controller.graphql;

import jakarta.annotation.Resource;
import org.qbynet.authorization.entity.Account;
import org.qbynet.authorization.entity.config.AuthConfig;
import org.qbynet.authorization.entity.dto.RegisterAppDTO;
import org.qbynet.authorization.entity.vo.JsonClientVO;
import org.qbynet.authorization.service.AccountService;
import org.qbynet.authorization.service.RegisteredClientService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class RegisteredClientController {
    @Resource
    RegisteredClientService registeredClientService;

    @Resource
    AccountService accountService;

    @Resource
    AuthConfig authConfig;

    @QueryMapping
    public List<String> availableScopes() {
        return authConfig.getScopes();
    }

    @QueryMapping
    @Secured("ROLE_USER")
    public List<JsonClientVO> myApps() {
        Account account = accountService.currentAccount();
        return registeredClientService.findAllByOwner(account).stream().map(JsonClientVO::from).toList();
    }

    @MutationMapping
    @Secured("ROLE_USER")
    public JsonClientVO registerApp(@Argument RegisterAppDTO input) {
        Account account = accountService.currentAccount();
        return JsonClientVO.from(registeredClientService.register(input, account));
    }
}
