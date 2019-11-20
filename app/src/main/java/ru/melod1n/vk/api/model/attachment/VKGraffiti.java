package ru.melod1n.vk.api.model.attachment;

import org.json.JSONObject;

import java.io.Serializable;

public class VKGraffiti implements Serializable {
    private static final long serialVersionUID = 1L;
    public int id;
    public int owner_id;
    public String src; //200*100 http://cs10730.vkontakte.ru/u110317842/s_5a43e302.png
    public String src_big;//586*293 http://cs10730.vkontakte.ru/u110317842/l_f8bc298f.png

    public static VKGraffiti parse(JSONObject o) {
        VKGraffiti graffiti = new VKGraffiti();
        graffiti.id = o.optInt("id");
        graffiti.owner_id = o.optInt("owner_id");
        graffiti.src = o.optString("photo_200");
        graffiti.src_big = o.optString("photo_586");
        return graffiti;
    }
}
