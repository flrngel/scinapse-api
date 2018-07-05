package io.scinapse.api.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
public class QueryTest {

    @Test
    public void valid_doi() throws Exception {
        String url1 = "HttP://doi.org/10.1038/nrd2614";
        assertThat(Query.parse(url1).getDoi()).isEqualTo("10.1038/nrd2614");

        String url2 = "doi.org/10.1038/nrd2614";
        assertThat(Query.parse(url2).getDoi()).isEqualTo("10.1038/nrd2614");

        String url3 = "10.1038/nrd2614";
        assertThat(Query.parse(url3).getDoi()).isEqualTo("10.1038/nrd2614");

        String url4 = "10.1002/(SICI)1521-4141(199811)28:11<3514::AID-IMMU3514>3.0.CO;2-T";
        assertThat(Query.parse(url4).getDoi()).isEqualTo("10.1002/(SICI)1521-4141(199811)28:11<3514::AID-IMMU3514>3.0.CO;2-T");

        String url5 = "10.1021/bi00403a004";
        assertThat(Query.parse(url5).getDoi()).isEqualTo("10.1021/bi00403a004");

        String url6 = "10.1207/s15327906mbr0801_4";
        assertThat(Query.parse(url6).getDoi()).isEqualTo("10.1207/s15327906mbr0801_4");

        String url7 = "   DOI   :    10.1016/j.psyneuen.2017.10.017";
        assertThat(Query.parse(url7).getDoi()).isEqualTo("10.1016/j.psyneuen.2017.10.017");

        String url8 = "doi:10.1007/s12272-013-0020-y";
        assertThat(Query.parse(url8).getDoi()).isEqualTo("10.1007/s12272-013-0020-y");
    }

    @Test
    public void invalid_doi() throws Exception {
        String url1 = "https://pluto.network/10.1038/nrd2614";
        assertThat(Query.parse(url1).getDoi()).isNull();

        String url2 = "/10.1038/nrd2614";
        assertThat(Query.parse(url2).getDoi()).isNull();

        String url3 = "10.13039/100005243@@@10.13039/501100003767@@@10.13039/501100003339@@@10.13039/100009042@@@10.13039/501100006393";
        assertThat(Query.parse(url3).getDoi()).isNull();
    }

}