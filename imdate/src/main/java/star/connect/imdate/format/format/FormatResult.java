package star.connect.imdate.format.format;

/**
 * Created by pappmar on 12/08/2015.
 */
public interface FormatResult {

    void error(String message, Throwable e);
    void error(long lineNumber, String field, String message, Throwable e);

}
