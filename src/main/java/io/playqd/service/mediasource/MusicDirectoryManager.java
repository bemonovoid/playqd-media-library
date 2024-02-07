package io.playqd.service.mediasource;

import io.playqd.commons.data.DirectoryItem;
import io.playqd.commons.data.MusicDirectory;
import io.playqd.commons.data.MusicDirectoryContentInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MusicDirectoryManager {

  List<MusicDirectory> getAll();

  MusicDirectory get(long sourceId);

  MusicDirectory create(MusicDirectory musicDirectory);

  Page<DirectoryItem> tree(Pageable page);

  Page<DirectoryItem> tree(long id, String pathBase64Encoded, Pageable page);

  MusicDirectoryContentInfo info(long sourceId);

}
