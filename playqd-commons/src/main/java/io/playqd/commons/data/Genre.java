package io.playqd.commons.data;

public record Genre(String id, String name, long artistCount, long albumCount, long trackCount)
    implements Comparable<Genre> {

  @Override
  public int compareTo(Genre that) {
    return String.CASE_INSENSITIVE_ORDER.compare(this.name, that.name);
  }
}
