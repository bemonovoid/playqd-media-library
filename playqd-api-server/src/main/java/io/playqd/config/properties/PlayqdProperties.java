package io.playqd.config.properties;

import jakarta.validation.constraints.NotEmpty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Set;

@Validated
@Getter
@Setter(AccessLevel.PACKAGE)
public class PlayqdProperties {

  private static final String PLAYLISTS_DIR_NAME = "playlists";
  private static final String LOGS_DIR_NAME = "logs";

  private String workingDir;

  @NotEmpty
  private Set<WatchFolderProperties> folders;

  @NonNull
  private SpotifyProperties spotify;

  @NonNull
  private WinampProperties winamp;

  private MusicBeeProperties musicbee;

  public Path getWorkingDirOrDefault() {
    return Optional.ofNullable(workingDir)
        .map(Paths::get)
        .orElseGet(() -> Paths.get(System.getProperty("user.home")));
  }

  public Path getLogsDir() {
    return getWorkingDirOrDefault().resolve(LOGS_DIR_NAME);
  }

  public Path getPlaylistsDir() {
    return getWorkingDirOrDefault().resolve(PLAYLISTS_DIR_NAME);
  }
}
