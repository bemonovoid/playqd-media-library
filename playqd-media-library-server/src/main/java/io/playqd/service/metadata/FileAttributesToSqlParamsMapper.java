package io.playqd.service.metadata;

import org.jaudiotagger.audio.AudioFile;

import java.util.Map;

@FunctionalInterface
public interface FileAttributesToSqlParamsMapper {

  Map<String, Object> toSqlParams(AudioFile jTaggerAudioFile);

}
