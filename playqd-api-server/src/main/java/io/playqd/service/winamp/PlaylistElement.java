package io.playqd.service.winamp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter(AccessLevel.PACKAGE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlaylistElement {

  private String id;

  private String title;

  private String filename;

  private int songs;

  private long seconds;

  private String cloud;

}
