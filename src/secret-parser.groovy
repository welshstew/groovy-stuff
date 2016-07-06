/**
 * Secret parser for openshift/kubernetes in groovy.
 * Extract base64 encoded files with groovy ;-D
 */
import org.yaml.snakeyaml.Yaml

@Grapes(
        @Grab(group='org.yaml', module='snakeyaml', version='1.17')
)

def inputFile = '/Users/swinchester/Downloads/thing-broker-secret.yaml'
def outputDir = '/Users/swinchester/temp/'

String f = new File(inputFile).text
Yaml yaml = new Yaml()
def map = yaml.load(f)
map.data.each(){ key, value ->
    def thing = value as String
    byte [] decoded = thing.decodeBase64()
    new File("${outputDir}${key}").withOutputStream {
        it.write decoded
    }
    println "extracted ${key} to ${outputDir}${key}"
}
