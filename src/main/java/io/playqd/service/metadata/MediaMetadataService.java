package io.playqd.service.metadata;

import io.playqd.service.metadata.MetadataContentInfo;

public interface MediaMetadataService {

  MetadataContentInfo getInfo(long sourceId);

  long clear(long sourceId);

}
