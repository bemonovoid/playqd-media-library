package io.playqd.service.metadata;

import java.nio.file.Path;
import java.util.Map;

@FunctionalInterface
public interface FileAttributesToSqlParamsMapper {

  Map<String, Object> toSqlParams(Path path);

}
