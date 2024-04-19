package io.playqd.controller.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter(AccessLevel.PACKAGE)
public class CreateMusicDirectoryRequest {

  @NotBlank
  private String name;

  @NotBlank
  private String path;

  private boolean autoScanOnCreate;

  private boolean autoScanOnStartUp;

  private Set<String> ignoredDirectories;

}
