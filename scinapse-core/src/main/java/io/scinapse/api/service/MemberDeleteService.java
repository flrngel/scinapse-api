package io.scinapse.api.service;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import io.scinapse.domain.data.scinapse.model.Member;
import io.scinapse.domain.data.scinapse.repository.CollectionRepository;
import io.scinapse.domain.data.scinapse.repository.CommentRepository;
import io.scinapse.domain.data.scinapse.repository.MemberRepository;
import io.scinapse.api.error.ResourceNotFoundException;
import io.scinapse.api.service.author.AuthorLayerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;

@XRayEnabled
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberDeleteService {

    private final MemberRepository memberRepository;

    private final AuthorLayerService layerService;
    private final CommentRepository commentRepository;
    private final CollectionRepository collectionRepository;

    @Transactional
    public void delete(long memberId) {
        Member member = Optional.ofNullable(memberRepository.findOne(memberId))
                .orElseThrow(() -> new ResourceNotFoundException("Member not found: " + memberId));

        Optional.of(member)
                .map(Member::getAuthorId)
                .ifPresent(layerService::disconnect);

        commentRepository.deleteByCreatedBy(member);
        collectionRepository.deleteByCreatedBy(member);

        memberRepository.delete(member);
    }

}
