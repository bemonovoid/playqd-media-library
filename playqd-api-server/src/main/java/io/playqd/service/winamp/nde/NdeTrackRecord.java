package io.playqd.service.winamp.nde;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

public record NdeTrackRecord(
    String fileName,
    String title,
    String artist,
    String album,
    int year,
    String genre,
    String comment,
    int trackNo,
    int length,
    int type,
    LocalDateTime lastUpd,
    LocalDateTime lastPlay,
    Integer rating,
    String tuId2,
    AtomicInteger playCount,
    LocalDateTime fileTime,
    long fileSize,
    int bitRate,
    int disc,
    String albumArtist,
    String replayGainAlbumGain,
    String replayGainTrackGain,
    String publisher,
    String composer,
    int bpm,
    int discs,
    int tracks,
    boolean isPodcast,
    String podcastChannel,
    LocalDateTime podcastPubDate,
    String graceNoteFileId,
    String graceNoteExtData,
    boolean lossless,
    String category,
    String codec,
    String director,
    String producer,
    int width,
    int height,
    String mimeType,
    LocalDateTime dateAdded
) implements Serializable {

  public boolean wasPlayed() {
    return playCount != null && playCount().get() > 0 && lastPlay() != null;
  }

}
