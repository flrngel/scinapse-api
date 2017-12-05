package network.pluto.absolute.service;

import network.pluto.bibliotheca.academic.Paper;
import network.pluto.bibliotheca.academic.PaperRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PaperService {

    private final PaperRepository paperRepository;

    @Autowired
    public PaperService(PaperRepository paperRepository) {
        this.paperRepository = paperRepository;
    }

    public Paper find(long paperId) {
        return paperRepository.findOne(paperId);
    }

    public List<Paper> findByIdIn(List<Long> paperIds) {
        return paperRepository.findByIdIn(paperIds);
    }
}
