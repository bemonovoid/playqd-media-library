package io.playqd.service.metadata;

import io.playqd.commons.data.MusicDirectoryContentInfo;
import io.playqd.exception.CounterException;
import io.playqd.persistence.AudioFileDao;
import io.playqd.service.mediasource.MusicDirectoryManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
class AudioFileMediaMetadataServiceImpl implements MediaMetadataService {

  private final AudioFileDao audioFileDao;
  private final MusicDirectoryManager musicDirectoryManager;

  AudioFileMediaMetadataServiceImpl(AudioFileDao audioFileDao,
                                    MusicDirectoryManager musicDirectoryManager) {
    this.audioFileDao = audioFileDao;
    this.musicDirectoryManager = musicDirectoryManager;
  }

  @Override
  public MetadataContentInfo getInfo(long sourceId) throws CounterException {
    var mediaSourceContentInfo = musicDirectoryManager.info(sourceId);

    try (Stream<AudioFileAttributes> audioFileStream = audioFileDao.streamByLocationStartsWith(
        mediaSourceContentInfo.musicDirectory().path(),
        AudioFileAttributes.class)) {

      var extensionCounts = audioFileStream
          .collect(Collectors.groupingBy(AudioFileAttributes::getExtension, Collectors.counting()))
          .entrySet().stream()
          .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
          .collect(Collectors.toMap(
              Map.Entry::getKey,
              Map.Entry::getValue,
              (e1, e2) -> e1,
              LinkedHashMap::new));
      var totalCount = extensionCounts.values().stream()
          .collect(Collectors.summarizingLong(value -> value))
          .getSum();

      var diffDetails = new ArrayList<String>();

      var isInSyncWithSource =
          isInSyncWithSource(mediaSourceContentInfo, totalCount, extensionCounts, diffDetails);

      return new MetadataContentInfo(totalCount, extensionCounts, isInSyncWithSource, diffDetails);

    } catch (Exception e) {
      throw new CounterException(e);
    }
  }

  @Override
  public long clear(long sourceId) {
    return audioFileDao.deleteAllByLocationsStartsWith(musicDirectoryManager.get(sourceId).path());
  }

  private boolean isInSyncWithSource(MusicDirectoryContentInfo musicDirectoryContentInfo,
                                     long metadataTotalCount,
                                     Map<String, Long> metadataFormats,
                                     List<String> detailsHolder) {

    var mediaSource = musicDirectoryContentInfo.musicDirectory();

    if (metadataTotalCount < musicDirectoryContentInfo.totalCount()) {
      detailsHolder.add(String.format("Scanned metadata store is out of sync and is %s file(s) behind.",
          musicDirectoryContentInfo.totalCount() - metadataTotalCount));
    } else if (metadataTotalCount > musicDirectoryContentInfo.totalCount()) {
      detailsHolder.add(String.format("Scanned metadata store is out of sync and has obsolete %s file(s).",
          metadataTotalCount - musicDirectoryContentInfo.totalCount()));
    }

    var modifiedFiles = audioFileDao.streamByLocationStartsWith(mediaSource.path(), AudioFileAttributes.class)
        .parallel()
        .filter(AudioFileAttributes::wasModified)
        .toList();
    ;

    if (modifiedFiles.isEmpty()) {
      return metadataTotalCount == musicDirectoryContentInfo.totalCount()
          && metadataFormats.equals(musicDirectoryContentInfo.formats());
    } else {
      var msgTemplate = "Source file was modified. Re-sync saved metadata item with id: %s, at path: %s";
      modifiedFiles.stream()
          .map(m -> String.format(msgTemplate, m.getId(), m.getLocation()))
          .forEach(detailsHolder::add);
      return false;
    }
  }
}
