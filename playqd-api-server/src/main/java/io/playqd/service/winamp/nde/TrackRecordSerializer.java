package io.playqd.service.winamp.nde;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

final class TrackRecordSerializer {

  static NdeTrackRecord serialize(Map<ColumnName, Object> item) {
    return new NdeTrackRecord(
        item.get(ColumnName.filename).toString(),
        Optional.ofNullable(item.get(ColumnName.title)).map(Object::toString).orElse(null),
        Optional.ofNullable(item.get(ColumnName.artist)).map(Object::toString).orElse(null),
        Optional.ofNullable(item.get(ColumnName.album)).map(Object::toString).orElse(null),
        Optional.ofNullable(item.get(ColumnName.year)).map(Integer.class::cast).orElse(0),
        Optional.ofNullable(item.get(ColumnName.genre)).map(Object::toString).orElse(null),
        Optional.ofNullable(item.get(ColumnName.comment)).map(Object::toString).orElse(null),
        Optional.ofNullable(item.get(ColumnName.trackno)).map(Integer.class::cast).orElse(0),
        Optional.ofNullable(item.get(ColumnName.length)).map(Integer.class::cast).orElse(0),
        Optional.ofNullable(item.get(ColumnName.type)).map(Integer.class::cast).orElse(0),
        Optional.ofNullable(item.get(ColumnName.lastupd)).map(LocalDateTime.class::cast).orElse(null),
        Optional.ofNullable(item.get(ColumnName.lastplay)).map(LocalDateTime.class::cast).orElse(null),
        Optional.ofNullable(item.get(ColumnName.rating)).map(Integer.class::cast).orElse(null),
        Optional.ofNullable(item.get(ColumnName.tuid2)).map(Object::toString).orElse(null),
        Optional.ofNullable(item.get(ColumnName.playcount))
            .map(i -> new AtomicInteger((Integer) i)).orElse(new AtomicInteger()),
        Optional.ofNullable(item.get(ColumnName.filetime)).map(LocalDateTime.class::cast).orElse(null),
        Optional.ofNullable(item.get(ColumnName.filesize)).map(Long.class::cast).orElse(0L),
        Optional.ofNullable(item.get(ColumnName.bitrate)).map(Integer.class::cast).orElse(0),
        Optional.ofNullable(item.get(ColumnName.disc)).map(Integer.class::cast).orElse(0),
        Optional.ofNullable(item.get(ColumnName.albumartist)).map(Object::toString).orElse(null),
        Optional.ofNullable(item.get(ColumnName.replaygain_album_gain)).map(Object::toString).orElse(null),
        Optional.ofNullable(item.get(ColumnName.replaygain_track_gain)).map(Object::toString).orElse(null),
        Optional.ofNullable(item.get(ColumnName.publisher)).map(Object::toString).orElse(null),
        Optional.ofNullable(item.get(ColumnName.composer)).map(Object::toString).orElse(null),
        Optional.ofNullable(item.get(ColumnName.bpm)).map(Integer.class::cast).orElse(0),
        Optional.ofNullable(item.get(ColumnName.discs)).map(Integer.class::cast).orElse(0),
        Optional.ofNullable(item.get(ColumnName.tracks)).map(Integer.class::cast).orElse(0),
        Optional.ofNullable(item.get(ColumnName.ispodcast)).map(Integer.class::cast).map(v -> v > 0).orElse(false),
        Optional.ofNullable(item.get(ColumnName.podcastchannel)).map(Object::toString).orElse(null),
        Optional.ofNullable(item.get(ColumnName.podcastpubdate)).map(LocalDateTime.class::cast).orElse(null),
        Optional.ofNullable(item.get(ColumnName.GracenoteFileID)).map(Object::toString).orElse(null),
        Optional.ofNullable(item.get(ColumnName.GracenoteExtData)).map(Object::toString).orElse(null),
        Optional.ofNullable(item.get(ColumnName.lossless)).map(Integer.class::cast).map(v -> v > 0).orElse(false),
        Optional.ofNullable(item.get(ColumnName.category)).map(Object::toString).orElse(null),
        Optional.ofNullable(item.get(ColumnName.codec)).map(Object::toString).orElse(null),
        Optional.ofNullable(item.get(ColumnName.director)).map(Object::toString).orElse(null),
        Optional.ofNullable(item.get(ColumnName.producer)).map(Object::toString).orElse(null),
        Optional.ofNullable(item.get(ColumnName.width)).map(Integer.class::cast).orElse(0),
        Optional.ofNullable(item.get(ColumnName.height)).map(Integer.class::cast).orElse(0),
        Optional.ofNullable(item.get(ColumnName.mimetype)).map(Object::toString).orElse(null),
        Optional.ofNullable(item.get(ColumnName.dateadded)).map(LocalDateTime.class::cast).orElse(null)
    );
  }

  private TrackRecordSerializer () {

  }
}
