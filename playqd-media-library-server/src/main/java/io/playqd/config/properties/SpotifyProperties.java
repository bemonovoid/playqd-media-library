package io.playqd.config.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter(AccessLevel.PACKAGE)
@Validated
public class SpotifyProperties {

  private boolean enabled;

  @NotBlank
  private String apiBaseUrl = "https://api.spotify.com";

  @NotBlank
  private String accountBaseUrl = "https://accounts.spotify.com";

  @NonNull
  private String apiVersion = "v1";

  @NonNull
  private String redirectUri;

  @NotBlank
  private String clientId;

  @NotBlank
  private String userId;

  @NotBlank
  private String secret;

  private String refreshToken;

}
