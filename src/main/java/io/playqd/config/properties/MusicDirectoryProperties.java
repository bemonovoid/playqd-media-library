package io.playqd.config.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;

import java.util.Set;

@Getter
@Setter(AccessLevel.PACKAGE)
@Validated
public class MusicDirectoryProperties {

  @NotBlank
  private String name;

  @NotBlank
  private String path;

  private boolean scanOnStart;

  private boolean watchable;

  private Set<String> ignoreDirs;

}
