package io.playqd.persistence;

import io.playqd.service.winamp.WinampLibrary;

import java.util.Optional;

public interface WinampLibraryDao {

  Optional<WinampLibrary> get();

  WinampLibrary create(WinampLibrary winampData);

  void update(WinampLibrary newWinampData);
}
