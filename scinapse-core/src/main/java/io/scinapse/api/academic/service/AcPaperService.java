package io.scinapse.api.academic.service;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import io.scinapse.api.academic.dto.AcPaperDto;
import io.scinapse.domain.configuration.AcademicJpaConfig;
import io.scinapse.domain.data.academic.repository.PaperRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@XRayEnabled
@Transactional(readOnly = true, transactionManager = AcademicJpaConfig.ACADEMIC_TX_MANAGER)
@Service
@RequiredArgsConstructor
public class AcPaperService {

    private final PaperRepository paperRepository;

    public List<AcPaperDto> findPapers(List<Long> paperIds, AcPaperDto.DetailSelector selector) {
        Map<Long, AcPaperDto> paperMap = findPapers(new HashSet<>(paperIds), selector);

        return paperIds
                .stream()
                .map(paperMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public Map<Long, AcPaperDto> findPapers(Set<Long> paperIds, AcPaperDto.DetailSelector selector) {
        return paperRepository.findByIdIn(paperIds)
                .stream()
                .map(paper -> new AcPaperDto(paper, selector))
                .collect(Collectors.toMap(
                        AcPaperDto::getId,
                        Function.identity()
                ));
    }

}
