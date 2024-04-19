package io.playqd.service.metadata;

import java.io.Serializable;
import java.util.Optional;

public interface MetadataFile extends Serializable {

  static MetadataFile emptyFile() {
    return new MetadataFile() {
    };
  }

  default Optional<String> performer() {
    return Optional.empty();
  }

  default Optional<String> title() {
    return Optional.empty();
  }

  default Optional<String> remDate() {
    return Optional.empty();
  }

  default Optional<String> remGenre() {
    return Optional.empty();
  }

  default Optional<String> remComment() {
    return Optional.empty();
  }

}
