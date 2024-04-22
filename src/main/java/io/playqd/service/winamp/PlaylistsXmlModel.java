package io.playqd.service.winamp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter(AccessLevel.PACKAGE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlaylistsXmlModel {

  private int playlists;

  @JacksonXmlProperty(localName = "playlist")
  @JacksonXmlElementWrapper(useWrapping = false)
  private List<PlaylistElement> playlistElements;

}
