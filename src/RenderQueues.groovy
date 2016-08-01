import de.vandermeer.asciitable.v2.RenderedTable
import de.vandermeer.asciitable.v2.V2_AsciiTable
import de.vandermeer.asciitable.v2.render.V2_AsciiTableRenderer
import de.vandermeer.asciitable.v2.render.WidthAbsoluteEven
import de.vandermeer.asciitable.v2.render.WidthLongestWord
import de.vandermeer.asciitable.v2.themes.V2_E_TableThemes

/**
 * Created by swinchester on 1/08/16.
 */

// see: https://github.com/vdmeer/asciitable

def queueListMap =[
        [broker:'broker1',
         queue: 'queue1',
         mbean: 'org.apache.activemq:brokerName=broker3-amq-1-aaomq,destinationName=queue.two,destinationType=Queue,type=Broker',
         consumerCount: 0,
         queueSize:12]
        ,
        [broker:'broker2',
         queue: 'queue2',
                mbean: 'org.apache.activemq:brokerName=broker3-amq-1-aaomq,destinationName=queue.two,destinationType=Queue,type=Broker',
         consumerCount: 4,
         queueSize:0]
]

@Grab(group='de.vandermeer', module = 'asciitable', version = '0.2.5')

def thingy = new TablePoc();
thingy.generate(queueListMap)


V2_AsciiTable at = new V2_AsciiTable();

//build the columns
List columns = new ArrayList<String>(queueListMap[0].keySet())
at.addRule();
at.addRow(columns.toArray());
at.addRule();

queueListMap.each { it->
    at.addRow(it.broker, it.queue, it.mbean,it.consumerCount,it.queueSize);
    at.addRule()
}

//at.addRule();
//at.addRow("first row (col1)", "with some information (col2)");
//at.addRule();
//at.addRow("second row (col1)", "with some information (col2)");
//at.addRule();

V2_AsciiTableRenderer rend = new V2_AsciiTableRenderer();
rend.setTheme(V2_E_TableThemes.UTF_LIGHT.get());
rend.setWidth(new WidthLongestWord());

RenderedTable rt = rend.render(at);

println rt



class TablePoc {

    //expecting a list of maps
    def generate(List listOfMaps){

        V2_AsciiTable at = new V2_AsciiTable();
        List columns = new ArrayList<String>(listOfMaps[0].keySet())
        at.addRule();
        at.addRow(columns.toArray());
        at.addRule();

        listOfMaps.each { it ->
            Map thing = it
            at.addRow(thing.values().asList().toArray());
            at.addRule()
        }

        V2_AsciiTableRenderer rend = new V2_AsciiTableRenderer();
        rend.setTheme(V2_E_TableThemes.UTF_LIGHT.get());
        rend.setWidth(new WidthLongestWord());

        RenderedTable rt = rend.render(at);
        println rt
    }
}
