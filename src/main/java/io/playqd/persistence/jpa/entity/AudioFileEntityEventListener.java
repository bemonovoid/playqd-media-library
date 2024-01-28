package io.playqd.persistence.jpa.entity;

import io.playqd.api.controller.RestApiResources;
import io.playqd.config.properties.PlayqdProperties;
import jakarta.persistence.PostLoad;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
class AudioFileEntityEventListener {

  @Autowired
  private ObjectFactory<PlayqdProperties> objectFactory;

  @PostLoad
  public void afterLoad(AudioFileJpaEntity entity) {
//        var hostAddress = objectFactory.getObject().buildHostAddress();
    var hostAddress = "localhost";
    var uri = String.format("http://%s%s/%s", hostAddress, RestApiResources.AUDIO_STREAM, entity.getId());
//    entity.withAudioFilesStreamUri(uri);
  }
}
