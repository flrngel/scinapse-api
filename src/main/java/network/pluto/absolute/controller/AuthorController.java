package network.pluto.absolute.controller;

import lombok.RequiredArgsConstructor;
import network.pluto.absolute.dto.PaperDto;
import network.pluto.absolute.service.mag.AuthorService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class AuthorController {

    private final AuthorService authorService;

    @RequestMapping(value = "/authors/{authorId}/papers/main", method = RequestMethod.GET)
    public HashMap<String, Object> getMainPapers(@PathVariable long authorId) {
        List<PaperDto> mainPapers = authorService.getMainPapers(authorId)
                .stream()
                .map(PaperDto::simple)
                .collect(Collectors.toList());

        HashMap<String, Object> result = new HashMap<>();
        Meta meta = mainPapers.isEmpty() ? Meta.unavailable() : Meta.available();
        result.put("meta", meta);
        result.put("data", mainPapers);

        return result;
    }

}
