package ontology.completion;

import java.util.Collections;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClassExpression;

public class SamplingOracle {
	private ExpertImpl expert;
	private Set<OWLClassExpression> baseSet;
	
	public SamplingOracle(ExpertImpl e, Set<OWLClassExpression> baseSet) {
		this.expert = e;
		this.baseSet = baseSet;
	}
	
	public Set<OWLClassExpression> getCounterExample() {
		// TODO: sampling
		// Q = U()
		
		Set<OWLClassExpression> x = Collections.<OWLClassExpression>emptySet();
		// TODO: closure X of C_Q
		// X = L(C_Q)
		CompletionQuery q = new CompletionQuery(this.baseSet, x, null);
		Set<OWLClassExpression> expert_c = this.expert.complete(null);
		// if (x.equals(x))
		return(x);
	}
}
