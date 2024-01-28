package io.playqd.service.metadata;

public interface MediaMetadataService {

  MetadataContentInfo getInfo(long sourceId);

  long clear(long sourceId);

}
