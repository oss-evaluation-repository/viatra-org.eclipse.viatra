package system.queries;

import org.eclipse.incquery.runtime.api.impl.BaseGeneratedPatternGroup;
import org.eclipse.incquery.runtime.exception.IncQueryException;
import system.queries.DataTaskReadCorrespondenceMatcher;
import system.queries.DataTaskWriteCorrespondenceMatcher;
import system.queries.JobInfoCorrespondenceMatcher;
import system.queries.JobTaskCorrespondenceMatcher;
import system.queries.TasksAffectedThroughDataMatcher;
import system.queries.TransitiveAffectedTasksThroughDataMatcher;
import system.queries.UndefinedServiceTasksMatcher;

/**
 * A pattern group formed of all patterns defined in derivedFeatures.eiq.
 * 
 * <p>Use the static instance as any {@link org.eclipse.incquery.runtime.api.IPatternGroup}, to conveniently prepare
 * an EMF-IncQuery engine for matching all patterns originally defined in file derivedFeatures.eiq,
 * in order to achieve better performance than one-by-one on-demand matcher initialization.
 * 
 * <p> From package system.queries, the group contains the definition of the following patterns: <ul>
 * <li>TaskKind</li>
 * <li>JobTaskCorrespondence</li>
 * <li>TaskHasJob</li>
 * <li>DataTaskReadCorrespondence</li>
 * <li>DataTaskWriteCorrespondence</li>
 * <li>JobInfoCorrespondence</li>
 * <li>UndefinedServiceTasks</li>
 * <li>TasksAffectedThroughData</li>
 * <li>TransitiveAffectedTasksThroughData</li>
 * </ul>
 * 
 * @see IPatternGroup
 * 
 */
@SuppressWarnings("all")
public final class DerivedFeatures extends BaseGeneratedPatternGroup {
  /**
   * Access the pattern group.
   * 
   * @return the singleton instance of the group
   * @throws IncQueryException if there was an error loading the generated code of pattern specifications
   * 
   */
  public static DerivedFeatures instance() throws IncQueryException {
    if (INSTANCE == null) {
    	INSTANCE = new DerivedFeatures();
    }
    return INSTANCE;
    
  }
  
  private static DerivedFeatures INSTANCE;
  
  private DerivedFeatures() throws IncQueryException {
    querySpecifications.add(JobTaskCorrespondenceMatcher.querySpecification());
    querySpecifications.add(DataTaskReadCorrespondenceMatcher.querySpecification());
    querySpecifications.add(TasksAffectedThroughDataMatcher.querySpecification());
    querySpecifications.add(UndefinedServiceTasksMatcher.querySpecification());
    querySpecifications.add(JobInfoCorrespondenceMatcher.querySpecification());
    querySpecifications.add(TransitiveAffectedTasksThroughDataMatcher.querySpecification());
    querySpecifications.add(DataTaskWriteCorrespondenceMatcher.querySpecification());
    
  }
}
