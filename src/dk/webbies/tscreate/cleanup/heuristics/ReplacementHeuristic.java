package dk.webbies.tscreate.cleanup.heuristics;

import com.google.common.collect.Multimap;
import dk.webbies.tscreate.analysis.declarations.types.DeclarationType;
import dk.webbies.tscreate.cleanup.CollectEveryTypeVisitor;

/**
 * Created by erik1 on 11-03-2016.
 */
public interface ReplacementHeuristic {
    Multimap<DeclarationType, DeclarationType> findReplacements(CollectEveryTypeVisitor collected);

    String getDescription();
}
