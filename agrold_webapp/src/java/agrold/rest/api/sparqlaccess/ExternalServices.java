package agrold.rest.api.sparqlaccess;

import java.util.ArrayList;
import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author tagny
 */
public class ExternalServices {

    public static String G_LINKS_SERVICE = "http://link.g-language.org/";
    public static String EUROPEPMC_SERVICE = "http://www.ebi.ac.uk/europepmc/webservices/rest/search/";
    
    public static String getPublicationsOfGeneOrProteinById(String entityId) {
        String source = "PubMed";
        String format = "json";
        String glinkJsonStr = APILib.executeHttpQuery(G_LINKS_SERVICE + entityId + "/extract=" + source + "/format=" + format);
        String result = "";
        JSONArray resultJsonArray = new JSONArray();
        // extract pubmed row from the json result
        try {
            JSONArray jsonArray = new JSONArray(glinkJsonStr);
            int count = jsonArray.length(); // get totalCount of all jsonObjects
            for (int i = 0; i < count; i++) {   // iterate through jsonArray 
                JSONObject jsonObject = jsonArray.getJSONObject(i);  // get jsonObject @ i position 
                JSONObject tempObj = jsonObject.getJSONObject(entityId);
                Iterator<String> keys = tempObj.keys();
                ArrayList<String> ids = new ArrayList<>();
                while (keys.hasNext()) {
                    String key = keys.next();
                    JSONArray tempArray = tempObj.getJSONArray(key);
                    int nb = tempArray.length();                    
                    for (int j = 0; j < nb; j++) {
                        try {
                            JSONObject paperObj = tempArray.getJSONObject(j);
                            String database = paperObj.getString("Database");
                            if (database.equals(source)) {
                                String id = paperObj.getString("ID");
                                if (!ids.contains(id)) { // avoid duplications e.g. gene GRMZM2G022192
                                    ids.add(id);
                                    //System.out.println(id);
                                    String pubXMLDetails = APILib.executeHttpQuery(EUROPEPMC_SERVICE + "query=" + id);
                                    paperObj.put("Authors", APILib.getValueFromXMLDocTagName(pubXMLDetails, "authorString"));
                                    paperObj.put("Title", APILib.getValueFromXMLDocTagName(pubXMLDetails, "title"));
                                    paperObj.put("Year", APILib.getValueFromXMLDocTagName(pubXMLDetails, "pubYear"));
                                    paperObj.put("Journal", APILib.getValueFromXMLDocTagName(pubXMLDetails, "journalTitle"));
                                    resultJsonArray.put(paperObj);
                                } else {
                                    //System.out.println(id);
                                }                                
                            }
                        } catch (JSONException e) {

                        }
                        //System.out.println(ids);
                    }
                }
            }
        } catch (JSONException e) {
        }
        return resultJsonArray.toString();
    }
    
    

    public static String getCharacterDataFromElement(Element e) {
        Node child = e.getFirstChild();
        if (child instanceof CharacterData) {
            CharacterData cd = (CharacterData) child;
            return cd.getData();
        }
        return "";
    }

    public static void main(String[] args) {
        //System.out.println(getPublicationsOfGeneOrProteinById("P0C127"));
        //System.out.println(getPublicationsOfGeneOrProteinById("GRMZM2G022192"));
        getPublicationsOfGeneOrProteinById("GRMZM2G022192");
    }
}
