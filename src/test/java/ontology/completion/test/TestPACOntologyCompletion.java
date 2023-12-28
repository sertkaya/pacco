package ontology.completion.test;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.IRIComparator;

import ontology.completion.ExpertOracle;
import ontology.completion.Implication;
import ontology.completion.ImplicationList;
import ontology.completion.PACOntologyCompletion;
import ontology.completion.RandomSampler;
import ontology.completion.ReasonerExpert;
import ontology.completion.SamplingOracle;

public class TestPACOntologyCompletion {

	static OWLOntologyManager om = OWLManager.createOWLOntologyManager();
	static OWLDataFactory df = om.getOWLDataFactory();
	
	
	public static void main(String[] args) {

		OWLClassExpression clsA = df.getOWLClass("A");
		OWLClassExpression clsB = df.getOWLClass("B");
		OWLClassExpression clsC = df.getOWLClass("C");
		OWLObjectProperty propR = df.getOWLObjectProperty("r");
	
		OWLClassExpression existsRA = df.getOWLObjectSomeValuesFrom(propR, clsA);
		OWLClassExpression existsRB = df.getOWLObjectSomeValuesFrom(propR, clsB);
		
		Set<OWLClassExpression> baseSet = new HashSet<OWLClassExpression>();
		baseSet.add(clsA);
		baseSet.add(clsB);
		baseSet.add(clsC);
		baseSet.add(existsRA);
		baseSet.add(existsRB);
	
		File expertOntology = new File("expertOntology.owl");
		IRI expertOntologyIRI = IRI.create(expertOntology);
		ExpertOracle expert = new ReasonerExpert(expertOntologyIRI, baseSet);
		SamplingOracle sampler = new RandomSampler(baseSet);

		File myOntology = new File("myOntology.owl");
		IRI myOntologyIRI = IRI.create(myOntology);

		PACOntologyCompletion pacCompletion = new PACOntologyCompletion(myOntologyIRI, expert, sampler);
		pacCompletion.upperApproximation(0, 0);
	}
}