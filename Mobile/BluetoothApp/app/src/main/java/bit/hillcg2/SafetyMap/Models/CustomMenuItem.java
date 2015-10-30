package bit.hillcg2.SafetyMap.Models;

//Class to have custom items for the menu as a listview in the main activity
public class CustomMenuItem {
    private String text;
    private int pictureResourceID;

    public CustomMenuItem(String text, int pictureResourceID){
        this.text = text;
        this.pictureResourceID = pictureResourceID;
    }

    public String getText(){
        return text;
    }

    public int getPictureResourceID(){
        return pictureResourceID;
    }

}
