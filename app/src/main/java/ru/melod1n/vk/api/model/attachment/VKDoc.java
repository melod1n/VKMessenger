package ru.melod1n.vk.api.model.attachment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

public class VKDoc implements Serializable {
    private static final long serialVersionUID = 1L;
    public long id;//0 means no value
    public long owner_id;//0 means no value
    public String title;
    public String url;
    public long size;
    public String ext;
    public String thumb;//for photos. 130*65.
    public String thumb_s;//for photos. 100*50.
    public String access_key;
    
    public static VKDoc parse(JSONObject o) {
        VKDoc d = new VKDoc();
        d.id = o.optLong("id");
        d.owner_id = o.optLong("owner_id");
        d.title = o.optString("title");
        d.url = o.optString("url");
        d.size = o.optLong("size");
        d.ext = o.optString("ext");
        d.thumb = o.optString("photo_130");
        d.thumb_s = o.optString("photo_100");
        d.access_key = o.optString("access_key");
        return d;
    }
    
    public static ArrayList<VKDoc> parseDocs(JSONArray array) throws JSONException {
        ArrayList<VKDoc> docs = new ArrayList<>();
        if (array != null) {
            for(int i = 0; i<array.length(); ++i) {
                Object item=array.get(i);
                if(!(item instanceof JSONObject))
                    continue;
                JSONObject o = (JSONObject)item;
                VKDoc doc = VKDoc.parse(o);
                docs.add(doc);
            }
        }
        return docs;
    }

}
