package io.playqd.model;

public interface MediaItem {

  String name();

  default String location() {
    return "n/a";
  }
}
