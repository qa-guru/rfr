package io.student.rangiffler.data.entity;

import lombok.*;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendShipId implements Serializable {

  private UUID requester;
  private UUID addressee;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    FriendShipId friendShipId = (FriendShipId) o;
    return Objects.equals(requester, friendShipId.requester) && Objects.equals(addressee, friendShipId.addressee);
  }

  @Override
  public int hashCode() {
    return Objects.hash(requester, addressee);
  }
}
