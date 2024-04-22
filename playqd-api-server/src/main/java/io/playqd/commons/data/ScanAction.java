package io.playqd.commons.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName(ScanAction.TYPE_NAME)
public class ScanAction extends WatchFolderAction {

  static final String TYPE_NAME = "scan";

  boolean deleteAllBeforeScan;

  @Override
  @JsonIgnore
  public void accept(WatchFolderActionVisitor visitor) {
    visitor.visit(this);
  }
}
