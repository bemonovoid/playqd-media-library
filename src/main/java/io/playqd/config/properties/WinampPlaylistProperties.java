package io.playqd.config.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;

@Validated
@Getter
@Setter(AccessLevel.PACKAGE)
public class WinampPlaylistProperties {

  private static final String DEFAULT_PLAYLISTS_XML_FILE_NAME = "playlists.xml";

  @NotBlank
  private String dir;

  private String playlistsXmlFileName = DEFAULT_PLAYLISTS_XML_FILE_NAME;

}
