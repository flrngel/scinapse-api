package network.pluto.absolute.validator;

import network.pluto.absolute.dto.MemberDto;
import network.pluto.absolute.model.Member;
import network.pluto.absolute.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class MemberDuplicationValidator implements Validator {

    private final MemberService memberService;

    @Autowired
    public MemberDuplicationValidator(MemberService memberService) {
        this.memberService = memberService;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return MemberDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        MemberDto dto = (MemberDto) target;
        Member member = memberService.findByEmail(dto.getEmail());

        if (member != null) {
            errors.rejectValue("email", "EXIST", "email already exists");
        }
    }

}
