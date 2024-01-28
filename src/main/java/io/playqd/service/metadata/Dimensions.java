package io.playqd.service.metadata;

import java.io.Serializable;

public record Dimensions(int height, int width) implements Serializable {

  public static Dimensions unknown() {
    return new Dimensions(0, 0);
  }

}
