package io.playqd.model;

import java.nio.file.Path;

public interface PlaylistFile {

  String name();

  String format();

  Path location();

  long itemsCount();
}
