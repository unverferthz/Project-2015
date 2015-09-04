package bit.hillcg2.SafetyMap;

import android.graphics.drawable.Drawable;

/**
 * Created by CamHill on 4/09/2015.
 */
public class CustomMenuItem {
    private String text;
    private Drawable picture;

    public CustomMenuItem(String text, Drawable picture){
        this.text = text;
        this.picture = picture;
    }

    public String getText(){
        return text;
    }

    public Drawable getPicture(){
        return picture;
    }

}
