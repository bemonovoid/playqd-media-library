package io.playqd.controller.request;

import io.playqd.commons.data.PlaylistProvider;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter(AccessLevel.PACKAGE)
public class SyncPlaylistsRequest {

  @NotNull
  private PlaylistProvider source;

  @NotNull
  private PlaylistProvider target;

}
