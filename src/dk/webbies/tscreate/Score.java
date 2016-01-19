package dk.webbies.tscreate;

/**
 * Created by erik1 on 19-01-2016.
 */
public class Score {
    public final double fMeasure;
    public final double precision;
    public final double recall;

    public Score(double fMeasure, double precision, double recall) {
        this.fMeasure = fMeasure;
        this.precision = precision;
        this.recall = recall;
    }
}
