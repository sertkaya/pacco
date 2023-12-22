package ontology.completion;

import java.util.Set;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

public interface ExpertOracle {

	/*
	 * Does the implication hold in expert's view?
	 */
	public boolean holds(OWLSubClassOfAxiom ax);
	// public boolean holds(Set<OWLClassExpression> p, Set<OWLClassExpression> c);
	
	public OWLOntology getExpertOntology();
	
}
