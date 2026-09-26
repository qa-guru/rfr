package io.student.rangiffler.service.mock;

import io.student.rangiffler.model.types.Country;
import io.student.rangiffler.model.types.Like;
import io.student.rangiffler.model.types.Likes;
import io.student.rangiffler.model.types.Photo;
import io.student.rangiffler.model.types.Stat;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Component
public class FeedMockData {

  private static final String FEED = "query_feed.json";
  private static final String FEED_WITH_FRIENDS = "query_feed_with_friends.json";
  private static final LocalDate MOCK_CREATION_DATE = LocalDate.of(2024, 1, 1);

  private final ObjectMapper objectMapper = new ObjectMapper();

  public List<Photo> photos(boolean withFriends) {
    JsonNode photosNode = load(withFriends).path("photos").path("edges");
    List<Photo> photos = new ArrayList<>();
    for (JsonNode edge : photosNode) {
      photos.add(toPhoto(edge.path("node")));
    }
    return photos;
  }

  public List<Stat> stat(boolean withFriends) {
    List<Stat> stats = new ArrayList<>();
    for (JsonNode node : load(withFriends).path("stat")) {
      stats.add(Stat.newBuilder()
          .country(toCountry(node.path("country")))
          .count(node.path("count").asInt())
          .build());
    }
    return stats;
  }

  public Optional<Photo> findPhoto(String id) {
    return Stream.concat(photos(false).stream(), photos(true).stream())
        .filter(photo -> photo.getId().equals(id))
        .findFirst();
  }

  private JsonNode load(boolean withFriends) {
    String filename = withFriends ? FEED_WITH_FRIENDS : FEED;
    try (InputStream inputStream = new ClassPathResource("mock/" + filename).getInputStream()) {
      return objectMapper.readTree(inputStream);
    } catch (IOException e) {
      throw new RuntimeException("Failed to load mock data from " + filename, e);
    }
  }

  private Photo toPhoto(JsonNode node) {
    JsonNode likesNode = node.path("likes");
    List<Like> likes = new ArrayList<>();
    for (JsonNode likeNode : likesNode.path("likes")) {
      String user = likeNode.path("user").asString();
      likes.add(Like.newBuilder()
          .user(user)
          .username(likeNode.path("username").asString(user))
          .creationDate(MOCK_CREATION_DATE)
          .build());
    }
    return Photo.newBuilder()
        .id(node.path("id").asString())
        .src(node.path("src").asString())
        .country(toCountry(node.path("country")))
        .description(node.path("description").asString())
        .creationDate(MOCK_CREATION_DATE)
        .likes(new Likes(likesNode.path("total").asInt(), likes))
        .isOwner(node.path("isOwner").asBoolean())
        .build();
  }

  private Country toCountry(JsonNode node) {
    return Country.newBuilder()
        .code(node.path("code").asString())
        .name(node.path("name").asString())
        .flag(node.path("flag").asString())
        .build();
  }
}
