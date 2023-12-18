package ontology.completion;

import java.util.Set;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

public class Implication {

	private Set<OWLClassExpression> premise, conclusion;
	private OWLDataFactory df;
	
	public Implication(Set<OWLClassExpression> lhs, Set<OWLClassExpression> rhs, OWLDataFactory df) {
		this.premise = lhs;
		this.conclusion = rhs;
		this.df = df;
	}

	public OWLSubClassOfAxiom toGCI() {
		OWLClassExpression premiseExpr = df.getOWLObjectIntersectionOf(premise);
		OWLClassExpression conclusionExpr = df.getOWLObjectIntersectionOf(conclusion);
		OWLSubClassOfAxiom gci = df.getOWLSubClassOfAxiom(premiseExpr, conclusionExpr);
		return(gci);
	}

	public Set<OWLClassExpression> getPremise() {
		return(this.premise);
	}

	public Set<OWLClassExpression> getConclusion() {
		return(this.conclusion);
	}
}
