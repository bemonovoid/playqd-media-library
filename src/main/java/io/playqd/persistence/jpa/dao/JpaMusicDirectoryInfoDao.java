package io.playqd.persistence.jpa.dao;

import io.playqd.persistence.MusicDirectoryInfoDao;
import io.playqd.persistence.jpa.repository.MusicDirectoryInfoRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JpaMusicDirectoryInfoDao implements MusicDirectoryInfoDao {

  private final MusicDirectoryInfoRepository repository;

  public JpaMusicDirectoryInfoDao(MusicDirectoryInfoRepository repository) {
    this.repository = repository;
  }


}
