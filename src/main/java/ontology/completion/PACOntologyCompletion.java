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
		
		// TODO Check this: is returning emptyset correct in this case? .
		// Set<OWLClassExpression> ce = Collections.<OWLClassExpression>emptySet();
		// return(ce);
		return(null);
	}
	
	/*
	 * Given epsilon, delta, and iteration i returns the number of calls to the sampling oracle.
	 */
	public int callsToSamplingOracle(double epsilon, double delta, int i) {
		return((int) Math.ceil(Math.log(delta/(i*(i + 1))) / Math.log(1 - epsilon)));
	}
	
	/*
	 * Complete a given set of concept expressions 
	 * baseSet: The base set of concept expressions 
	 * query: The set of concept expressions to be completed
	 * ontology: 
	 * expert: The domain expert
	 */
	public Set<OWLClassExpression> complete(Set<OWLClassExpression> baseSet, Set<OWLClassExpression> query, OWLOntology ontology, ExpertOracle expert) {
		OWLClassExpression queryConjunction = df.getOWLObjectIntersectionOf(query);
		
		if (queryConjunction.isBottomEntity()) {
			Set<OWLClassExpression> s = new HashSet<OWLClassExpression>();
			s.add(df.getOWLNothing());
			return(s);
		}

		Set<OWLClassExpression> completion = new HashSet<OWLClassExpression>(query);
		for (OWLClassExpression c : baseSet) {
			if (!query.contains(c)) {
				OWLSubClassOfAxiom ax = df.getOWLSubClassOfAxiom(queryConjunction, c);
				if (r.isEntailed(ax) || expert.holds(ax))
					completion.add(c);
			}
		}
		
		return(completion);
	}
	
	/*
	 * Computes an upper approximation of expert's view of the domain.
	 * ontology: the initial ontology
	 */
	public OWLOntology upperApproximation(Set<OWLClassExpression> baseSet, ExpertOracle expert, SamplingOracle sampler, OWLOntology ontology, double epsilon, double delta) {
		ImplicationSet imps = new ImplicationSet(baseSet);
		Set<OWLClassExpression> counterExample;
		
		// Hashtable for storing implication -> GCI. 
		// Later used to find out the GCI to remove from the ontology
		// Key: implicaton 
		// Value: corresponding GCI
		Hashtable<Implication, OWLSubClassOfAxiom> implicationAxiomHash = new Hashtable<>();
		
		int iteration = 0;
		boolean found = false;
		// TODO: Check whether the assignment returns a value (like in C)
		while ((counterExample = getCounterExample(baseSet, imps, ontology, expert, sampler, callsToSamplingOracle(epsilon, delta, iteration))) != null) { 
			found = false;
			for (Implication imp : imps) {
				if (!counterExample.containsAll(imp.getPremise())) {
					Set<OWLClassExpression> newPremise = new HashSet<OWLClassExpression>(imp.getPremise());
					newPremise.retainAll(counterExample);
				
					// ontology is the initial ontology extended with GCIs resulting from new implications.
					// every change to the implication set (like adding or removing implications) should be reflected to the ontology
					Set<OWLClassExpression> newConclusion = new HashSet<OWLClassExpression>(complete(baseSet, newPremise, ontology, expert));
					if (!newPremise.equals(newConclusion)) {
						found = true;
						// remove imp from imps
						imps.remove(imp);
						// get the GCI corresponding to imp
						OWLSubClassOfAxiom ax = implicationAxiomHash.get(imp);
						// remove the GCI constructed from imp from the ontology
						ontology.remove(ax);

						// construct the new implication
						Implication newImp = new Implication(newPremise, newConclusion, df);
						// add newImp to imps if the same implication is not already in imps
						if (imps.add(newImp)) {
							// add the GCI constructed from newImp to the ontology
							OWLSubClassOfAxiom newAx = newImp.toGCI();
							implicationAxiomHash.put(newImp, newAx);
							ontology.add(newAx);
						}

					}
				}
			}
			if (!found) {
				Set<OWLClassExpression> newConclusion = new HashSet<OWLClassExpression>(complete(baseSet, counterExample, ontology, expert));
				// construct the new implication
				Implication newImp = new Implication(counterExample, newConclusion, df);
				if (imps.add(newImp)) {
					// add the GCI constructed from newImp to the ontology
					OWLSubClassOfAxiom newAx = newImp.toGCI();
					implicationAxiomHash.put(newImp, newAx);
					ontology.add(newAx);
				}
			}
			++iteration;
		}
		
		return(ontology);
	}

}
