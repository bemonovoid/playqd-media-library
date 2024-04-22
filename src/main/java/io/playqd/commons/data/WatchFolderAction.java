package io.playqd.commons.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter(AccessLevel.PACKAGE)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(@JsonSubTypes.Type(value = ScanAction.class, name = ScanAction.TYPE_NAME))
public abstract class WatchFolderAction implements VisitableWatchFolderAction {

  @Positive
  private long id;

  @Override
  @JsonIgnore
  public void accept(WatchFolderActionVisitor visitor) {
    visitor.visit(this);
  }
}
