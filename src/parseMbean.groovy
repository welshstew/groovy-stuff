/**
 * Created by swinchester on 1/08/16.
 */


String mbean = "org.apache.activemq:brokerName=broker3-amq-1-aaomq,destinationName=queue.two,destinationType=Queue,type=Broker"
String replaced = mbean.replace("org.apache.activemq:", "")
def kvMap = [:]
replaced.tokenize(',').each { token ->
    def kv = token.tokenize('=')
    kvMap.put(kv[0],kv[1])
}
print kvMap
