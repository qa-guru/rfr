package io.student.rangiffler.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.student.rangiffler.model.types.Country;
import io.student.rangiffler.model.types.Feed;
import io.student.rangiffler.model.types.Like;
import io.student.rangiffler.model.types.Likes;
import io.student.rangiffler.model.types.Photo;
import io.student.rangiffler.model.types.Stat;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Controller
@PreAuthorize("isAuthenticated()")
public class FeedMockQueryController {

  private final ObjectMapper objectMapper = new ObjectMapper();

  private JsonNode loadMockData(String filename) {
    try {
      ClassPathResource resource = new ClassPathResource("mock/" + filename);
      try (InputStream inputStream = resource.getInputStream()) {
        return objectMapper.readTree(inputStream);
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed to load mock data from " + filename, e);
    }
  }

  @SchemaMapping(typeName = "Feed", field = "stat")
  public List<Stat> stat(Feed feed) {
    JsonNode feedData = loadMockData(feed.getWithFriends()
        ? "query_feed_with_friends.json"
        : "query_feed.json"
    );
    JsonNode statNode = feedData.path("stat");

    List<Stat> stats = new ArrayList<>();
    if (statNode.isArray()) {
      for (JsonNode node : statNode) {
        Stat stat = Stat.newBuilder()
            .country(Country.newBuilder()
                .code(node.path("country").path("code").asText())
                .name(node.path("country").path("name").asText())
                .flag(node.path("country").path("flag").asText())
                .build())
            .count(node.path("count").asInt())
            .build();
        stats.add(stat);
      }
    }
    return stats;
  }

  @SchemaMapping(typeName = "Photo", field = "likes")
  public Likes likes(Photo photo) {
    return photo.getLikes();
  }

  @SchemaMapping(typeName = "Feed", field = "photos")
  public Page<Photo> photos(Feed feed,
                            @Argument int page,
                            @Argument int size) {
    JsonNode feedData = loadMockData(feed.getWithFriends()
        ? "query_feed_with_friends.json"
        : "query_feed.json"
    );
    JsonNode photosNode = feedData.path("photos").path("edges");

    List<Photo> photos = new ArrayList<>();
    if (photosNode.isArray()) {
      for (JsonNode edge : photosNode) {
        JsonNode node = edge.path("node");
        JsonNode likesNode = node.path("likes");

        List<Like> likesList = new ArrayList<>();
        JsonNode likesArray = likesNode.path("likes");
        if (likesArray.isArray()) {
          for (JsonNode likeNode : likesArray) {
            Like like = Like.newBuilder()
                .user(likeNode.path("user").asText())
                .build();
            likesList.add(like);
          }
        }

        Photo photo = Photo.newBuilder()
            .id(node.path("id").asText())
            .src(node.path("src").asText())
            .country(Country.newBuilder()
                .code(node.path("country").path("code").asText())
                .name(node.path("country").path("name").asText())
                .flag(node.path("country").path("flag").asText())
                .build())
            .description(node.path("description").asText())
            .likes(new Likes(likesNode.path("total").asInt(), likesList))
            .build();
        photos.add(photo);
      }
    }

    JsonNode pageInfo = feedData.path("photos").path("pageInfo");
    int totalCount = pageInfo.path("totalCount").asInt(photos.size());

    return new PageImpl<>(photos, PageRequest.of(page, size), totalCount);
  }

  @QueryMapping
  public Feed feed(@AuthenticationPrincipal Jwt principal,
                   @Argument boolean withFriends) {
    return new Feed(
        principal.getClaim("sub"),
        withFriends,
        null,
        null
    );
  }
}
