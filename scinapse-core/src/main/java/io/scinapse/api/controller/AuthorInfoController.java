package io.scinapse.api.controller;

import io.scinapse.domain.data.scinapse.model.Member;
import io.scinapse.api.dto.author.AuthorAwardDto;
import io.scinapse.api.dto.author.AuthorEducationDto;
import io.scinapse.api.dto.author.AuthorExperienceDto;
import io.scinapse.api.dto.author.AuthorInfoDto;
import io.scinapse.api.dto.response.Response;
import io.scinapse.api.facade.AuthorLayerFacade;
import io.scinapse.api.facade.MemberFacade;
import io.scinapse.api.security.jwt.JwtUser;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class AuthorInfoController {

    private final AuthorLayerFacade layerFacade;
    private final MemberFacade memberFacade;

    @RequestMapping(value = "/authors/{authorId}/information", method = RequestMethod.GET)
    public Response<AuthorInfoDto> getInformation(@PathVariable long authorId) {
        return Response.success(layerFacade.getInformation(authorId));
    }

    @Transactional
    @RequestMapping(value = "/authors/{authorId}/educations", method = RequestMethod.POST)
    public Response<List<AuthorEducationDto>> addEducation(JwtUser user,
                                                           @PathVariable long authorId,
                                                           @RequestBody @Valid AuthorEducationDto educationDto) {
        Member member = memberFacade.loadMember(user);
        return Response.success(layerFacade.addEducation(member, authorId, educationDto));
    }

    @Transactional
    @RequestMapping(value = "/authors/educations/{educationId}", method = RequestMethod.PUT)
    public Response<List<AuthorEducationDto>> updateEducation(JwtUser user,
                                                              @PathVariable String educationId,
                                                              @RequestBody @Valid AuthorEducationDto educationDto) {
        Member member = memberFacade.loadMember(user);
        return Response.success(layerFacade.updateEducation(member, educationId, educationDto));
    }

    @Transactional
    @RequestMapping(value = "/authors/educations/{educationId}", method = RequestMethod.DELETE)
    public Response<List<AuthorEducationDto>> deleteEducation(JwtUser user, @PathVariable String educationId) {
        Member member = memberFacade.loadMember(user);
        return Response.success(layerFacade.deleteEducation(member, educationId));
    }

    @Transactional
    @RequestMapping(value = "/authors/{authorId}/experiences", method = RequestMethod.POST)
    public Response<List<AuthorExperienceDto>> addExperience(JwtUser user,
                                                             @PathVariable long authorId,
                                                             @RequestBody @Valid AuthorExperienceDto experienceDto) {
        Member member = memberFacade.loadMember(user);
        return Response.success(layerFacade.addExperience(member, authorId, experienceDto));
    }

    @Transactional
    @RequestMapping(value = "/authors/experiences/{experienceId}", method = RequestMethod.PUT)
    public Response<List<AuthorExperienceDto>> updateExperience(JwtUser user,
                                                                @PathVariable String experienceId,
                                                                @RequestBody @Valid AuthorExperienceDto experienceDto) {
        Member member = memberFacade.loadMember(user);
        return Response.success(layerFacade.updateExperience(member, experienceId, experienceDto));
    }

    @Transactional
    @RequestMapping(value = "/authors/experiences/{experienceId}", method = RequestMethod.DELETE)
    public Response<List<AuthorExperienceDto>> deleteExperience(JwtUser user, @PathVariable String experienceId) {
        Member member = memberFacade.loadMember(user);
        return Response.success(layerFacade.deleteExperience(member, experienceId));
    }

    @Transactional
    @RequestMapping(value = "/authors/{authorId}/awards", method = RequestMethod.POST)
    public Response<List<AuthorAwardDto>> addAward(JwtUser user,
                                                   @PathVariable long authorId,
                                                   @RequestBody @Valid AuthorAwardDto awardDto) {
        Member member = memberFacade.loadMember(user);
        return Response.success(layerFacade.addAward(member, authorId, awardDto));
    }

    @Transactional
    @RequestMapping(value = "/authors/awards/{awardId}", method = RequestMethod.PUT)
    public Response<List<AuthorAwardDto>> updateAward(JwtUser user,
                                                      @PathVariable String awardId,
                                                      @RequestBody @Valid AuthorAwardDto awardDto) {
        Member member = memberFacade.loadMember(user);
        return Response.success(layerFacade.updateAward(member, awardId, awardDto));
    }

    @Transactional
    @RequestMapping(value = "/authors/awards/{awardId}", method = RequestMethod.DELETE)
    public Response<List<AuthorAwardDto>> deleteAward(JwtUser user, @PathVariable String awardId) {
        Member member = memberFacade.loadMember(user);
        return Response.success(layerFacade.deleteAward(member, awardId));
    }

}
