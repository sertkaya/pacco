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
	private Set<OWLClassExpression> C;
	private OWLDataFactory df;
	private OWLOntology o;
	private OWLReasoner r;
	
	public ExpertImpl(IRI iri, Set<OWLClassExpression> C) {
		OWLOntologyManager om = OWLManager.createOWLOntologyManager();
		OWLDataFactory df = om.getOWLDataFactory();
		try {
			this.o = om.loadOntology(iri);
		}
		catch (OWLOntologyCreationException e) {
			System.err.print("Error loading ontology");
			System.exit(-1);
		}
		OWLReasonerFactory rf = new ReasonerFactory();
		this.r = rf.createReasoner(o);
		r.precomputeInferences(InferenceType.CLASS_HIERARCHY);
		this.df = df;
		this.C = C;
	}

	public Set<OWLClassExpression> complete(Set<OWLClassExpression> X) {
		OWLClassExpression xExpr = df.getOWLObjectIntersectionOf(X);
		
		if (xExpr.isBottomEntity())
			return(this.C);

		Set<OWLClassExpression> y = X;
		for (OWLClassExpression c : this.C) {
			if (!X.contains(c)) {
				OWLSubClassOfAxiom ax = df.getOWLSubClassOfAxiom(xExpr, c);
				if (r.isEntailed(ax))
					y.add(c);
			}
		}
		
		return(y);
	}
}
