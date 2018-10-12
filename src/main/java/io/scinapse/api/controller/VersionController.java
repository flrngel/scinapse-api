package io.scinapse.api.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class VersionController {
    @Value("${git.commit.message.short}")
    private String commitMessage;

    @Value("${git.branch}")
    private String branch;

    @Value("${git.commit.id}")
    private String commitId;

    @RequestMapping("/")
    public Map<String, String> getCommitId() {
        Map<String, String> result = new HashMap<>();
        result.put("Commit message", commitMessage);
        result.put("Commit branch", branch);
        result.put("Commit id", commitId);
        return result;
    }
}
