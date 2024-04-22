package io.playqd.commons.data;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record Track(long id,
                    String uuid,
                    String spotifyId,
                    String title,
                    String number,
                    Artist artist,
                    Album album,
                    Length length,
                    Playback playback,
                    Rating rating,
                    AudioFormat audioFormat,
                    FileAttributes fileAttributes,
                    AdditionalInfo additionalInfo) {

  public record Artist(String id, String spotifyId, String name) {

  }

  public record Album(String id, String spotifyId, String name, String genreId, String genre) {

  }

  public record Length(int seconds, double precise, String readable) {

  }

  public record Playback(int count, @JsonFormat(pattern = "HH:mm MMM-dd-yyyy") LocalDateTime lastPlayedDate) {

  }

  public record FileAttributes(String name, String location, String extension, String size) {

  }

  public record AudioFormat(String mimeType,
                            String bitRate,
                            String sampleRate,
                            int bitsPerSample) {
  }

  public record Rating(String value) {

  }

  public record AdditionalInfo(@JsonFormat(pattern = "dd-MM-yyyy") LocalDate addedToWatchFolderDate) {

  }
}