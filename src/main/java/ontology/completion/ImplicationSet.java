package ontology.completion;

import java.util.Set;
import java.util.LinkedHashSet;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;

import org.semanticweb.owlapi.model.OWLClassExpression;

public class ImplicationSet extends LinkedHashSet<Implication>{

	private static final long serialVersionUID = 1L;
	/**
	 * To keep track of in which premises an attribute occurs.
	 */
	private final Hashtable<OWLClassExpression, Set<Implication>> occursInPremises;
	
	public ImplicationSet(Set<OWLClassExpression> baseSet) {
		super();
		this.occursInPremises = new Hashtable<>();
		for (OWLClassExpression clsExp : baseSet) {
			this.occursInPremises.put(clsExp, new HashSet<>());
		}
	}
	
	@Override
	public boolean add(Implication imp) {
		// First check if an implication with the same premise and conclusion already exists
		for (Implication x : this) {
			if (x.getPremise().equals(imp.getPremise()) && x.getConclusion().equals(imp.getConclusion()))
				return(false);
		}
		Set<Implication> tmp;
		for (OWLClassExpression clsExpr : imp.getPremise()) {
			tmp = this.occursInPremises.get(clsExpr);
			tmp.add(imp);
			this.occursInPremises.put(clsExpr, tmp);
		}
		return (super.add(imp));
	}
	
	/**
	 * Computes the closure of a given attribute set under this implication set.
	 * Implementation the linear closure algorithm.
	 * 
	 * @param x
	 *            the attribute set to be closed
	 * @return the closure of <code>x</code> under this implication set
	 */
	public Set<OWLClassExpression> closure(Set<OWLClassExpression> x) {
		Set<OWLClassExpression> update = new HashSet<>(x);
		Set<OWLClassExpression> newDep = new LinkedHashSet<>(x);
		Hashtable<Implication, Integer> premiseSizes = new Hashtable<>();

		for (Implication imp : this) {
			premiseSizes.put(imp, imp.getPremise().size());
			if (imp.getPremise().isEmpty()) {
				newDep.addAll(imp.getConclusion());
				update.addAll(imp.getConclusion());
			}
		}
		OWLClassExpression attr;
		while (!update.isEmpty()) {
			Iterator<OWLClassExpression> it = update.iterator();
			attr = it.next();
			update.remove(attr);
			for (Implication imp : this.occursInPremises.get(attr)) {
				int tmp = premiseSizes.get(imp);
				premiseSizes.put(imp, --tmp);
				if (tmp == 0) {
					update.addAll(imp.getConclusion());
					update.removeAll(newDep);
					newDep.addAll(imp.getConclusion());
				}
			}
		}
		return newDep;
	}
	
	/*
	 * Return the implication with the given premise and conclusion. null if not found.
	 */
	public Implication getImplication(Set<OWLClassExpression> premise, Set<OWLClassExpression> conclusion) {
		for (Implication imp : this)
			if (imp.getPremise().equals(premise) && imp.getConclusion().equals(conclusion))
				return(imp);
		return(null);
	}
	
	/**
	 * Checks if a given attribute set is closed under this set of implications.
	 * 
	 * @param x
	 *            the attribute set to be checked
	 * @return <code>true</code> if <code>x</code> is closed, <code>false</code>
	 *         otherwise
	 */
	public boolean isClosed(Set<OWLClassExpression> x) {
		return x.equals(closure(x));
	}
	
}
