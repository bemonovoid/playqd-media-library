package io.playqd.config.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;

@Validated
@Getter
@Setter(AccessLevel.PACKAGE)
public class WinampProperties {

  private static final String DEFAULT_DATA_INDEX_FILE = "main.idx";
  private static final String DEFAULT_DATA_FILE = "main.dat";
  private static final String DEFAULT_PLAYLISTS_XML_FILE_NAME = "playlists.xml";

  private boolean enabled;

  @NotBlank
  private String dir;

  private String indexFile = DEFAULT_DATA_INDEX_FILE;

  private String dataFile = DEFAULT_DATA_FILE;

  private String playlistsXmlFileName = DEFAULT_PLAYLISTS_XML_FILE_NAME;

}
