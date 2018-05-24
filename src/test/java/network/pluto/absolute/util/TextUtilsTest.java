package network.pluto.absolute.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
public class TextUtilsTest {

    @Test
    public void parsePhrase() {
        String query = "\"this\" \"should be matched\" \"but\"this\" should\"not\"be\"matched\"";
        List<String> queries = TextUtils.parsePhrase(query);
        assertThat(queries).hasSize(3);
        assertThat(queries.get(0)).isEqualTo("this");
        assertThat(queries.get(1)).isEqualTo("should be matched");
        assertThat(queries.get(2)).isEqualTo("but\"this");
    }

}