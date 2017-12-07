package network.pluto.absolute.service;

import network.pluto.bibliotheca.models.Paper;
import network.pluto.bibliotheca.models.PaperReference;
import network.pluto.bibliotheca.repositories.PaperReferenceRepository;
import network.pluto.bibliotheca.repositories.PaperRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
@Service
public class PaperService {

    private final PaperRepository paperRepository;
    private final PaperReferenceRepository paperReferenceRepository;

    @Autowired
    public PaperService(PaperRepository paperRepository, PaperReferenceRepository paperReferenceRepository) {
        this.paperRepository = paperRepository;
        this.paperReferenceRepository = paperReferenceRepository;
    }

    public Paper find(long paperId) {
        return paperRepository.findOne(paperId);
    }

    public List<Paper> findByIdIn(List<Long> paperIds) {
        return paperRepository.findByIdIn(paperIds);
    }

    public long countReference(long paperId) {
        return paperReferenceRepository.countByPaperId(paperId);
    }

    public long countCited(long paperId) {
        return paperReferenceRepository.countByReferenceId(paperId);
    }

    public Page<Long> findReferences(long paperId, Pageable pageable) {
        return paperReferenceRepository.findByPaperId(paperId, pageable).map(PaperReference::getReferenceId);
    }

    public Page<Long> findCited(long paperId, Pageable pageable) {
        return paperReferenceRepository.findByReferenceId(paperId, pageable).map(PaperReference::getPaperId);
    }
}
