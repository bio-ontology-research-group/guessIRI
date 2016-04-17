@Grapes([
          @Grab(group='com.google.code.gson', module='gson', version='2.3.1'),
          @Grab(group='org.slf4j', module='slf4j-log4j12', version='1.7.10'),
          @Grab(group='org.semanticweb.elk', module='elk-owlapi', version='0.4.2'),
          @Grab(group='net.sourceforge.owlapi', module='owlapi-api', version='4.1.0'),
          @Grab(group='net.sourceforge.owlapi', module='owlapi-apibinding', version='4.1.0'),
          @Grab(group='net.sourceforge.owlapi', module='owlapi-impl', version='4.1.0'),
          @Grab(group='net.sourceforge.owlapi', module='owlapi-parsers', version='4.1.0'),
          @Grab(group='org.codehaus.gpars', module='gpars', version='1.1.0'),
          @Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.7' ),
	  @GrabConfig(systemClassLoader=true)
	])
 
import org.semanticweb.owlapi.io.* 
import org.semanticweb.owlapi.model.*
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model.AddImport
import groovyx.net.http.HTTPBuilder
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.elk.owlapi.ElkReasonerConfiguration
import org.semanticweb.elk.reasoner.config.*
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.reasoner.*
import org.semanticweb.owlapi.reasoner.structural.StructuralReasoner
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.io.*;
import org.semanticweb.owlapi.owllink.*;
import org.semanticweb.owlapi.util.*;
import org.semanticweb.owlapi.search.*;
import org.semanticweb.owlapi.manchestersyntax.renderer.*;
import org.semanticweb.owlapi.reasoner.structural.*
import groovy.json.JsonBuilder

OWLOntologyLoaderConfiguration config = new OWLOntologyLoaderConfiguration();
config.setFollowRedirects(false);
config.setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT);

def uriSchemes = [:]

new HTTPBuilder('http://aber-owl.net/').get(path: 'service/api/getStatuses.groovy') { resp, ontologies ->
  ontologies = ontologies.findAll { name, status -> status.status == 'classified' }
  ontologies.each { name, status ->
    println "[FINDIRI] Processing " + name

    try {
      def manager = OWLManager.createOWLOntologyManager();
      def ontology = manager.loadOntologyFromOntologyDocument(new IRIDocumentSource(IRI.create("http://localhost/rtestserv/ontology/"+name+"/download")), config);

      def currentLSB
      def iris = []
      def classes = ontology.getClassesInSignature(false)

      def classList = new ArrayList<>(classes)
      if(classList.size() > 5000) {
        classList = classList.subList(0, 5000)
      }
      
      classList.each { oClass ->
        def iri = oClass.getIRI().toString()

        iris.each {
          currentLSB = longestCommonSubstring(iri, it)
        }

        iris << iri
      }

      uriSchemes[name] = currentLSB
      println "[FINDIRI] Most likely IRI scheme: " + currentLSB

      new File("ontology_iri_patterns.json").write(new JsonBuilder(uriSchemes).toPrettyString())
    } catch(e) {
      println "[FINDIRI] Unable to process " + name + ". Cause: " + e.getMessage()
    }
  }
}

// from https://en.wikibooks.org/wiki/Algorithm_Implementation/Strings/Longest_common_substring#Java

def longestCommonSubstring(String S1, String S2) {
    int Start = 0;
    int Max = 0;
    for (int i = 0; i < S1.length(); i++)
    {
        for (int j = 0; j < S2.length(); j++)
        {
            int x = 0;
            while (S1.charAt(i + x) == S2.charAt(j + x))
            {
                x++;
                if (((i + x) >= S1.length()) || ((j + x) >= S2.length())) break;
            }
            if (x > Max)
            {
                Max = x;
                Start = i;
            }
         }
    }
    return S1.substring(Start, (Start + Max));
}


