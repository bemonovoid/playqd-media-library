package io.playqd.config.properties;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;

@Validated
@Getter
@Setter(AccessLevel.PACKAGE)
public class MusicBeeProperties {

  private boolean enabled;

  private String playlistsDir;

  private String exportedPlaylistsDir;

}
