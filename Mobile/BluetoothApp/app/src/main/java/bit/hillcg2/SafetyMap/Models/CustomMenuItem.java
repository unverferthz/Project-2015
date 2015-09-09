package bit.hillcg2.SafetyMap.Models;


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
