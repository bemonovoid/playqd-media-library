package io.playqd.persistence.jpa.entity.view;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import org.springframework.data.annotation.Immutable;

@Entity
@Immutable
@Table(name = ArtistViewEntity.VIEW_NAME)
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ArtistViewEntity implements Comparable<ArtistViewEntity> {

  static final String VIEW_NAME = "artist_view";

  private static final String COL_TOTAL_ALBUMS = "total_albums";
  private static final String COL_TOTAL_TRACKS = "total_tracks";

  @Id
  private String id;

  private String name;

  private String country;

  @Column(name = COL_TOTAL_ALBUMS)
  @JsonProperty(COL_TOTAL_ALBUMS)
  private int totalAlbums;

  @Column(name = COL_TOTAL_TRACKS)
  @JsonProperty(COL_TOTAL_TRACKS)
  private int totalTracks;

  @Override
  public int compareTo(ArtistViewEntity that) {
    return String.CASE_INSENSITIVE_ORDER.compare(this.name, that.name);
  }

}
