package io.scinapse.api.controller;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.querydsl.core.annotations.QueryProjection;
import io.scinapse.api.dto.mag.AuthorDto;
import io.scinapse.api.dto.mag.PaperDto;
import io.scinapse.api.dto.profile.ProfileAwardDto;
import io.scinapse.api.dto.profile.ProfileDto;
import io.scinapse.api.dto.profile.ProfileEducationDto;
import io.scinapse.api.dto.profile.ProfileExperienceDto;
import io.scinapse.api.dto.response.Response;
import io.scinapse.api.facade.MemberFacade;
import io.scinapse.api.facade.ProfileFacade;
import io.scinapse.api.model.Member;
import io.scinapse.api.security.jwt.JwtUser;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileFacade profileFacade;
    private final MemberFacade memberFacade;

    @RequestMapping(value = "/profiles", method = RequestMethod.POST)
    public Response<ProfileDto> create(JwtUser user, @RequestBody @Valid ProfileDto dto) {
        Member member = memberFacade.loadMember(user);
        return Response.success(profileFacade.createProfile(member, dto));
    }

    @RequestMapping(value = "/profiles/me", method = RequestMethod.POST)
    public Response<ProfileDto> createMyProfile(JwtUser user, @RequestBody @Valid ProfileAuthorWrapper wrapper) {
        Member member = memberFacade.loadMember(user);
        return Response.success(profileFacade.createMyProfile(member, wrapper.getAuthorIds()));
    }

    @RequestMapping(value = "/profiles/{profileId}", method = RequestMethod.GET)
    public Response<ProfileDto> find(JwtUser user, @PathVariable String profileId) {
        Long memberId = user == null ? null : user.getId();
        return Response.success(profileFacade.findProfile(profileId, memberId));
    }

    @RequestMapping(value = "/profiles/me", method = RequestMethod.GET)
    public Response<ProfileDto> find(JwtUser user) {
        Member member = memberFacade.loadMember(user);
        return Response.success(profileFacade.findProfile(member));
    }

    @RequestMapping(value = "/profiles/{profileId}/connect/authors", method = RequestMethod.GET)
    public Response<List<AuthorDto>> getConnectedAuthors(@PathVariable String profileId) {
        return Response.success(profileFacade.getConnectedAuthors(profileId));
    }

    @RequestMapping(value = "/profiles/{profileId}/connect/authors", method = RequestMethod.POST)
    public Response<AuthorDto> connectAuthor(JwtUser user,
                                             @PathVariable String profileId,
                                             @RequestBody AuthorIdWrapper wrapper) {
        Member member = memberFacade.loadMember(user);
        return Response.success(profileFacade.connectAuthor(member, profileId, wrapper.getAuthorId()));
    }

    @RequestMapping(value = "/profiles/{profileId}/connect/authors/{authorId}", method = RequestMethod.DELETE)
    public Response disconnectAuthor(JwtUser user,
                                     @PathVariable String profileId,
                                     @PathVariable long authorId) {
        Member member = memberFacade.loadMember(user);
        profileFacade.disconnectAuthor(member, profileId, authorId);
        return Response.success();
    }

    @RequestMapping(value = "/profiles/{profileId}/papers", method = RequestMethod.GET)
    public Response<List<PaperDto>> getProfilePapers(@PathVariable String profileId, PageRequest pageRequest) {
        return Response.success(profileFacade.getProfilePapers(profileId, pageRequest));
    }

    @RequestMapping(value = "/profiles/{profileId}/papers/all", method = RequestMethod.GET)
    public Response<List<PaperTitleDto>> getAllProfilePapers(@PathVariable String profileId) {
        return Response.success(profileFacade.getAllProfilePapers(profileId));
    }

    @RequestMapping(value = "/profiles/{profileId}/educations", method = RequestMethod.POST)
    public Response<ProfileEducationDto> addEducation(JwtUser user,
                                                      @PathVariable String profileId,
                                                      @RequestBody @Valid ProfileEducationDto educationDto) {
        Member member = memberFacade.loadMember(user);
        return Response.success(profileFacade.addEducation(member, profileId, educationDto));
    }

    @RequestMapping(value = "/profiles/educations/{educationId}", method = RequestMethod.PUT)
    public Response<ProfileEducationDto> updateEducation(JwtUser user,
                                                         @PathVariable String educationId,
                                                         @RequestBody @Valid ProfileEducationDto educationDto) {
        Member member = memberFacade.loadMember(user);
        return Response.success(profileFacade.updateEducation(member, educationId, educationDto));
    }

    @RequestMapping(value = "/profiles/educations/{educationId}", method = RequestMethod.DELETE)
    public Response deleteEducation(JwtUser user, @PathVariable String educationId) {
        Member member = memberFacade.loadMember(user);
        profileFacade.deleteEducation(member, educationId);
        return Response.success();
    }

    @RequestMapping(value = "/profiles/{profileId}/experiences", method = RequestMethod.POST)
    public Response<ProfileExperienceDto> addExperience(JwtUser user,
                                                        @PathVariable String profileId,
                                                        @RequestBody @Valid ProfileExperienceDto experienceDto) {
        Member member = memberFacade.loadMember(user);
        return Response.success(profileFacade.addExperience(member, profileId, experienceDto));
    }

    @RequestMapping(value = "/profiles/experiences/{experienceId}", method = RequestMethod.PUT)
    public Response<ProfileExperienceDto> updateExperience(JwtUser user,
                                                           @PathVariable String experienceId,
                                                           @RequestBody @Valid ProfileExperienceDto experienceDto) {
        Member member = memberFacade.loadMember(user);
        return Response.success(profileFacade.updateExperience(member, experienceId, experienceDto));
    }

    @RequestMapping(value = "/profiles/experiences/{experienceId}", method = RequestMethod.DELETE)
    public Response deleteExperience(JwtUser user, @PathVariable String experienceId) {
        Member member = memberFacade.loadMember(user);
        profileFacade.deleteExperience(member, experienceId);
        return Response.success();
    }

    @RequestMapping(value = "/profiles/{profileId}/awards", method = RequestMethod.POST)
    public Response<ProfileAwardDto> addAward(JwtUser user,
                                              @PathVariable String profileId,
                                              @RequestBody @Valid ProfileAwardDto awardDto) {
        Member member = memberFacade.loadMember(user);
        return Response.success(profileFacade.addAward(member, profileId, awardDto));
    }

    @RequestMapping(value = "/profiles/awards/{awardId}", method = RequestMethod.PUT)
    public Response<ProfileAwardDto> updateAward(JwtUser user,
                                                 @PathVariable String awardId,
                                                 @RequestBody @Valid ProfileAwardDto awardDto) {
        Member member = memberFacade.loadMember(user);
        return Response.success(profileFacade.updateAward(member, awardId, awardDto));
    }

    @RequestMapping(value = "/profiles/awards/{awardId}", method = RequestMethod.DELETE)
    public Response deleteAward(JwtUser user, @PathVariable String awardId) {
        Member member = memberFacade.loadMember(user);
        profileFacade.deleteAward(member, awardId);
        return Response.success();
    }

    @RequestMapping(value = "/profiles/{profileId}/selected-publications", method = RequestMethod.PUT)
    public Response<List<PaperDto>> updateSelectedPublications(JwtUser user,
                                                               @PathVariable String profileId,
                                                               @RequestBody SelectedPublicationWrapper wrapper) {
        Member member = memberFacade.loadMember(user);
        return Response.success(profileFacade.updateSelectedPublications(member, profileId, wrapper.getPaperIds()));
    }

    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    @Getter
    @Setter
    private static class SelectedPublicationWrapper {
        private List<Long> paperIds;
    }

    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    @Getter
    @Setter
    private static class ProfileAuthorWrapper {
        @Size(min = 1, max = 20)
        @NotNull
        private List<Long> authorIds;
    }

    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    @Getter
    @Setter
    private static class AuthorIdWrapper {
        private long authorId;
    }

    @Getter
    @Setter
    public static class PaperTitleDto {
        private long id;
        private String title;

        @QueryProjection
        public PaperTitleDto(long id, String title) {
            this.id = id;
            this.title = title;
        }
    }

}
