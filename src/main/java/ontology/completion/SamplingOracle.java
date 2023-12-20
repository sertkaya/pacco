package ontology.completion;

import java.util.Collections;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClassExpression;

public class SamplingOracle {
	private Set<OWLClassExpression> baseSet;
	
	public SamplingOracle(Set<OWLClassExpression> baseSet) {
		this.baseSet = baseSet;
	}
	
	public Set<OWLClassExpression> sample() {
		// TODO: sampling
		// Q = U()
		
		Set<OWLClassExpression> s = Collections.<OWLClassExpression>emptySet();
		return(s);
	}
}
