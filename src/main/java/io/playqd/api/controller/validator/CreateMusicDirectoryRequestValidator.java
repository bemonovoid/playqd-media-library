package io.playqd.api.controller.validator;

import io.playqd.api.controller.request.CreateMusicDirectoryRequest;
import io.playqd.commons.data.MusicDirectory;
import io.playqd.service.mediasource.MusicDirectoryManager;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.Optional;

@Slf4j
class CreateMusicDirectoryRequestValidator
    implements ConstraintValidator<ValidCreateMusicDirectoryRequest, CreateMusicDirectoryRequest> {

  private final MusicDirectoryManager musicDirectoryManager;

  CreateMusicDirectoryRequestValidator(MusicDirectoryManager musicDirectoryManager) {
    this.musicDirectoryManager = musicDirectoryManager;
  }

  @Override
  public boolean isValid(CreateMusicDirectoryRequest request, ConstraintValidatorContext context) {

    context.disableDefaultConstraintViolation();


    if (!pathIsValid(request, context)) {
      return false;
    }

    return validateNoSourceWithTheSamePath(request, context);
  }

  private boolean validateNoSourceWithTheSamePath(CreateMusicDirectoryRequest request,
                                                  ConstraintValidatorContext context) {

    var path = Paths.get(request.getPath());

    Optional<MusicDirectory> existingMusicDirectoryOpt = musicDirectoryManager.getAll().stream()
        .filter(aSource -> path.startsWith(aSource.path()))
        .findFirst();

    if (existingMusicDirectoryOpt.isPresent()) {
      var existingMusicDirectory = existingMusicDirectoryOpt.get();
      var message = String.format("Media source containing same path already exists. Source id: %s, path: %s",
          existingMusicDirectory.id(), existingMusicDirectory.path());
      addErrorMessage(context, message);
      return false;
    }

    return true;
  }

  private boolean pathIsValid(CreateMusicDirectoryRequest request, ConstraintValidatorContext context) {
    try {
      var path = Paths.get(request.getPath());
      if (!Files.exists(path)) {
        String message = String.format("Path does not exist: %s", path);
        log.error(message);
        addErrorMessage(context, message);
        return false;
      }
      return true;
    } catch (InvalidPathException e) {
      var message = String.format("Path is invalid. Verify the path is correct: %s", request.getPath());
      log.error(message);
      addErrorMessage(context, message);
      return false;
    }
  }

  private void addErrorMessage(ConstraintValidatorContext context, String message) {
    context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
  }
}
