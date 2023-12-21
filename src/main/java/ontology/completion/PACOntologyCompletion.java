package ontology.completion;

import java.util.Collections;
import java.util.Set;
import java.util.HashSet;
import java.util.Hashtable;

import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;


public class PACOntologyCompletion {
	
	private static OWLOntologyManager om = OWLManager.createOWLOntologyManager();
	private static OWLDataFactory df = om.getOWLDataFactory();
	private static OWLReasonerFactory rf = new ReasonerFactory();
	private static OWLReasoner r;


	public static void main(String[] args) throws OWLOntologyCreationException {

		// IRI of the initial ontology
		IRI initialOntology = IRI.create(args[0]);
		
		// IRI of the expert ontology
		IRI expertOntology = IRI.create(args[1]);
		
		Set<OWLClassExpression> baseSet = null;
		// TODO: read the baseSet! From file? 
		// or let the user select from a list?

		OWLOntology o = null;
		try {
			o = om.loadOntology(initialOntology);
		}
		catch (OWLOntologyCreationException e) {
			System.err.print("Error loading ontology");
			System.exit(-1);
		}
		r = rf.createReasoner(o);
		r.precomputeInferences(InferenceType.CLASS_HIERARCHY);
		
		ExpertOracle eo = new ExpertImpl(expertOntology, baseSet);
		SamplingOracle so = new SamplingOracle(baseSet);
		
	}
	
	public Set<OWLClassExpression> getCounterExample(Set<OWLClassExpression> baseSet, ImplicationSet imps, OWLOntology o, ExpertOracle eo, SamplingOracle so, int k) {
		for (int i = 0; i < k; ++i) {
			Set<OWLClassExpression> query = so.sample();
			Set<OWLClassExpression> closure = imps.closure(query);
			Set<OWLClassExpression> completion = complete(baseSet, closure, o, eo);
			if (!closure.equals(completion)) {
				return(closure);
			}
		}
		
		// TODO: is returning emptyset correct in this case? Check.
		Set<OWLClassExpression> ce = Collections.<OWLClassExpression>emptySet();
		return(ce);
	}
	
	public int callsToSamplingOracle(double epsilon, double delta, int i) {
		return((int) Math.ceil(Math.log(delta/(i*(i + 1))) / Math.log(1 - epsilon)));
	}
	
	public Set<OWLClassExpression> complete(Set<OWLClassExpression> baseSet, Set<OWLClassExpression> query, OWLOntology o, ExpertOracle e) {
		OWLClassExpression queryConjunction = df.getOWLObjectIntersectionOf(query);
		
		if (queryConjunction.isBottomEntity())
			return(baseSet);

		Set<OWLClassExpression> completion = query;
		for (OWLClassExpression c : baseSet) {
			if (!query.contains(c)) {
				OWLSubClassOfAxiom ax = df.getOWLSubClassOfAxiom(queryConjunction, c);
				if (r.isEntailed(ax) || e.holds(ax))
					completion.add(c);
			}
		}
		
		return(completion);
	}
	
	public OWLOntology upperApproximation(Set<OWLClassExpression> baseSet, ExpertOracle eo, SamplingOracle so, OWLOntology o, double epsilon, double delta) {
		ImplicationSet imps = new ImplicationSet(baseSet);
		Set<OWLClassExpression> counterExample;
		
		// Hashtable storing implication->GCI
		// key: implicaton 
		// value: corresponding GCI
		Hashtable<Implication, OWLSubClassOfAxiom> implicationAxiomHash = new Hashtable<>();
		
		int iteration = 0;
		boolean found = false;
		while (!(counterExample = getCounterExample(baseSet, imps, o, eo, so, callsToSamplingOracle(epsilon, delta, iteration))).equals(Collections.emptySet())) {
			found = false;
			for (Implication imp : imps) {
				if (!counterExample.containsAll(imp.getPremise())) {
					Set<OWLClassExpression> newPremise = new HashSet<OWLClassExpression>(imp.getPremise());
					newPremise.retainAll(counterExample);
					
					Set<OWLClassExpression> newConclusion = new HashSet<OWLClassExpression>(complete(baseSet, newPremise, o, eo));
					if (!newPremise.equals(newConclusion)) {
						found = true;
						// remove imp from imps
						imps.remove(imp);
						// get the GCI corresponding to imp
						OWLSubClassOfAxiom ax = implicationAxiomHash.get(imp);
						// remove the GCI constructed from imp from the ontology
						o.remove(ax);

						// construct the new implication
						Implication newImp = new Implication(newPremise, newConclusion, df);
						// add newImp to imps if the same implication is not already in imps
						if (imps.add(newImp)) {
							// add the GCI constructed from newImp to the ontology
							OWLSubClassOfAxiom newAx = newImp.toGCI();
							implicationAxiomHash.put(newImp, newAx);
							o.add(newAx);
						}

					}
				}
			}
			if (!found) {
				Set<OWLClassExpression> newConclusion = new HashSet<OWLClassExpression>(complete(baseSet, counterExample, o, eo));
				// construct the new implication
				Implication newImp = new Implication(counterExample, newConclusion, df);
				if (imps.add(newImp)) {
					// add the GCI constructed from newImp to the ontology
					OWLSubClassOfAxiom newAx = newImp.toGCI();
					implicationAxiomHash.put(newImp, newAx);
					o.add(newAx);
				}
			}
			++iteration;
		}
		
		return(o);
	}

}
