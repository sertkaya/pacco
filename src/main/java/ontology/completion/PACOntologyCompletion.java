package ontology.completion;

import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;


public class PACOntologyCompletion {
	public static void main(String[] args) throws OWLOntologyCreationException {
		Set<OWLClassExpression> C = null;
		// IRI of the expert ontology
		IRI iri = IRI.create(args[1]);
		
		ExpertOracle e = new ExpertImpl(iri, C);
		
	}

}
