package ontology.completion;

import java.util.Set;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

/**
 * An expert implementation that answers questions w.r.t. an expert ontology.
 */
public class ReasonerExpert implements ExpertOracle {
	private OWLOntology ontology;
	private OWLReasoner reasoner;
	
	public ReasonerExpert(IRI iri) {
		OWLOntologyManager om = OWLManager.createOWLOntologyManager();
		OWLDataFactory df = om.getOWLDataFactory();
		try {
			this.ontology = om.loadOntology(iri);
		}
		catch (OWLOntologyCreationException e) {
			System.err.print("Error loading ontology");
			System.exit(-1);
		}
		OWLReasonerFactory rf = new ReasonerFactory();
		this.reasoner = rf.createReasoner(ontology);
		this.reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
	}

	public boolean holds(Implication imp) {
		OWLSubClassOfAxiom ax = imp.toGCI();
		return(this.reasoner.isEntailed(ax));
	}

	public boolean holds(OWLSubClassOfAxiom ax) {
		return(this.reasoner.isEntailed(ax));
	}

	public OWLOntology getExpertOntology() {
		return(this.ontology);
	}
	
	public OWLReasoner getReasoner() {
		return(this.reasoner);
	}
}
