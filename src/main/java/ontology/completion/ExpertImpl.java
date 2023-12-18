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

/*
 * An expert implementation that completes queries w.r.t. an expert ontology.
 */
public class ExpertImpl implements ExpertOracle {
	private Set<OWLClassExpression> baseSet;
	private OWLDataFactory dataFactory;
	private OWLOntology ontology;
	private OWLReasoner reasoner;
	
	public ExpertImpl(IRI iri, Set<OWLClassExpression> C) {
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
		reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
		this.dataFactory = df;
		this.baseSet = C;
	}

	public Set<OWLClassExpression> complete(Set<OWLClassExpression> query) {
		OWLClassExpression queryConjunction = dataFactory.getOWLObjectIntersectionOf(query);
		
		if (queryConjunction.isBottomEntity())
			return(this.baseSet);

		Set<OWLClassExpression> completion = query;
		for (OWLClassExpression c : this.baseSet) {
			if (!query.contains(c)) {
				OWLSubClassOfAxiom ax = dataFactory.getOWLSubClassOfAxiom(queryConjunction, c);
				if (reasoner.isEntailed(ax))
					completion.add(c);
			}
		}
		
		return(completion);
	}
}
