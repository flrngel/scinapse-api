package io.scinapse.api.controller;

import io.scinapse.api.data.scinapse.model.Member;
import io.scinapse.api.dto.author.AuthorAwardDto;
import io.scinapse.api.dto.author.AuthorEducationDto;
import io.scinapse.api.dto.author.AuthorExperienceDto;
import io.scinapse.api.dto.author.AuthorInfoDto;
import io.scinapse.api.dto.response.Response;
import io.scinapse.api.facade.AuthorLayerFacade;
import io.scinapse.api.facade.MemberFacade;
import io.scinapse.api.security.jwt.JwtUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
public class AuthorInfoController {

    private final AuthorLayerFacade layerFacade;
    private final MemberFacade memberFacade;

    @RequestMapping(value = "/authors/{authorId}/information", method = RequestMethod.GET)
    public Response<AuthorInfoDto> getInformation(@PathVariable long authorId) {
        return Response.success(layerFacade.getInformation(authorId));
    }

    @RequestMapping(value = "/authors/{authorId}/educations", method = RequestMethod.POST)
    public Response<AuthorEducationDto> addEducation(JwtUser user,
                                                     @PathVariable long authorId,
                                                     @RequestBody @Valid AuthorEducationDto educationDto) {
        Member member = memberFacade.loadMember(user);
        return Response.success(layerFacade.addEducation(member, authorId, educationDto));
    }

    @RequestMapping(value = "/authors/educations/{educationId}", method = RequestMethod.PUT)
    public Response<AuthorEducationDto> updateEducation(JwtUser user,
                                                        @PathVariable String educationId,
                                                        @RequestBody @Valid AuthorEducationDto educationDto) {
        Member member = memberFacade.loadMember(user);
        return Response.success(layerFacade.updateEducation(member, educationId, educationDto));
    }

    @RequestMapping(value = "/authors/educations/{educationId}", method = RequestMethod.DELETE)
    public Response deleteEducation(JwtUser user, @PathVariable String educationId) {
        Member member = memberFacade.loadMember(user);
        layerFacade.deleteEducation(member, educationId);
        return Response.success();
    }

    @RequestMapping(value = "/authors/{authorId}/experiences", method = RequestMethod.POST)
    public Response<AuthorExperienceDto> addExperience(JwtUser user,
                                                       @PathVariable long authorId,
                                                       @RequestBody @Valid AuthorExperienceDto experienceDto) {
        Member member = memberFacade.loadMember(user);
        return Response.success(layerFacade.addExperience(member, authorId, experienceDto));
    }

    @RequestMapping(value = "/authors/experiences/{experienceId}", method = RequestMethod.PUT)
    public Response<AuthorExperienceDto> updateExperience(JwtUser user,
                                                          @PathVariable String experienceId,
                                                          @RequestBody @Valid AuthorExperienceDto experienceDto) {
        Member member = memberFacade.loadMember(user);
        return Response.success(layerFacade.updateExperience(member, experienceId, experienceDto));
    }

    @RequestMapping(value = "/authors/experiences/{experienceId}", method = RequestMethod.DELETE)
    public Response deleteExperience(JwtUser user, @PathVariable String experienceId) {
        Member member = memberFacade.loadMember(user);
        layerFacade.deleteExperience(member, experienceId);
        return Response.success();
    }

    @RequestMapping(value = "/authors/{authorId}/awards", method = RequestMethod.POST)
    public Response<AuthorAwardDto> addAward(JwtUser user,
                                             @PathVariable long authorId,
                                             @RequestBody @Valid AuthorAwardDto awardDto) {
        Member member = memberFacade.loadMember(user);
        return Response.success(layerFacade.addAward(member, authorId, awardDto));
    }

    @RequestMapping(value = "/authors/awards/{awardId}", method = RequestMethod.PUT)
    public Response<AuthorAwardDto> updateAward(JwtUser user,
                                                @PathVariable String awardId,
                                                @RequestBody @Valid AuthorAwardDto awardDto) {
        Member member = memberFacade.loadMember(user);
        return Response.success(layerFacade.updateAward(member, awardId, awardDto));
    }

    @RequestMapping(value = "/authors/awards/{awardId}", method = RequestMethod.DELETE)
    public Response deleteAward(JwtUser user, @PathVariable String awardId) {
        Member member = memberFacade.loadMember(user);
        layerFacade.deleteAward(member, awardId);
        return Response.success();
    }

}
