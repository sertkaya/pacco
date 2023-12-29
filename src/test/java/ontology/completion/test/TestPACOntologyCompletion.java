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

		// OWLClassExpression clsA = df.getOWLClass("A");
		// OWLClassExpression clsB = df.getOWLClass("B");
		// OWLClassExpression clsC = df.getOWLClass("C");
		OWLClassExpression clsA = df.getOWLClass("http://www.semanticweb.org/bs/ontologies/2023/11/untitled-ontology-20#A");
		OWLClassExpression clsB = df.getOWLClass("http://www.semanticweb.org/bs/ontologies/2023/11/untitled-ontology-20#B");
		OWLClassExpression clsC = df.getOWLClass("http://www.semanticweb.org/bs/ontologies/2023/11/untitled-ontology-20#C");
		OWLClassExpression clsD = df.getOWLClass("http://www.semanticweb.org/bs/ontologies/2023/11/untitled-ontology-20#D");
		OWLObjectProperty propR = df.getOWLObjectProperty("r");
	
		OWLClassExpression existsRA = df.getOWLObjectSomeValuesFrom(propR, clsA);
		OWLClassExpression existsRB = df.getOWLObjectSomeValuesFrom(propR, clsB);
		
		Set<OWLClassExpression> baseSet = new HashSet<OWLClassExpression>();
		baseSet.add(clsA);
		baseSet.add(clsB);
		baseSet.add(clsC);
		baseSet.add(clsD);
		// baseSet.add(existsRA);
		// baseSet.add(existsRB);
	
		File expertOntology = new File("/home/bs/research/dev/pacco/src/test/resources/expertOntology.owx");
		IRI expertOntologyIRI = IRI.create(expertOntology);
		ExpertOracle expert = new ReasonerExpert(expertOntologyIRI, baseSet);
		System.out.println("Ontology prefix: " + expert.getExpertOntology().getOntologyID().getOntologyIRI().get());
		System.out.println("Axioms:");
		expert.getExpertOntology().logicalAxioms().forEach(System.out::println);

		SamplingOracle sampler = new RandomSampler(baseSet);

		File myOntology = new File("/home/bs/research/dev/pacco/src/test/resources/myOntology.owx");
		IRI myOntologyIRI = IRI.create(myOntology);

		PACOntologyCompletion pacCompletion = new PACOntologyCompletion(myOntologyIRI, baseSet, expert, sampler);
		pacCompletion.upperApproximation(0.2, 0.9);
		// System.out.println(pacCompletion.callsToSamplingOracle(0.5, 0.5, 3));
	}
}