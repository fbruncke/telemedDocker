package qas;

//import jdk.nashorn.internal.ir.debug.ObjectSizeCalculator;
import kong.unirest.json.JSONObject;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import telemed.domain.TeleObservation;
import telemed.server.Director;
import telemed.server.HL7Builder;
import telemed.server.HL7JsonBuilder;
import telemed.server.MetadataBuilder;
import telemed.storage.MetaData;
import telemed.storage.XDSBackend;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import java.lang.instrument.Instrumentation;

public class TestHL7BuilderPerformance {
    private MetaData md;


    @Test
    public void measureHL7BuilderPerformance()
    {
        Random rnd = new Random();

        List<TeleObservation> teleobservations = new ArrayList<>();
        String[] names = {"Bjarne", "Anton", "Børge", "Hugo", "Cornelius", "Mathæus","Matheus", "Alfons-Åberg","Ole", "Gurli"};

        for (int i = 0; i < 10; i++) {
            teleobservations.add(new TeleObservation(names[i], 128.0 + rnd.nextDouble(),  89.0 + + rnd.nextDouble() ));
        }

        int noOfBuildHl7 = 10000;
        long start = System.currentTimeMillis();
        for (int i = 0; i < noOfBuildHl7; i++) {
            buildMdAndHl7For(teleobservations.get(rnd.nextInt(10)));
        }
        long finish = System.currentTimeMillis();
        long timeElapsed = finish - start;
        System.out.printf("TimeElapsed for %d HL7 XML builds: %d ms", noOfBuildHl7, timeElapsed);
        System.out.println("");
    }

    @Test
    public void measureHL7XmlSize()
    {
        TeleObservation to = new TeleObservation("Bjarne",128, 89);

        //MetadataBuilder mdBuilder = new MetadataBuilder();
        //Director.construct(to, mdBuilder);
        //md = mdBuilder.getResult();

        HL7Builder hl7Builder = new HL7Builder();
        Director.construct(to, hl7Builder);
        Document hl7 = hl7Builder.getResult();

        //test xml format
        System.out.println("test: "+ convertXmlDomToString(hl7));

        //long objSize = ObjectSizeCalculator.getObjectSize(hl7);

         int xmlStringSize = convertXmlDomToString(hl7).length();

        System.out.println("size of xml: " + xmlStringSize);
        System.out.println("");


    }




    @Test
    public void measureHL7JsonBuilderPerformance()
    {
        Random rnd = new Random();

        List<TeleObservation> teleobservations = new ArrayList<>();
        String[] names = {"Bjarne", "Anton", "Børge", "Hugo", "Cornelius", "Mathæus","Matheus", "Alfons-Åberg","Ole", "Gurli"};

        for (int i = 0; i < 10; i++) {
            teleobservations.add(new TeleObservation(names[i], 128.0 + rnd.nextDouble(),  89.0 + + rnd.nextDouble() ));
        }

        int noOfBuildHl7 = 10000;
        long start = System.currentTimeMillis();
        for (int i = 0; i < noOfBuildHl7; i++) {
            buildMdAndHl7JsonFor(teleobservations.get(rnd.nextInt(10)));
        }
        long finish = System.currentTimeMillis();
        long timeElapsed = finish - start;
        System.out.printf("TimeElapsed for %d HL7 JSON builds: %d ms", noOfBuildHl7, timeElapsed);
        System.out.println("");
    }

    /**
     * Used this resouserce for XML to Json conversion:
     * https://www.convertjson.com/xml-to-json.htm
     *
     * and this resource for visual validating the format:
     * https://jsonformatter.org/json-viewer
     */
    @Test
    public void measureHL7JsonSize()
    {
        TeleObservation to = new TeleObservation("Bjarne",128, 89);

        //MetadataBuilder mdBuilder = new MetadataBuilder();
        //Director.construct(to, mdBuilder);
        //md = mdBuilder.getResult();

        HL7JsonBuilder hl7Builder = new HL7JsonBuilder();
        Director.construct(to, hl7Builder);
        JSONObject hl7Json = hl7Builder.getResult();

        //test json format
        System.out.println("test JSON formatting: " + hl7Json);

        //long objSize = ObjectSizeCalculator.getObjectSize(hl7Json);

        //System.out.printf("Size of JSON HL7 document: %d bytes", objSize);
        System.out.printf("HL7 Json size: %d",  hl7Json.toString().length());
        System.out.println("");


    }


    private void buildMdAndHl7For(TeleObservation to) {
        //MetadataBuilder mdBuilder = new MetadataBuilder();
        //Director.construct(to, mdBuilder);
        //md = mdBuilder.getResult();

        HL7Builder hl7Builder = new HL7Builder();
        Director.construct(to, hl7Builder);
        Document hl7 = hl7Builder.getResult();


    }

    private void buildMdAndHl7JsonFor(TeleObservation to) {
        //MetadataBuilder mdBuilder = new MetadataBuilder();
        //Director.construct(to, mdBuilder);
        //md = mdBuilder.getResult();

        HL7JsonBuilder hl7JsonBuilder = new HL7JsonBuilder();
        Director.construct(to, hl7JsonBuilder);
        JSONObject hl7Json = hl7JsonBuilder.getResult();


    }

    public static String convertXmlDomToString(Document xmlDocument) {

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            transformer = tf.newTransformer();

            // Uncomment if you do not require XML declaration
            // transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

            //A character stream that collects its output in a string buffer,
            //which can then be used to construct a string.
            StringWriter writer = new StringWriter();

            //transform document to string
            transformer.transform(new DOMSource(xmlDocument), new StreamResult(writer));

            String test = writer.getBuffer().toString();

            System.out.println(test);


            return test;
        } catch (TransformerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
