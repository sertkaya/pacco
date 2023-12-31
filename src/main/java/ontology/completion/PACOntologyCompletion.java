package ontology.completion;

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
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;


public class PACOntologyCompletion {
	
	private OWLOntology ontology;
	private OWLOntologyManager om;
	private OWLDataFactory df;
	private OWLReasonerFactory rf;
	private OWLReasoner reasoner;
	
	private Set<OWLClassExpression> baseSet;
	private ExpertOracle expert;
	private SamplingOracle sampler;


	/**
	 * @param baseSet
	 * @param ontology: IRI of the (possibly empty) initial ontology 
	 * @param expert: The domain expert 
	 * @param sampler
	 */
	public PACOntologyCompletion(IRI ontologyIRI, Set<OWLClassExpression> baseSet, ExpertOracle expert, SamplingOracle sampler) {
		om = OWLManager.createOWLOntologyManager();
		df = om.getOWLDataFactory();
		rf = new ReasonerFactory();

		this.baseSet = new HashSet<OWLClassExpression>(baseSet);
		// TODO: read the baseSet! From file? 
		// or let the user select from a list?

		OWLOntology ontology = null;
		try {
			ontology = om.loadOntology(ontologyIRI);
		}
		catch (OWLOntologyCreationException e) {
			System.err.print("Error loading ontology");
			System.exit(-1);
		}
		this.ontology = ontology;
		
		this.reasoner = rf.createReasoner(ontology);
		this.reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);

		this.expert = expert;
		this.sampler = sampler;
		
		
	}
	
	public Set<OWLClassExpression> getCounterExample(ImplicationList imps, int k) {
		for (int i = 0; i < k; ++i) {
			Set<OWLClassExpression> query = this.sampler.sample();
			Set<OWLClassExpression> closure = imps.closure(query);
			Set<OWLClassExpression> completion = complete(closure);
			if (!closure.equals(completion)) {
				return(closure);
			}
		}
		
		return(null);
	}
	
	/**
	 * Given epsilon, delta, and iteration i returns the number of calls to the sampling oracle.
	 */
	public int callsToSamplingOracle(double epsilon, double delta, int i) {
		return((int) Math.ceil(Math.log(delta/(i*(i + 1))) / Math.log(1 - epsilon)));
	}
	
	/**
	 * Complete a given set of concept expressions 
	 * @param query The set of concept expressions to be completed
	 */
	public Set<OWLClassExpression> complete(Set<OWLClassExpression> query) {

		OWLClassExpression queryConjunction;
		if (query.isEmpty())
			queryConjunction = df.getOWLThing();
		else
			queryConjunction = this.df.getOWLObjectIntersectionOf(query);
		
		if (queryConjunction.isBottomEntity()) {
			Set<OWLClassExpression> s = new HashSet<OWLClassExpression>();
			s.add(df.getOWLNothing());
			return(s);
		}

		Set<OWLClassExpression> completion = new HashSet<OWLClassExpression>(query);
		for (OWLClassExpression c : this.baseSet) {
			if (!query.contains(c)) {
				OWLSubClassOfAxiom ax = df.getOWLSubClassOfAxiom(queryConjunction, c);
				if (this.reasoner.isEntailed(ax) || this.expert.holds(ax))
					completion.add(c);
			}
		}
		
		return(completion);
	}
	
	/**
	 * Computes an upper approximation of expert's view of the domain.
	 * @param ontology the initial ontology
	 */
	public OWLOntology upperApproximation(double epsilon, double delta, IRI resultOntologyIRI) {
		ImplicationList imps = new ImplicationList(baseSet);
		Set<OWLClassExpression> counterExample;
		
		// Hashtable for storing implication -> GCI. 
		// Later used to find out the GCI to remove from the ontology
		// Key: implicaton 
		// Value: corresponding GCI
		Hashtable<Implication, OWLSubClassOfAxiom> implicationAxiomHash = new Hashtable<>();
		
		int iteration = 1;
		boolean found = false;

		while ((counterExample = getCounterExample(imps, callsToSamplingOracle(epsilon, delta, iteration))) != null) { 
			System.out.println("iteration:" + iteration);
			found = false;
			Implication imp = null;
			for (int i = 0; i < imps.size(); i++) {
				imp = imps.get(i);
				if (!counterExample.containsAll(imp.getPremise())) {
					Set<OWLClassExpression> newPremise = new HashSet<OWLClassExpression>(imp.getPremise());
					newPremise.retainAll(counterExample);
				
					// ontology is the initial ontology extended with GCIs resulting from new implications.
					// every change to the implication set (like adding or removing implications) should be reflected to the ontology
					Set<OWLClassExpression> newConclusion = new HashSet<OWLClassExpression>(complete(newPremise));
					if (!newPremise.equals(newConclusion)) {
						found = true;
						// construct the new implication
						newConclusion.removeAll(newPremise);
						Implication newImp = new Implication(newPremise, newConclusion, df);
						
						// replace imp with the newImp
						imps.set(i, newImp);
						
						// get the GCI corresponding to imp
						OWLSubClassOfAxiom ax = implicationAxiomHash.get(imp);
						// remove the GCI constructed from imp from the ontology
						this.ontology.remove(ax);
						System.out.println("Removed axiom: " + ax);

						// add the GCI constructed from newImp to the ontology
						OWLSubClassOfAxiom newAx = newImp.toGCI();
						implicationAxiomHash.put(newImp, newAx);
						this.ontology.add(newAx);
						System.out.println("Added axiom: " + newAx);
						if (!counterExample.containsAll(newConclusion)) {
						    break;
						}
					}
				}
			}
			if (!found) {
				Set<OWLClassExpression> newConclusion = new HashSet<OWLClassExpression>(complete(counterExample));
				// construct the new implication
				newConclusion.removeAll(counterExample);
				Implication newImp = new Implication(counterExample, newConclusion, df);
				if (imps.add(newImp)) {
					// add the GCI constructed from newImp to the ontology
					OWLSubClassOfAxiom newAx = newImp.toGCI();
					implicationAxiomHash.put(newImp, newAx);
					this.ontology.add(newAx);
					System.out.println("Added axiom: " + newAx);
				}
			}
			++iteration;
		}
		
		try {
			ontology.saveOntology(resultOntologyIRI);
		} catch (OWLOntologyStorageException e) {
			System.err.println("Error while saving ontology");
			e.printStackTrace();
		}
		return(ontology);
	}

}
