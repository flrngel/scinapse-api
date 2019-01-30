package io.scinapse.api.academic.service;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import io.scinapse.api.academic.dto.AcAuthorDto;
import io.scinapse.api.configuration.AcademicJpaConfig;
import io.scinapse.api.data.academic.repository.AuthorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@XRayEnabled
@Transactional(readOnly = true, transactionManager = AcademicJpaConfig.ACADEMIC_TX_MANAGER)
@Service
@RequiredArgsConstructor
public class AcAuthorService {

    private final AuthorRepository authorRepository;

    public List<AcAuthorDto> findAuthors(List<Long> authorIds, boolean loadFos) {
        Map<Long, AcAuthorDto> authorMap = authorRepository.findByIdIn(authorIds)
                .stream()
                .map(author -> new AcAuthorDto(author, loadFos))
                .collect(Collectors.toMap(
                        AcAuthorDto::getId,
                        Function.identity()
                ));

        return authorIds
                .stream()
                .map(authorMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

}
