import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class MIPSsim {

    PrintWriter writer;

    private static final String REGISTERFILE = "registers.txt";
    private static final String DATAFILE = "datamemory.txt";
    private static final String INSTRUCTIONFILE = "instructions.txt";

    int stepNumber = 0;

    private List<InstructionToken> INM = new ArrayList<>();

    InstructionToken INB = null;
    InstructionToken AIB = null;
    InstructionToken SIB = null;
    InstructionToken PRB = null;
    Register ADB_reg = null;
    int ADB_value = 0;
    Register [] REB_reg = {null,null};
    int [] REB_value = {0,0};

    //private List<RegisterToken> RGF = new ArrayList<>();
    Map <Register,Integer> RGF = new HashMap<>();
    //private List<AddressToken> DAM = new ArrayList<>();
    Map <Integer,Integer> DAM = new HashMap<>();

    public static void main(String [] args){
        MIPSsim m = new MIPSsim();
        m.simulate();
        m.writer.close();
    }


    public MIPSsim(){
        try {
            writer = new PrintWriter("simulation.txt");
            List<String> temp = Files.readAllLines(Paths.get(REGISTERFILE));
            for (String t:temp) {
                String [] tokenAsString = t.replace("<","").replace(">","").split(",");
                RGF.put(Register.valueOf(tokenAsString[0]),Integer.valueOf(tokenAsString[1]));
                //RGF.add(new RegisterToken(t));
            }
            temp = Files.readAllLines(Paths.get(DATAFILE));
            for (String t:temp) {
                String [] tokenAsString = t.replace("<","").replace(">","").split(",");
                DAM.put(Integer.valueOf(tokenAsString[0]),Integer.valueOf(tokenAsString[1]));
                //DAM.add(new AddressToken(t));
            }
            temp = Files.readAllLines(Paths.get(INSTRUCTIONFILE));
            for (String t:temp) { INM.add(new InstructionToken(t)); }
        } catch (IOException e) {
            writer.println("Failed to parse files");
            e.printStackTrace();
            System.exit(0);
        }
        print();
    }

    public void simulate(){
        do {
            readAndDecode();
            print();
        } while(INB != null || AIB != null || SIB != null || PRB != null || ADB_reg != null ||
                REB_reg[0] != null || REB_reg[1] != null);
    }

    private void readAndDecode(){
        InstructionToken temp;
        if(INM.isEmpty()){
            temp = null;
        } else {
            temp = INM.remove(0);
            temp.data1 = RGF.get(Register.valueOf(temp.source1));
            if (!temp.isStore) {
                temp.data2 = RGF.get(Register.valueOf(temp.source2));
            }
        }
        issue();
        INB = temp;
        ++stepNumber;
    }

    private void issue(){ //no modification to token
        addr();
        mlu1();
        asu();
        if (INB == null) { // no token in INB
            SIB = null;
            AIB = null;
        } else if (INB.opcode == Instruction.ST){
            SIB = INB;
            AIB = null;
        } else { //ADD SUB or MUL
            SIB = null;
            AIB = INB;
        }
    }

    private void asu(){
        if(AIB != null && AIB.opcode == Instruction.ADD){
            REB_reg[1] = AIB.destination;
            REB_value[1] = AIB.data1 + AIB.data2;
        } else if (AIB != null && AIB.opcode == Instruction.SUB){
            REB_reg[1] = AIB.destination;
            REB_value[1] = AIB.data1 - AIB.data2;
        } else {
            REB_reg[1] = null;
            REB_value[1] = 0;
        }
    }

    private void mlu1(){
        InstructionToken temp = null;
        if (AIB != null && AIB.opcode == Instruction.MUL){
            temp = AIB;
        }
        mlu2();
        PRB = temp;
    }

    private void mlu2(){ // index 0 in REB []
        write(0);
        if (PRB != null){
            REB_reg[0] = PRB.destination;
            REB_value[0] = PRB.data1 * PRB.data2;
        } else {
            REB_reg[0] = null;
            REB_value[0] = 0;
        }
    }

    private void addr(){
        store();
        if(SIB != null){
            ADB_reg = SIB.destination;
            ADB_value = SIB.data1 + SIB.data2;
        } else {
            ADB_reg = null;
            ADB_value = 0;
        }
    }

    private void store(){
        if(ADB_reg != null){
            DAM.put(ADB_value,RGF.get(ADB_reg));
        }
    }

    private void write(int index){
        if(REB_reg[0] != null){
            RGF.put(REB_reg[0],REB_value[0]);
        } else if (REB_reg[1] != null){
            RGF.put(REB_reg[1],REB_value[1]);
        }
    }

    private void print(){
        writer.println("STEP " + stepNumber + ":");

        writer.print("INM:");
        int i ;
        String s = "";
        for (InstructionToken t:INM) {
            s += "<" + t.opcode + "," + t.destination + "," + t.source1 + "," + t.source2 + ">,";
        }
        if (s.length() > 0) {
            writer.println(s.substring(0, s.length() - 1));
        } else {
            writer.println();
        }

        writer.print("INB:");
        if (INB == null){ writer.println(); }
        else { writer.println(INB.toString()); }

        writer.print("AIB:");
        if (AIB == null){ writer.println(); }
        else { writer.println(AIB.toString()); }

        writer.print("SIB:");
        if (SIB == null){ writer.println(); }
        else { writer.println(SIB.toString()); }

        writer.print("PRB:");
        if (PRB == null){ writer.println(); }
        else { writer.println(PRB.toString()); }

        writer.print("ADB:");
        if (ADB_reg == null){ writer.println(); }
        else { writer.println("<" + ADB_reg + "," + ADB_value + ">"); }

        writer.print("REB:");
        if (REB_reg[0] != null && REB_reg[1] != null){
            writer.println("<" + REB_reg[0] + "," + REB_value[0] + ">,<" + REB_reg[1] + "," + REB_value[1] + ">");
        } else if (REB_reg[0] != null) {
            writer.println("<" + REB_reg[0] + "," + REB_value[0] + ">");
        } else if (REB_reg[1] != null) {
            writer.println("<" + REB_reg[1] + "," + REB_value[1] + ">");
        } else {
            writer.println();
        }

        writer.print("RGF:");
        s = "";
        for (Register r: Register.values()){
            Integer value = RGF.get(r);
            if (value != null){
                s += "<" + r + "," + value + ">,";
            }
        }
        if (s.length() > 0) {
            writer.println(s.substring(0, s.length() - 1)); // removes last comma
        } else {
            writer.println();
        }

        writer.print("DAM:");
        s = "";
        for (i = 0; i < 16; i++){
            Integer value = DAM.get(i);
            if (value != null){
                s += "<" + i + "," + value + ">,";
            }
        }
        if (s.length() > 0) {
            writer.println(s.substring(0, s.length() - 1)); // removes last comma
        } else {
            writer.println();
        }


        writer.println();
    }


}

enum Instruction {ADD, SUB, MUL, ST}
enum Register {R0, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15}

class InstructionToken{
    Instruction opcode;
    Register destination;
    String source1;
    int data1;
    String source2;
    int data2;
    boolean isStore = false;

    InstructionToken(String token){
        String temp [] = token.replace("<","").replace(">","").split(",");
        opcode = Instruction.valueOf(temp[0]);
        destination = Register.valueOf(temp[1]);
        source1 = temp[2];
        source2 = temp[3];
        if(opcode == Instruction.ST){
            isStore = true;
            data2 = Integer.parseInt(source2);
        }
    }

    public String toString(){
        return "<" + opcode + "," + destination + "," + data1 + "," + data2 + ">";
    }

}