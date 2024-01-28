package io.playqd.persistence.jpa.entity.view;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import org.springframework.data.annotation.Immutable;

@Entity
@Immutable
@Table(name = GenreViewEntity.VIEW_NAME)
@Getter
public class GenreViewEntity {

  static final String VIEW_NAME = "genre_view";

  private static final String COL_TOTAL_ARTISTS = "total_artists";
  private static final String COL_TOTAL_ALBUMS = "total_albums";
  private static final String COL_TOTAL_TRACKS = "total_tracks";

  @Id
  private String id;

  private String name;

  @Column(name = COL_TOTAL_ARTISTS)
  private long totalArtists;

  @Column(name = COL_TOTAL_ALBUMS)
  private long totalAlbums;

  @Column(name = COL_TOTAL_TRACKS)
  private long totalTracks;

}
