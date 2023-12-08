package ontology.completion;

import java.util.Set;
import org.semanticweb.owlapi.model.OWLClassExpression;

public interface ExpertOracle {

	/*
	 * Completes a given set of concept expression w.r.t. expert knowledge
	 */
	public Set<OWLClassExpression> complete(Set<OWLClassExpression> X);
}
