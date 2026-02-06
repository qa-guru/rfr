package io.student.rangiffler.controller;

import graphql.schema.DataFetchingEnvironment;
import io.student.rangiffler.model.types.User;
import io.student.rangiffler.service.api.UserService;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;

@Controller
@PreAuthorize("isAuthenticated()")
public class UserQueryController {

  private final UserService userService;

  @Autowired
  public UserQueryController(UserService userService) {
    this.userService = userService;
  }

  @SchemaMapping(typeName = "User", field = "friends")
  public Page<User> friends(User user,
                            @Argument int page,
                            @Argument int size,
                            @Argument @Nullable String searchQuery) {
    return userService.friends(
        user.getUsername(),
        PageRequest.of(page, size),
        searchQuery
    );
  }

  @SchemaMapping(typeName = "User", field = "incomeInvitations")
  public Page<User> incomeInvitations(User user,
                                      @Argument int page,
                                      @Argument int size,
                                      @Argument @Nullable String searchQuery) {
    return userService.incomeInvitations(
        user.getUsername(),
        PageRequest.of(page, size),
        searchQuery
    );
  }

  @SchemaMapping(typeName = "User", field = "outcomeInvitations")
  public Page<User> outcomeInvitations(User user,
                                       @Argument int page,
                                       @Argument int size,
                                       @Argument @Nullable String searchQuery) {
    return userService.outcomeInvitations(
        user.getUsername(),
        PageRequest.of(page, size),
        searchQuery
    );
  }

  @QueryMapping
  public User user(@AuthenticationPrincipal Jwt principal,
                   DataFetchingEnvironment env) {
    return userService.createNewUserIfNotPresent(principal.getClaim("sub"));
  }

  @QueryMapping
  public Page<User> users(@AuthenticationPrincipal Jwt principal,
                          @Argument int page,
                          @Argument int size,
                          @Argument @Nullable String searchQuery) {
    return userService.allUsers(
        principal.getClaim("sub"),
        PageRequest.of(page, size),
        searchQuery
    );
  }
}
