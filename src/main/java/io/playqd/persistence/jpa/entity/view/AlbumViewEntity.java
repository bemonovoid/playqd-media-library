package io.playqd.persistence.jpa.entity.view;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import org.springframework.data.annotation.Immutable;

@Entity
@Table(name = AlbumViewEntity.VIEW_NAME)
@Immutable
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AlbumViewEntity {

  static final String VIEW_NAME = "album_view";

  private static final String COL_GENRE_ID = "genre_id";
  private static final String COL_ARTIST_ID = "artist_id";
  private static final String COL_ARTIST_NAME = "artist_name";
  private static final String COL_TOTAL_TRACKS = "total_tracks";
  private static final String COL_RELEASE_DATE = "release_date";
  private static final String COL_ARTWORK_EMBEDDED = "artwork_embedded";

  @Id
  private String id;

  private String name;

  @Column(name = COL_GENRE_ID)
  private String genreId;

  private String genre;

  @Column(name = COL_RELEASE_DATE)
  private String releaseDate;

  @Column(name = COL_ARTIST_ID)
  private String artistId;

  @Column(name = COL_ARTIST_NAME)
  private String artistName;

  @Column(name = COL_ARTWORK_EMBEDDED)
  private boolean artworkEmbedded;

  @Column(name = COL_TOTAL_TRACKS)
  private int totalTracks;

}
