import Jama.Matrix;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Criterion {

    //Parent criterion of this object
    @XmlTransient
    public Criterion parent;

    //Path from root criterion to this object
    @XmlTransient
    public String path;

    //Values of comparisions using 2D array representation
    @XmlTransient
    public double[][] valuesForMatrix;

    //Comparisions matrix size
    @XmlTransient
    public int matrixSize;

    //Final comparisions matrix
    @XmlTransient
    public Matrix matrix;

    @XmlTransient
    public double[] eigenvector;

    @XmlTransient
    public double eigenvectorRanking[];

    @XmlTransient
    public double geomVector[];

    @XmlTransient
    public double geometricRanking[];

    //XML elements
    @XmlElement(name="CRITERION")
    List<Criterion> subcrit = new ArrayList<>();
    @XmlAttribute
    String name;
    @XmlAttribute
    String m;

    //Default constructor required to read XML file
    Criterion(){}
    Criterion(String c){
        this.name = c;
    }


    //Set properties after read XML file
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


    //Reading choises and criterion data from user
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

    //Create 2D array
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

    //Read comparision values form user
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

    //Create m attribute for XML file
    public void setM(){
        StringBuilder b = new StringBuilder();
        for(int i=0 ; i<matrixSize ; ++i){
            for(int j=0 ; j<matrixSize ; ++j){
                b.append(this.valuesForMatrix[i][j]);
                if(j == matrixSize-1 && i != matrixSize-1)
                    b.append(";");
                if(i == this.matrixSize-1 && j == this.matrixSize-1)
                    b.append("");
                else
                    b.append(" ");
            }
        }
        this.m = b.toString();
    }

    //Auxiliary function to control readed data
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

    //Matrix representation of comparision values
    public Matrix createMatrix(){
        this.matrix = new Matrix(this.valuesForMatrix);
        return this.matrix;
    }
    public void countRanking(){
        this.countEigenvector();
        this.countGeomVector();
        if(!this.subcrit.isEmpty()){
            for(Criterion c : this.subcrit){
                c.countRanking();
            }
            this.eigenvectorRanking = new double[Root.choices.size()];
            this.geometricRanking = new double[Root.choices.size()];
            for(int i = 0; i<this.eigenvectorRanking.length ; ++i){
                this.eigenvectorRanking[i] = 0;
                this.geometricRanking[i] = 0;
                for(int j=0 ; j<this.subcrit.size(); ++j){
                    this.eigenvectorRanking[i] += this.eigenvector[j] * this.subcrit.get(j).eigenvectorRanking[i];
                    this.geometricRanking[i] += this.geomVector[j] * this.subcrit.get(j).geometricRanking[i];
                }
            }
        }

    }

    public void countEigenvector(){
        this.createMatrix();
        double eigenvalues[] = this.matrix.eig().getRealEigenvalues();
        double maxValue = -1000;
        int maxValueIndex = -1;
        for(int i=0 ; i<eigenvalues.length ; ++i){
            if(eigenvalues[i]>maxValue){
                maxValueIndex = i;
                maxValue = eigenvalues[i];
            }
        }
        this.eigenvector = new double[this.matrix.getRowDimension()];
        double eigenSum = 0;
        for(int i=0 ; i<this.eigenvector.length ; ++i) {
            this.eigenvector[i] = this.matrix.eig().getV().get(i, maxValueIndex);
            eigenSum += this.eigenvector[i];
        }
        for(int i=0 ; i<this.eigenvector.length ; ++i){
            this.eigenvector[i] = this.eigenvector[i]/eigenSum;
        }

        if(this.subcrit.isEmpty()){
            this.eigenvectorRanking = this.eigenvector;
        }

    }

    public void countGeomVector(){
        this.geomVector = new double[this.matrix.getRowDimension()];
        double geomSum = 0;
        for(int i=0 ; i<this.matrix.getRowDimension() ; ++i){
            double multiRow = 1;
            for(int j=0 ; j<this.matrix.getColumnDimension() ; ++j){
                multiRow *= this.matrix.get(i,j);
            }
            this.geomVector[i] = Math.pow(multiRow,(1.0/this.matrix.getColumnDimension()));
            geomSum +=this.geomVector[i];
        }

        for(int i=0 ; i<this.geomVector.length ; ++i){
            this.geomVector[i] = this.geomVector[i]/geomSum;
        }

        if(this.subcrit.isEmpty())
            this.geometricRanking = this.geomVector;
    }


}
