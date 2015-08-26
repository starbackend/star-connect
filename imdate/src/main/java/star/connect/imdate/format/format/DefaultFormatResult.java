package star.connect.imdate.format.format;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Optional;

/**
 * Created by pappmar on 14/08/2015.
 */
public class DefaultFormatResult implements FormatResult {

    public final List<Error> errors = Lists.newArrayList();

    @Override
    public void error(String message, Throwable e) {
        errors.add(new Error(message, e));

    }

    @Override
    public void error(long lineNumber, String field, String message, Throwable e) {
        errors.add(new Error(lineNumber, field, message, e));
    }

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public static class Error {

        final Long lineNumber;
        final String field;
        final String message;
        @JsonIgnore
        final Optional<Throwable> throwable;

        public String getType() {
            return throwable.map(t -> t.getClass().getName()).orElse(null);
        }

        public Error(Long lineNumber, String field, String message, Throwable throwable) {
            this.lineNumber = lineNumber;
            this.field = field;
            this.message = message;
            this.throwable = Optional.ofNullable(throwable);
        }

        public Error(String message, Throwable e) {
            this(null, null, message, e);
        }
    }
}
