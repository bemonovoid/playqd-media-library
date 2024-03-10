package io.playqd.api.controller;

import io.playqd.model.event.AudioFileByteStreamRequestedEvent;
import io.playqd.persistence.AudioFileDao;
import io.playqd.service.WatchFolderFilePathResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.AbstractMessageConverterMethodProcessor;

import java.util.Arrays;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping(RestApiResources.AUDIO_STREAM)
class AudioStreamController {

  private final AudioFileDao audioFileDao;
  private final ApplicationEventPublisher eventPublisher;
  private final WatchFolderFilePathResolver watchFolderFilePathResolver;

  AudioStreamController(AudioFileDao audioFileDao,
                        ApplicationEventPublisher eventPublisher,
                        WatchFolderFilePathResolver watchFolderFilePathResolver) {
    this.audioFileDao = audioFileDao;
    this.eventPublisher = eventPublisher;
    this.watchFolderFilePathResolver = watchFolderFilePathResolver;
  }

  /**
   * See: Spring's {@link AbstractMessageConverterMethodProcessor} (line: 186 & 194) implementation that handles byte ranges
   *
   * @param audioFileId
   * @param httpHeaders
   * @return Audio file stream at the given byte range.
   */
  @GetMapping("/{trackId}")
  ResponseEntity<Resource> audioTrackStream(@PathVariable String trackId, @RequestHeader HttpHeaders httpHeaders) {

    var audioFile = audioFileDao.getAudioFileByTrackId(trackId);
    var audioFilePath = watchFolderFilePathResolver.unRelativize(audioFile);

    log.info("\n---Processed audio streaming info---\nTrack id: {}\nRange: {}\nResource externalUrl: {}\nContent-Type: {}",
        trackId,
        Arrays.toString(httpHeaders.getRange().toArray()),
        audioFilePath,
        audioFile.mimeType());

    getHttpRangeRequestIfExists(httpHeaders).ifPresentOrElse(
        httpRange -> {
          // 'getRangeStart' length is ignored for ByteRange and can be anything.
          if (httpRange.getRangeStart(0) == 0) {
            eventPublisher.publishEvent(new AudioFileByteStreamRequestedEvent(audioFile.id()));
          }
        },
        () -> eventPublisher.publishEvent(new AudioFileByteStreamRequestedEvent(audioFile.id())));

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_TYPE, audioFile.mimeType())
        .body(new FileSystemResource(audioFilePath));
  }

  private Optional<HttpRange> getHttpRangeRequestIfExists(HttpHeaders httpHeaders) {
    if (httpHeaders.isEmpty()) {
      return Optional.empty();
    }
    if (CollectionUtils.isEmpty(httpHeaders.getRange())) {
      return Optional.empty();
    }
    if (httpHeaders.getRange().size() > 1) {
      log.warn("'Range' header contains multiple ranges. The first range is being used.");
    }
    if (!httpHeaders.getRange().getClass().getSimpleName().equals("ByteRange")) {
      return Optional.empty();
    }
    return Optional.of(httpHeaders.getRange().get(0));
  }
}