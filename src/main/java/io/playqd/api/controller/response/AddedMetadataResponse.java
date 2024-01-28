package io.playqd.api.controller.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record AddedMetadataResponse(@JsonProperty("search_period")
                                    SearchPeriod searchPeriod,
                                    int tracks,
                                    List<AddedItem> items) {

  public record SearchPeriod(@JsonFormat(pattern = "dd-MM-yyyy")
                             @JsonSerialize(using = LocalDateSerializer.class)
                             LocalDate from,
                             @JsonFormat(pattern = "dd-MM-yyyy")
                             @JsonSerialize(using = LocalDateSerializer.class)
                             LocalDate to) {
  }

  @JsonPropertyOrder({"date", "metadata"})
  public record AddedItem(@JsonFormat(pattern = "MMMM dd, yyyy")
                          @JsonSerialize(using = LocalDateSerializer.class)
                          LocalDate date,
                          List<MetadataItem> metadata) implements Comparable<AddedItem> {
    @Override
    public int compareTo(AddedItem that) {
      return that.date().compareTo(date());
    }
  }

  public record MetadataItem(@JsonProperty("artist_id") String artistId,
                             @JsonProperty("artist_name") String artistName,
                             @JsonProperty("album_id") String albumId,
                             @JsonProperty("album_name") String albumName) implements Comparable<MetadataItem> {

    @Override
    public int compareTo(MetadataItem that) {
      return albumName.compareTo(that.albumName());
    }
  }
}
