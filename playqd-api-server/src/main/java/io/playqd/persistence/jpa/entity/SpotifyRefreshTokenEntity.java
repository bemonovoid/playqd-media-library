package io.playqd.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table
public class SpotifyRefreshTokenEntity extends PersistableAuditableEntity {

  static final String TABLE_NAME = "spotify_refresh_token";

  private static final String COL_REFRESH_TOKEN = "refresh_token";

  @Column(name = COL_REFRESH_TOKEN, nullable = false)
  private String refreshToken;

}
