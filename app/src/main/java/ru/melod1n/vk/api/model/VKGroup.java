package ru.melod1n.vk.api.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

import ru.melod1n.vk.api.VKApi;
import ru.melod1n.vk.api.model.attachment.VKLink;

public class VKGroup implements Serializable {
    
    private static final long serialVersionUID = 1L;

    public long gid;
    public String name;
    public String photo;//50*50
    public Boolean is_closed;
    public Boolean is_member;
    
    //это новые поля, которых у нас пока нет в базе
    //public String screen_name;
    public String photo_medium;//100*100
    public String photo_big;//200*200
    public String description;
    public String wiki_page;
    public Long fixed_post;
    public Boolean can_see_all_posts;//can_see_all_posts=false означает что стена закрыта
    public Boolean is_admin;
    public Integer admin_level;//1-moder, 2-editor, 3-admin
    public Integer members_count;
    public Integer type; //0 - group, 1 - page, 2 - event    
    public ArrayList<VKLink> links;

    public static VKGroup parse(JSONObject o) throws JSONException{
        VKGroup g=new VKGroup();
        g.gid = o.getLong("id");
        g.name = VKApi.unescape(o.getString("name"));
        g.photo = o.optString("photo_50");
        g.photo_medium = o.optString("photo_100");
        g.photo_big = o.optString("photo_200");
        String is_closed = o.optString("is_closed");
        g.is_closed = is_closed.equals("1");
        String is_member = o.optString("is_member");
        g.is_member = is_member.equals("1");
        g.description = VKApi.unescape(o.optString("description", null));
        g.wiki_page = VKApi.unescape(o.optString("wiki_page", null));
        
        //fixed post
        g.fixed_post=o.optLong("fixed_post", -1);//may be just false - boolean. If failed to parse long it means no post is fixed.
        if(g.fixed_post==-1)
            g.fixed_post=null;
        
        //это новые поля, которых у нас пока нет в базе
        //g.screen_name=o.optString("screen_name");

        if(o.has("can_see_all_posts"))
            g.can_see_all_posts=o.optInt("can_see_all_posts", 1)==1;
        
        //if doesn't exist it means value is unknown
        if(o.has("is_admin"))
            //opt because there may be something unparseable
            g.is_admin=o.optInt("is_admin", 0)==1;
        
        //if doesn't exist it means value is unknown
        if(o.has("admin_level"))
            //opt because there may be something unparseable
            g.admin_level=o.optInt("admin_level", 1);
        
        //if doesn't exist it means value is unknown
        if(o.has("members_count"))
            //opt because there may be something unparseable
            g.members_count=o.optInt("members_count", 0);
        if (o.has("type")) {
            String str_type = o.optString("type");
            if ("group".equals(str_type))
                g.type = 0;
            else if ("page".equals(str_type))
                g.type = 1;
            else if ("event".equals(str_type))
                g.type = 2;
        }
        
        JSONArray jlinks = o.optJSONArray("links");
        if (jlinks != null) {
            g.links = new ArrayList<VKLink>();
            for (int i = 0; i < jlinks.length(); i++) {
                JSONObject jlink = (JSONObject)jlinks.get(i);
                VKLink link = VKLink.parseFromGroup(jlink);
                if (link != null)
                    g.links.add(link);
            }
        }
        return g;
    }
    
    public static ArrayList<VKGroup> parseGroups(JSONArray jgroups) throws JSONException {
        ArrayList<VKGroup> groups=new ArrayList<VKGroup>();
        for(int i = 0; i < jgroups.length(); i++) {
            //для метода groups.get первый элемент - количество
            if(!(jgroups.get(i) instanceof JSONObject))
                continue;
            JSONObject jgroup = (JSONObject)jgroups.get(i);
            VKGroup group = VKGroup.parse(jgroup);
            groups.add(group);
        }
        return groups;
    }
}