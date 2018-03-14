package network.pluto.absolute.service.mag;

import lombok.RequiredArgsConstructor;
import network.pluto.bibliotheca.models.mag.Paper;
import network.pluto.bibliotheca.models.mag.PaperAbstract;
import network.pluto.bibliotheca.models.mag.RelPaperReference;
import network.pluto.bibliotheca.repositories.mag.PaperAbstractRepository;
import network.pluto.bibliotheca.repositories.mag.PaperRepository;
import network.pluto.bibliotheca.repositories.mag.RelPaperReferenceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class PaperService {

    private final PaperRepository paperRepository;
    private final PaperAbstractRepository paperAbstractRepository;
    private final RelPaperReferenceRepository paperReferenceRepository;

    public Paper find(long paperId) {
        Paper paper = paperRepository.findOne(paperId);
        if (paper == null) {
            return null;
        }

        PaperAbstract paperAbstract = paperAbstractRepository.findOne(paperId);
        paper.setPaperAbstract(paperAbstract);
        return paper;
    }

    public List<Paper> findByIdIn(List<Long> paperIds) {
        List<Paper> papers = paperRepository.findByIdIn(paperIds);

        Map<Long, PaperAbstract> abstractMap = paperAbstractRepository.findByPaperIdIn(paperIds)
                .stream()
                .collect(Collectors.toMap(
                        PaperAbstract::getPaperId,
                        Function.identity()
                ));
        papers.forEach(p -> p.setPaperAbstract(abstractMap.get(p.getId())));

        return papers;
    }

    public Page<Long> findReferences(long paperId, Pageable pageable) {
        return paperReferenceRepository.findByPaperId(paperId, pageable).map(RelPaperReference::getPaperReferenceId);
    }

    public Page<Long> findCited(long paperId, Pageable pageable) {
        return paperReferenceRepository.findByPaperReferenceId(paperId, pageable).map(RelPaperReference::getPaperId);
    }

}
