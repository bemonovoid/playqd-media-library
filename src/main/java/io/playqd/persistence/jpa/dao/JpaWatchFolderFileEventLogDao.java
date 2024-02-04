package io.playqd.persistence.jpa.dao;

import io.playqd.model.event.AudioFileMetadataAddedEvent;
import io.playqd.persistence.WatchFolderFileEventLogDao;
import io.playqd.persistence.jpa.entity.WatchFolderLastFileEventLogJpaEntity;
import io.playqd.persistence.jpa.repository.WatchFolderLastFileEventLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
class JpaWatchFolderFileEventLogDao implements WatchFolderFileEventLogDao {

  private final WatchFolderLastFileEventLogRepository repository;

  JpaWatchFolderFileEventLogDao(WatchFolderLastFileEventLogRepository repository) {
    this.repository = repository;
  }

  @Override
  public boolean hasEvents() {
    return repository.count() > 0;
  }

  @Override
  public LocalDate getLastAddedDate() {
    return repository.findAll().getFirst().getLastAddedDate();
  }

  @Async
  @EventListener(AudioFileMetadataAddedEvent.class)
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void handleEvent(AudioFileMetadataAddedEvent event) {

    log.info("Received event: {}. Updating {} table with new event dates.",
        event, WatchFolderLastFileEventLogJpaEntity.TABLE_NAME);

    var entity = Optional.of(repository.findAll())
        .filter(entities -> !entities.isEmpty())
        .map(List::getFirst)
        .orElseGet(WatchFolderLastFileEventLogJpaEntity::new);

    var currentLastAddedDate = entity.getLastAddedDate();
    var newLastAddedDate = event.addedToWatchFolderDate();

    if (currentLastAddedDate != null) {
      if (currentLastAddedDate.equals(newLastAddedDate)) {
        log.info("Current and new last added to watch folder dates are the same. Nothing to update.");
        return;
      }
      if (currentLastAddedDate.isAfter(newLastAddedDate)) {
        log.warn("If old metadata was manually removed from database the last added to watch folder date " +
            "may be too far behind. In this case use previous last added to watch folder date.");
        return;
      }
    }

    entity.setLastAddedDate(newLastAddedDate);

    repository.saveAndFlush(entity);
  }

}
