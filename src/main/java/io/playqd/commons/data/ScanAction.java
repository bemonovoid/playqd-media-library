package io.playqd.commons.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.playqd.commons.data.WatchFolderAction;
import io.playqd.commons.data.WatchFolderActionVisitor;

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
