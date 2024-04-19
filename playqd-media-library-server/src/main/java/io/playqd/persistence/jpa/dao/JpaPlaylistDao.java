package io.playqd.persistence.jpa.dao;

import io.playqd.commons.data.Playlist;
import io.playqd.persistence.PlaylistDao;
import io.playqd.persistence.jpa.entity.PlaylistEntity;
import io.playqd.persistence.jpa.repository.PlaylistRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Component
class JpaPlaylistDao implements PlaylistDao {

  private final PlaylistRepository repository;

  JpaPlaylistDao(PlaylistRepository repository) {
    this.repository = repository;
  }

  @Override
  public Optional<Playlist> getById(long id) {
    return repository.findById(id).map(JpaPlaylistDao::fromEntity);
  }

  @Override
  public Optional<Playlist> getByUUID(String uuid) {
    return repository.findByUuid(uuid).map(JpaPlaylistDao::fromEntity);
  }

  @Override
  public List<Playlist> getPlaylists() {
    return repository.findAll().stream().map(JpaPlaylistDao::fromEntity).toList();
  }

  @Override
  public Playlist create(Playlist playlist) {
    var entity = new PlaylistEntity();
    entity.setFormat(playlist.format());
    entity.setUuid(playlist.uuid());
    entity.setTitle(playlist.title());
    entity.setFileName(playlist.fileName());
    entity.setLocation(playlist.location().toString());
    entity.setFileLastModifiedDate(playlist.fileLastModifiedDate());
    return fromEntity(repository.saveAndFlush(entity));
  }

  @Override
  @Transactional
  public Playlist update(Playlist playlist) {
    var entity = repository.findByUuid(playlist.uuid()).get();
    if (StringUtils.hasLength(playlist.title()) && !entity.getTitle().equals(playlist.title())) {
      entity.setTitle(playlist.title());
    }
    var playlistLastModifiedDate = playlist.fileLastModifiedDate();
    if (playlistLastModifiedDate != null && playlistLastModifiedDate.isAfter(entity.getFileLastModifiedDate())) {
      entity.setFileLastModifiedDate(playlistLastModifiedDate);
    }
    return fromEntity(repository.saveAndFlush(entity));
  }

  @Override
  @Transactional
  public void delete(Playlist playlist) {
    repository.deleteByUuid(playlist.uuid());
  }

  private static Playlist fromEntity(PlaylistEntity entity) {
    return new Playlist(
        entity.getId(),
        entity.getFormat(),
        entity.getUuid(),
        entity.getTitle(),
        entity.getFileName(),
        Paths.get(entity.getLocation()),
        entity.getFileLastModifiedDate());
  }
}
