import groovy.util.XmlSlurper

def ontologyIRI = args[0]
def ont = new XmlSlurper(false, true).parse(new File(ontologyIRI)) 
println ont['RDF']
println ont[0].attributes()
