package network.pluto.absolute.service.mag;

import lombok.RequiredArgsConstructor;
import network.pluto.bibliotheca.models.mag.Paper;
import network.pluto.bibliotheca.models.mag.RelPaperReference;
import network.pluto.bibliotheca.repositories.mag.PaperRepository;
import network.pluto.bibliotheca.repositories.mag.RelPaperReferenceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class PaperService {

    private final PaperRepository paperRepository;
    private final RelPaperReferenceRepository paperReferenceRepository;

    public Paper find(long paperId) {
        return paperRepository.findOne(paperId);
    }

    public List<Paper> findByIdIn(List<Long> paperIds) {
        return paperRepository.findByIdIn(paperIds);
    }

    public Page<Long> findReferences(long paperId, Pageable pageable) {
        return paperReferenceRepository.findByPaperId(paperId, pageable).map(RelPaperReference::getPaperReferenceId);
    }

    public Page<Long> findCited(long paperId, Pageable pageable) {
        return paperReferenceRepository.findByPaperReferenceId(paperId, pageable).map(RelPaperReference::getPaperId);
    }

}
