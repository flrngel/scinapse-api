package network.pluto.absolute.controller;

import network.pluto.bibliotheca.models.Member;
import network.pluto.bibliotheca.models.Wallet;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class ArticleController {

    @RequestMapping(value = "articles/{articleId}", method = RequestMethod.GET)
    public Object getArticle(@PathVariable long articleId) {
        Map<String, Object> articleMap = new HashMap<>();
        articleMap.put("articleId", articleId);
        articleMap.put("type", "OPEN_ACCESS_PAPER");
        articleMap.put("title", "Blockchain technology for improving clinical research quality");
        articleMap.put("link", "https://app.zeplin.io/project/59ace356074c57e00eef4431/screen/59ae3dd8a1290cdf548eea14");
        articleMap.put("source", "Arxiv");

        Member member1 = new Member();
        member1.setMemberId(11);
        member1.setEmail("test@pluto.network");
        member1.setFullName("Jeffrey C. Lagarias");
        Wallet wallet = new Wallet();
        wallet.setWalletId(33L);
        wallet.setAddress("0x822408EAC8C331002BE00070AFDD2A5A02065D3F");
        member1.setWallet(wallet);
        articleMap.put("createdBy", member1);
        articleMap.put("createdAt", LocalDateTime.now());

        List<Object> authors = new ArrayList<>();

        Map<String, Object> authorMap = new HashMap<>();
        authorMap.put("type", "LEAD_AUTHOR");
        authorMap.put("name", "Antonia Ruprecht");
        authorMap.put("organization", "University of Michigan");
        Member member2 = new Member();
        member2.setMemberId(22);
        member2.setEmail("test@postech.ac.kr");
        member2.setFullName("Antonia Ruprecht");
        authorMap.put("member", member2);
        authors.add(authorMap);

        Map<String, Object> authorMap2 = new HashMap<>();
        authorMap2.put("type", "CORRESPONDING_AUTHOR");
        authorMap2.put("name", "Nsia Chelsea");
        authorMap2.put("organization", "University of Michigan");
        authors.add(authorMap2);

        Map<String, Object> authorMap3 = new HashMap<>();
        authorMap3.put("type", "CO_AUTHOR");
        authorMap3.put("name", "Xander Aldhard");
        authorMap3.put("organization", "University of Michigan");
        authors.add(authorMap2);

        articleMap.put("authors", authors);

        articleMap.put("abstract", "Reproducibility, data sharing, personal data privacy concerns and patient enrolment in clinical trials are huge medical challenges for contemporary clinical research. A new technology, Blockchain, may be a key to addressing these challenges and should draw the attention of the whole clinical research community.\n" +
                "\n" +
                "Blockchain brings the Internet to its definitive decentralisation goal. The core principle of Blockchain is that any service relying on trusted third parties can be built in a transparent, decentralised, secure \"trustless\" manner at the top of the Blockchain (in fact, there is trust, but it is hardcoded in the Blockchain protocol via a complex cryptographic algorithm). Therefore, users have a high degree of control over and autonomy and trust of the data and its integrity. Blockchain allows for reaching a substantial level of historicity and inviolability of data for the whole document flow in a clinical trial. Hence, it ensures traceability, prevents a posteriori reconstruction and allows for securely automating the clinical trial through what are called Smart Contracts. At the same time, the technology ensures fine-grained control of the data, its security and its shareable parameters, for a single patient or group of patients or clinical trial stakeholders.\n" +
                "\n" +
                "In this commentary article, we explore the core functionalities of Blockchain applied to clinical trials and we illustrate concretely its general principle in the context of consent to a trial protocol. Trying to figure out the potential impact of Blockchain implementations in the setting of clinical trials will shed new light on how modern clinical trial methods could evolve and benefit from Blockchain technologies in order to tackle the aforementioned challenges.");

        Map<String, Object> pointMap = new HashMap<>();
        pointMap.put("total", 7.4);
        pointMap.put("originality", 8.1);
        pointMap.put("contribution", 5.9);
        pointMap.put("analysis", 9.2);
        pointMap.put("expressiveness", 6.7);
        articleMap.put("point", pointMap);

        List<Object> evaluations = new ArrayList<>();
        Map<String, Object> evaluation = new HashMap<>();
        evaluation.put("evaluationId", 1);
        evaluation.put("createdBy", member1);
        evaluation.put("createdAt", LocalDateTime.now());
        evaluation.put("like", 9);

        Map<String, Object> pointMap2 = new HashMap<>();
        pointMap2.put("total", 7.5);
        pointMap2.put("originality", 6);
        pointMap2.put("contribution", 8);
        pointMap2.put("analysis", 7);
        pointMap2.put("expressiveness", 9);
        pointMap2.put("originalityComment", "Please specify as the mechanism of loss of function of the mutation in patients with mutations at position p.335-339 is reduced protein stability due to rapid protein degradation at the proteasome, rather than reduced catalysis.");
        pointMap2.put("contributionComment", "I would begin the words aldosterone, amlodipine, metoprolol with lower case letters when in the midst of a sentence.");
        pointMap2.put("analysisComment", "I think you should correct the following sentence - p.R337 loci of 11HSDB2 to- “The p.R337 residue of 11-β-dehydrogenase isozyme 2 enzyme is a recognised mutation site. The mutation p.R337C has been previously identified in a family from Iran with three affected children.”");
        pointMap2.put("expressivenessComment", "Syndrome of apparent mineralocorticoid excess (MIM#218030) is a rare cause of juvenile hypertension occuring due to homozygous or compound heterozygous mutations in HSD11B2 gene (MIM*614232).");
        evaluation.put("point", pointMap2);

        List<Object> comments = new ArrayList<>();
        Map<String, Object> commentMap = new HashMap<>();
        commentMap.put("commentId", 1);
        commentMap.put("createdBy", member2);
        commentMap.put("createdAt", LocalDateTime.now());
        commentMap.put("comment", "Trying to figure out the potential impact of Blockchain implementations in the setting of clinical trials will shed new light on how modern clinical trial methods could evolve and benefit from Blockchain technologies in order to tackle the aforementioned challenges.");
        comments.add(commentMap);
        comments.add(commentMap);
        comments.add(commentMap);
        evaluation.put("comments", comments);

        evaluations.add(evaluation);
        evaluations.add(evaluation);
        evaluations.add(evaluation);
        evaluations.add(evaluation);
        evaluations.add(evaluation);

        articleMap.put("evaluations", evaluations);

        return articleMap;
    }
}
