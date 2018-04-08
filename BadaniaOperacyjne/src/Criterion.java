import Jama.Matrix;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Criterion {

    @XmlTransient
    public Criterion parent;
    @XmlTransient
    public String path;
    @XmlTransient
    public double[][] valuesForMatrix;
    @XmlTransient
    public int matrixSize;
    @XmlTransient
    public Matrix matrix;

    @XmlElement(name="CRITERION")
    List<Criterion> subcrit = new ArrayList<>();

    @XmlAttribute
    String name;

    @XmlAttribute
    String m;

/////////////////////////// Constructors ////////////////////////////////////////
    Criterion(){}
    Criterion(String c){
        this.name = c;
    }


    //////////////////////// Read from XML file //////////////////////////////////
    public void readProperties(){

        if(this.parent == null)
            path = this.name;
        else
            path = this.parent.path + " -> " + this.name;

        for(Criterion c : this.subcrit) {
            c.parent = this;
            c.readProperties();
        }



        this.create2DArray();
        this.readMatrixFromM();
    }

    //Convert readed m attribute to 2D matrix
    public void readMatrixFromM(){
        String m[] = this.m.split(" ");
        this.matrixSize = (int)Math.sqrt(m.length);
        int i = 0;
        int j = 0;
        for(String e : m){
            if(e.isEmpty())
                continue;
            this.valuesForMatrix[i][j] = Double.parseDouble(e.replace(";",""));
            if(j == this.matrixSize-1){
                j = 0;
                i +=1;
            }else
                j++;
        }
    }


    ///////////////////////// Write to XML file /////////////////////////////////////
    public void setProperties(){
        Scanner scanner = new Scanner(System.in);

        if(this.parent == null){
            this.path = this.name;
        }else{
            this.path = parent.path + " -> " + this.name;
        }

        System.out.println("How many subcriterion does " + this.name + " have?");
        int subcriterion = scanner.nextInt();
        scanner.nextLine();
        for(int i=0 ; i<subcriterion ; ++i){
            System.out.println("Set name of new subcriterion of "+this.path + ":");
            Criterion sub = new Criterion(scanner.nextLine());
            sub.parent = this;
            sub.setProperties();
            this.subcrit.add(sub);
        }

        this.create2DArray();

        for(int i=0 ; i<matrixSize ; ++i){
            for(int j=i ; j<matrixSize ; ++j){
                this.setValues(i,j);
            }
        }

        this.setM();


    }

    public void create2DArray(){
        if(this.subcrit.isEmpty()){
            this.matrixSize = Root.choices.size();
            this.valuesForMatrix = new double[matrixSize][];
            for(int i=0 ; i<matrixSize ; ++i){
                this.valuesForMatrix[i] = new double[matrixSize];
            }
        }else{
            this.matrixSize = this.subcrit.size();
            this.valuesForMatrix = new double[this.matrixSize][];
            for(int i=0 ; i<this.matrixSize ; ++i) {
                this.valuesForMatrix[i] = new double[this.matrixSize];
            }
        }
    }

    public void setValues(int i, int j){
        if(i == j){
            this.valuesForMatrix[i][j] = 1;
        }else{
            System.out.println("Criterion: "+this.path);
            if(this.subcrit.isEmpty()){
                System.out.println(Root.choices.get(i).content + " vs." + Root.choices.get(j).content);
            }else{
                System.out.println(this.subcrit.get(i).name + " vs. "+ this.subcrit.get(j).name);
            }
            Scanner s = new Scanner(System.in);
            Double value = s.nextDouble();
            this.valuesForMatrix[i][j] = value;
            this.valuesForMatrix[j][i] = 1/value;
        }
    }

    public void setM(){
        StringBuilder b = new StringBuilder();
        for(int i=0 ; i<matrixSize ; ++i){
            for(int j=0 ; j<matrixSize ; ++j){
                b.append(this.valuesForMatrix[i][j]);
                if(j == matrixSize-1)
                    b.append(";");
                if(i == this.matrixSize-1 && j == this.matrixSize-1)
                    b.append("");
                else
                    b.append(" ");
            }
        }
        this.m = b.toString();
    }

    public String toString(){
        StringBuilder b = new StringBuilder();
        b.append(this.path);
        b.append("\n");
        for(int i=0 ; i<matrixSize ; ++i){
            for(int j=0 ; j<matrixSize ; ++j){
                b.append(this.valuesForMatrix[i][j]);
                b.append(" ");
            }
            b.append("\n");
        }
        b.append("\n");
        for(Criterion child : this.subcrit){
            b.append(child.toString());
        }
        return b.toString();
    }

    public Matrix createMatrix(){
        this.matrix = new Matrix(this.valuesForMatrix);
        return this.matrix;
    }


}
