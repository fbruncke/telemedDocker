package telemed.server;

import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import telemed.domain.ClinicalQuantity;
import telemed.domain.TeleObservation;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class HL7JsonBuilder implements Builder {

    /*
{
  "ClinicalDocument": {
    "effectiveTime": "",
    "patient": {
      "id": ""
    },
    "component": {
      "observation": [
        {
          "code": "",
          "value": ""
        },
        {
          "code": "",
          "value": ""
        }
      ]
    }
  }
}

    */


    // The JSON document to be built
    private JSONObject jsonDocument;
    private JSONArray jsonObservations;
    private JSONObject patientJson;

    public JSONObject getResult() {
        return jsonDocument;
    }

    @Override
    public void buildHeader(TeleObservation to) {
        // The HL7 time format is
        // DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        // but I use ISO8601 as it is easy to read, and support time zones.
        OffsetDateTime observationTime = to.getTime();
        String timeInHL7Format = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(observationTime);
        //time.setAttribute("value", timeInHL7Format);

        patientJson = new JSONObject();

        jsonDocument = new JSONObject();
        jsonDocument.put("ClinicalDocument", new JSONObject()
                                                    .put("effectiveTime",new JSONObject().put("value", timeInHL7Format) )
                                                    .put("patient",patientJson ));

        //jo.put("age", "22");
        //jo.put("city", "chicago");
        //System.out.println("test: " + jsonDocument);
    }

    @Override
    public void buildPatientInfo(TeleObservation to) {
        patientJson.put("id",new JSONObject().put("extension", to.getPatientId()));

    }

    @Override
    public void buildObservationList(TeleObservation to) {
        jsonDocument.put("component", new JSONObject().put("observation",jsonObservations = new JSONArray()));



    }

    @Override
    public void appendObservation(ClinicalQuantity quantity) {


        JSONObject jsonObservation = new JSONObject();
        jsonObservation.put("code",new JSONObject().put("code",quantity.getCode()).put("displayName",quantity.getDisplayName()));
        jsonObservation.put("value",new JSONObject().put("unit",quantity.getUnit()).put("value",quantity.getValue()));

        jsonObservations.put(jsonObservation);


    }
}
