import com.github.ixtf.japp.core.J;

import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * @author jzb 2019-10-02
 */
public class JgraphqlTest {
    public static void main(String[] args) {
        final Date date = new Date();
        final String format = DateTimeFormatter.ISO_OFFSET_TIME.format(J.localDateTime(date));
        System.out.println(format);
//        final TemporalAccessor parse = DateTimeFormatter.ISO_OFFSET_TIME.parse(String.valueOf(date.getTime()));
//        System.out.println(parse);

    }
}
