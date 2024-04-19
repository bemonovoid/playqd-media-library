package io.playqd.persistence.jpa.dao;

import io.playqd.persistence.WinampLibraryDao;
import io.playqd.persistence.jpa.entity.WinampLibraryEntity;
import io.playqd.persistence.jpa.repository.WinampLibraryRepository;
import io.playqd.service.winamp.WinampLibrary;
import io.playqd.service.winamp.nde.NdeData;
import io.playqd.service.winamp.nde.NdeTrackRecord;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Component
class JpaWinampLibraryDao implements WinampLibraryDao {

  private final WinampLibraryRepository repository;

  JpaWinampLibraryDao(WinampLibraryRepository repository) {
    this.repository = repository;
  }

  @Override
  public WinampLibrary create(WinampLibrary winampData) {
    var entity = new WinampLibraryEntity();
    entity.setFileName(winampData.fileName());
    entity.setLocation(winampData.location().toString());
    entity.setData(serializeNdeData(winampData.data()));
    entity.setFileLastModifiedDate(winampData.fileLastModifiedDate());
    return fromEntity(repository.saveAndFlush(entity));
  }

  @Override
  public void update(WinampLibrary winampData) {
    var entity = repository.findAll().getFirst();
    entity.setData(serializeNdeData(winampData.data()));
    entity.setFileLastModifiedDate(winampData.fileLastModifiedDate());
    repository.saveAndFlush(entity);
  }

  @Override
  public Optional<WinampLibrary> get() {
    var count = repository.count();
    if (count == 0) {
      return Optional.empty();
    }
    if (count > 1) {
      throw new IllegalStateException("Winamp data table must contain just one row");
    }
    return Optional.of(fromEntity(repository.findAll().getFirst()));
  }

  private static WinampLibrary fromEntity(WinampLibraryEntity entity) {
    return new WinampLibrary(
        entity.getId(),
        entity.getFileName(),
        Paths.get(entity.getLocation()),
        deserializeNdeDataBytes(entity.getData()),
        entity.getFileLastModifiedDate());
  }

  private static byte[] serializeNdeData(NdeData ndeData) {
    try (var bos = new ByteArrayOutputStream(); var oos = new ObjectOutputStream(bos)) {
      oos.writeObject(ndeData.getRecords());
      return bos.toByteArray();
    } catch (IOException e) {
      throw new RuntimeException("Failed to serialize nde data.", e);
    }
  }

  @SuppressWarnings("unchecked")
  private static NdeData deserializeNdeDataBytes(byte[] data) {
    try (var bis = new ByteArrayInputStream(data); var ois = new ObjectInputStream(bis)) {
      return NdeData.from((List<NdeTrackRecord>) ois.readObject());
    } catch (Exception  e) {
      throw new RuntimeException("Failed to serialize nde data.", e);
    }
  }
}
