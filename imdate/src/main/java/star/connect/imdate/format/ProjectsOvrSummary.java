package star.connect.imdate.format;

import hu.mapro.mfw.model.Field;
import hu.mapro.mfw.model.Model;

import java.util.Calendar;

/**
 * Created by pappmar on 11/08/2015.
 */
@Model
public abstract class ProjectsOvrSummary {

    public interface F<V> extends Field<ProjectsOvrSummary, V> {}

    public ProjectsOvrSummary() {
    }

    public ProjectsOvrSummary(long count, Calendar lastUpdate) {
        this.count().set(count);
        this.lastUpdate().set(lastUpdate);
    }

    abstract public
    F<Long> count();

    abstract public
    F<Calendar>  lastUpdate();

}
