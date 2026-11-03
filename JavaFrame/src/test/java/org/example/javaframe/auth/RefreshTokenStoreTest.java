package org.example.javaframe.auth;

import org.example.javaframe.common.BizException;
import org.example.javaframe.common.ErrorCode;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RefreshTokenStoreTest {

    private static final Instant START = Instant.parse("2026-01-01T00:00:00Z");

    /** 测试用时钟，可以手动往前走。 */
    private static final class MutableClock extends Clock {
        private Instant now;
        private final ZoneId zone = ZoneOffset.UTC;

        MutableClock(Instant start) {
            this.now = start;
        }

        void advance(Duration duration) {
            this.now = this.now.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return zone;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }

    private final MutableClock clock = new MutableClock(START);
    private final RefreshTokenStore store = new RefreshTokenStore(7, clock);

    @Test
    void issuedTokenCanBeRotatedOnce() {
        String token = store.issue("admin");

        assertThat(store.rotate(token)).isEqualTo("admin");
    }

    @Test
    void sameTokenCannotBeUsedTwice() {
        String token = store.issue("admin");
        store.rotate(token);

        assertThatThrownBy(() -> store.rotate(token))
                .isInstanceOf(BizException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.REFRESH_INVALID);
    }

    @Test
    void unknownTokenIsRejected() {
        assertThatThrownBy(() -> store.rotate("not-a-real-token"))
                .isInstanceOf(BizException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.REFRESH_INVALID);
    }

    @Test
    void eachIssueReturnsDifferentToken() {
        assertThat(store.issue("admin")).isNotEqualTo(store.issue("admin"));
    }

    @Test
    void expiredTokenIsRejected() {
        String token = store.issue("admin");
        clock.advance(Duration.ofDays(8));

        assertThatThrownBy(() -> store.rotate(token))
                .isInstanceOf(BizException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.REFRESH_INVALID);
    }

    @Test
    void tokenStillValidJustBeforeExpiry() {
        String token = store.issue("admin");
        clock.advance(Duration.ofDays(6));

        assertThat(store.rotate(token)).isEqualTo("admin");
    }
}
