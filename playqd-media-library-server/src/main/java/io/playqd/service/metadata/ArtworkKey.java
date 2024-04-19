package io.playqd.service.metadata;

import io.playqd.commons.data.ArtworkSize;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class ArtworkKey implements Serializable {

  private final String albumId;
  private final ArtworkSize artworkSize;
}
