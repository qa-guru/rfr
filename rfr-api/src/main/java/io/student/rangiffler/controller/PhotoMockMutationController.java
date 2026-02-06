package io.student.rangiffler.controller;

import io.student.rangiffler.model.types.Country;
import io.student.rangiffler.model.types.Photo;
import io.student.rangiffler.model.types.PhotoInput;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
@PreAuthorize("isAuthenticated()")
public class PhotoMockMutationController {

  @MutationMapping
  public Photo photo(@AuthenticationPrincipal Jwt principal,
                     @Argument PhotoInput input) {
    return Photo.newBuilder()
        .id(UUID.randomUUID().toString())
        .src(input.getSrc())
        .description(input.getDescription())
        .country(Country.newBuilder()
            .build())
        .build();
  }

  @MutationMapping
  public void deletePhoto(@AuthenticationPrincipal Jwt principal, @Argument UUID id) {

  }
}
