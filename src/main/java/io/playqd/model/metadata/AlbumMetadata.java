package io.playqd.model.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.playqd.model.ReleaseType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
public class AlbumMetadata extends MediaMetadata {

  static final String TYPE_NAME = "album";
  private String artistId;
  @JsonProperty("release_type")
  private ReleaseType releaseType;
  @JsonProperty("release_date")
  private String releaseDate;

  public AlbumMetadata() {
    setType(TYPE_NAME);
  }

  @Override
  public final boolean isAlbum() {
    return true;
  }

  @Override
  public final AlbumMetadata getAsAlbum() {
    return this;
  }
}
