package ontology.completion;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

public interface ExpertOracle {

	/*
	 * Does the implication hold in expert's view?
	 */
	public boolean holds(OWLSubClassOfAxiom ax);
	
	public OWLOntology getExpertOntology();
	
}
