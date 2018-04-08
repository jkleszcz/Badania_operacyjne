import javax.xml.bind.annotation.XmlValue;

public class Choice {

    @XmlValue
    String content;

    Choice(){

    }

    Choice(String c){
        this.content = c;
    }

    public String toString(){
        StringBuilder b = new StringBuilder();
        b.append(this.content);
        b.append("\n");
        return b.toString();
    }
}
