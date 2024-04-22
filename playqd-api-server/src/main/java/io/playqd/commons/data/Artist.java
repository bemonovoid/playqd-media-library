package io.playqd.commons.data;

public record Artist(String id, String name, int albumsCount, int tracksCount) implements Comparable<Artist> {

  @Override
  public int compareTo(Artist that) {
    return String.CASE_INSENSITIVE_ORDER.compare(this.name, that.name);
  }
}
