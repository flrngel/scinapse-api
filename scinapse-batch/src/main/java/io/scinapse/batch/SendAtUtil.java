package io.scinapse.batch;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.OffsetTime;

public class SendAtUtil {

    private static LocalDate today;
    private static LocalDate tomorrow;
    private static OffsetTime sendingTime;

    public static void init() {
        today = LocalDate.now();
        tomorrow = today.plusDays(1);
        sendingTime = OffsetTime.now().plusHours(1);
    }

    public static OffsetDateTime getSendAt(OffsetDateTime targetDateTime) {
        return getSendAt(targetDateTime.toOffsetTime());
    }

    public static OffsetDateTime getSendAt(OffsetTime targetTime) {
        if (sendingTime.isBefore(targetTime)) {
            // send at today
            return targetTime.atDate(today);
        } else {
            // send at tomorrow
            return targetTime.atDate(tomorrow);
        }
    }
}
