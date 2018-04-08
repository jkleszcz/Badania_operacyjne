import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@XmlRootElement (name="ROOT")
public class Root {

    @XmlElement(name = "CHOICE")
    static List<Choice> choices = new ArrayList<>();

    @XmlElement(name="CRITERION")
    Criterion c;

    public void addChoises(String c){
        Root.choices.add(new Choice(c));
    }

    public void addCriterion(String c){
        Criterion criterion = new Criterion(c);
        criterion.setProperties();
        this.c = criterion;

    }

    public void write(String fileName) {
        try {
            JAXBContext jc = JAXBContext.newInstance(Root.class);
            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            FileWriter writer = new FileWriter(fileName);
            m.marshal(this, writer);
        } catch (JAXBException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void readPropertiesOfElements(){
        this.c.readProperties();
    }

    public static Root read(String fileName){
            try {
                JAXBContext jc = JAXBContext.newInstance(Root.class);
                Unmarshaller m = jc.createUnmarshaller();
                FileReader reader = new FileReader(fileName);
                return (Root) m.unmarshal(reader);
            } catch (JAXBException ex) {
                ex.printStackTrace();
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }
            return null;
        }

        public String toString(){
        StringBuilder b = new StringBuilder();
        b.append("CHOISES:\n");
        for(Choice c : Root.choices){
            b.append(c.toString());
        }

        b.append("CRITERION:\n");
            b.append(this.c.toString());
        return b.toString();
        }



    public static void main(String[] argv) {
        String command = "";
        Scanner s = new Scanner(System.in);
        Root root = null;
        while (true) {
            //MENU
            System.out.println("Please choose one of the following option:");
            System.out.println("(N) - to create new structure");
            System.out.println("(R) - to read existing structure");
            System.out.println("(C) - to count the eigenvectorRanking");
            System.out.println("(Q) - to exit");

            command = s.nextLine();

            if (command.equalsIgnoreCase("Q"))
                break;
            else if (command.equalsIgnoreCase("R")) {
                System.out.println("File?");
                root = Root.read(s.nextLine());
                root.readPropertiesOfElements();
                System.out.print(root);
            }
            else if(command.equalsIgnoreCase("C")){
                if(root != null) {
                    root.c.countRanking();
                    System.out.println("Eigenvector:");
                    for(int i = 0; i<root.c.eigenvectorRanking.length ; ++i){
                        System.out.println(root.c.eigenvectorRanking[i]);
                    }
                    System.out.println("Geometric mean:");
                    for(int i = 0; i<root.c.geometricRanking.length ; ++i){
                        System.out.println(root.c.geometricRanking[i]);
                    }
                }
            }
            else {
                root = new Root();
                int choises;
                System.out.println("How many choises");
                choises = s.nextInt();
                s.nextLine();
                for (int i = 0; i < choises; ++i) {
                    System.out.println("Choice " + i + ". Select it name: ");
                    String choiceName = s.nextLine();
                    root.addChoises(choiceName);
                }
                System.out.println("Add main criteria. Select it name: ");
                root.addCriterion(s.nextLine());
                root.write("data.xml");
            }

        }
    }

}
