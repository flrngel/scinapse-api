package network.pluto.absolute.service;

import network.pluto.bibliotheca.models.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class TransactionService {

    @Value("${pluto.server.alfred.url}")
    private String alfredUrl;

    private final RestTemplate restTemplate;

    @Autowired
    public TransactionService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void createWallet(Member member) {
        // TODO handle exception
        restTemplate.postForObject(alfredUrl + "/wallets/" + member.getId(), null, String.class);
    }
}
