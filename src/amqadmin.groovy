import de.vandermeer.asciitable.v2.RenderedTable
import de.vandermeer.asciitable.v2.V2_AsciiTable
import de.vandermeer.asciitable.v2.render.V2_AsciiTableRenderer
import de.vandermeer.asciitable.v2.render.WidthLongestWord
import de.vandermeer.asciitable.v2.themes.V2_E_TableThemes
@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.7.1')
@Grab(group='log4j', module='log4j', version='1.2.17')
@Grab(group='org.slf4j', module='slf4j-api', version='1.7.7')
@Grab(group='org.slf4j', module='slf4j-log4j12', version='1.7.7')
@Grab(group='de.vandermeer', module = 'asciitable', version = '0.2.5')


import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovyx.net.http.HTTPBuilder
import static groovyx.net.http.ContentType.JSON
import static groovyx.net.http.Method.POST
import org.apache.log4j.*
import groovy.util.logging.*


class TableProducer {

    //expecting a list of maps
    def generate(Map mapOfMaps){

        JsonOutput.println(mapOfMaps)

        V2_AsciiTable at = new V2_AsciiTable();
        List columns = new ArrayList<String>(mapOfMaps.values()[0].keySet())

        def colArray = columns.toArray()
        at.addRule();
        at.addRow(colArray);
        at.addRule();

        mapOfMaps.each { key, value ->
            Map thing = value

            def row = []
            colArray.each { col ->
                row.add(thing[col])
            }

            at.addRow(row.toArray());
            at.addRule()
        }

        V2_AsciiTableRenderer rend = new V2_AsciiTableRenderer();
        rend.setTheme(V2_E_TableThemes.UTF_LIGHT.get());
        rend.setWidth(new WidthLongestWord());

        RenderedTable rt = rend.render(at);
        println rt
    }
}


@Log4j
class JolokiaAdmin{

    def amqadmin(cli, args) {

        def config = new ConfigSlurper().parse(new File('log4jconfig.groovy').toURL())
        PropertyConfigurator.configure(config.toProperties())

        // this will print/write as the loglevel is info
        log.debug 'Execute amqadmin.'

        def options = cli.parse(args)
        def cfg = [:]

        //we need to have the json file OR command line args to run this stuff
//        println JsonOutput.toJson(options)
        if (!options) {
            return
        }
        // Show usage text when -h or --help option is used.
        if (options.h) {
            cli.usage()
            return
        }

        if(!options.j){
            cli.usage()
            return
        }else {
            println options.j
            cfg = parseProps(options.j)

        }

        if(options.c){
            println 'things: ' + JsonOutput.toJson(options)
        }

        if(options.q){
            println 'q: ' + JsonOutput.toJson(options)
        }



        return runCommands(cfg)
    }

    def Map parseProps(def propFile){
        def config = new JsonSlurper().parse(new File(propFile))
        return config
    }


    def Map buildRequest(Map config){

        def req = [path:"",headers:[:],body:[:]]

        req.headers = ["kube-namespace": config.namespace,
                       "kube-label": config.label,
                       "Authorization": "Bearer ${config.token}",
                       "Accept": JSON]


        if(config.command == 'stats'){
            req.path = "/jolokia/aggregate"
            //comma separated list for attribubtes
            def attList = "${config.properties}".split(",").collect()
            req.body = [type:"read",
                        mbean:"org.apache.activemq:type=Broker,brokerName=kube-lookup,destinationType=Queue,destinationName=${config.queue}",
                        attribute: attList]
        }

        if(config.command == 'purge'){
            req.path = "/jolokia/purge"
            req.headers.put("queueName", config.queue)
            println "attempting to purge ${config.queue}"
        }
        return req
    }

    def String runCommands(Map config){

        def req = buildRequest(config)
        def http = new HTTPBuilder(config.api)
        http.ignoreSSLIssues()
        def jsonOut

        http.request(POST, JSON) {
            uri.path = req.path
            body =  JsonOutput.toJson(req.body)
            headers = req.headers
            response.success = { resp, json ->
                assert resp.statusLine.statusCode == 200
                //here we have our json response to do stuff with
                //println json
                jsonOut = json
            }

            response.failure = { resp ->
                println(resp.data)
                jsonOut = resp.data
            }
        }

        if(config.command == 'purge'){
            println JsonOutput.toJson(jsonOut)

            outputStats(jsonOut)
            def success = jsonOut.findAll {it.status == 200}
            def fail = jsonOut.findAll {it.status != 200 }
            println 'success :' + success
            println 'fail: ' + fail

        }

        if(config.command == 'stats'){
            outputStats(jsonOut)
        }

    }


    def outputStats(ArrayList data){
        def overallMap = [:]

        println JsonOutput.toJson(data)

        data.each { item ->

            //we want a datamap of the values consistently as:
            //

            def dataMap = [:]

            String mbeanName

            //if item['value'] does not exist then populate the value and class them as nil
            if(item['value'] == null){

                mbeanName = item.request.mbean
                Map meta = parseMbean(mbeanName)
                meta.put("status", item.status)

                if(item.request.operation == 'purge'){

                    dataMap.put(mbeanName, meta)

                }else{
                    def exceptionMap = [:]
                    item.request.attribute.each{ att ->
                        exceptionMap.put(att, "null")
                    }

                    dataMap.put(mbeanName, exceptionMap)
                    dataMap[mbeanName] << meta
                }

            }else{
                def itemValue = item['value'] as Map
                //get the request attributes
                //if the value has a map in it containing one of those values - then we have an EXACT match
                if(itemValue.containsKey(item.request.attribute[0])){
                    //get the mbean name
                    mbeanName = item.request.mbean

                    dataMap.put(mbeanName, itemValue)
                    Map meta = parseMbean(mbeanName)
                    meta.put("status", item.status)
                    dataMap[mbeanName] << meta
                }else{

                    //in the ideal format already
                    item['value'].each { it ->
                        Map meta = parseMbean(it.key)
                        meta.put("status", item.status)
                        dataMap << it
                        dataMap[it.key] << meta

                    }
                    println 'as required!' + JsonOutput.toJson(dataMap)

                }
            }

            overallMap << dataMap
            //listDataMap.add(dataMap)
        }

        println 'OVERALL: ' + JsonOutput.toJson(overallMap)

        TableProducer tableProducer = new TableProducer()
        tableProducer.generate(overallMap)
        //println JsonOutput.toJson(listDataMap)
    }

    def Map parseMbean(String mbean){
        String replaced = mbean.replace("org.apache.activemq:", "")
        def kvMap = [:]
        replaced.tokenize(',').each { token ->
            def kv = token.tokenize('=')
            kvMap.put(kv[0],kv[1])
        }
        kvMap.put("mbean",mbean)
        return kvMap
    }

}

def cli = new CliBuilder(usage: 'amqadmin.groovy -j jsonFile')
// Create the list of options.
cli.with {
    h longOpt: 'help', 'Show usage information'
    j longOpt: 'json', args: 1, argName: 'json', 'json config file'
    q longOpt: 'queue', 'Queue name/wildcard'
    c longOpt: 'command', 'Command like stats, purge, etc'
    t longOpt: 'token', 'Openshift Token'
    a longOpt: 'api', 'Jolokia API URL'
    l longOpt: 'label', 'Kube label'
}

JolokiaAdmin hi = new JolokiaAdmin()
hi.amqadmin(cli, args)


