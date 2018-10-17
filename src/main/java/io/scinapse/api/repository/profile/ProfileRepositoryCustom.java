package io.scinapse.api.repository.profile;

import io.scinapse.api.controller.ProfileController;

import java.util.List;

public interface ProfileRepositoryCustom {
    List<ProfileController.PaperTitleDto> getAllProfilePapers(String profileId);
}
