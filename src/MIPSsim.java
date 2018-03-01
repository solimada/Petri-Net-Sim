import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class MIPSsim {

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




    public MIPSsim(){
        try {
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
            System.out.println("Failed to parse files");
            e.printStackTrace();
        }
    }

    public void simulate(){
        print(); // print step 0
        readAndDecode();
        print();
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
        asu();
        mlu1();
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
        write(1);
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
        if(REB_reg[index] != null){
            RGF.put(REB_reg[index],REB_value[index]);
        }
    }

    private void print(){
        System.out.println("STEP " + stepNumber + ":");

        System.out.print("INM:");
        int i = 0;
        for (InstructionToken t:INM) {
            if (i < 15) {
                System.out.print("<" + t.opcode + "," + t.destination + "," + t.source1 + "," + t.source2 + ">,");
            } else {
                System.out.print("<" + t.opcode + "," + t.destination + "," + t.source1 + "," + t.source2 + ">");
                break;
            }

            ++i;
        }

        System.out.print("INB:");
        if (INB == null){ System.out.println(); }
        else { System.out.println(INB.toString()); }

        System.out.print("AIB:");
        if (AIB == null){ System.out.println(); }
        else { System.out.println(AIB.toString()); }

        System.out.print("SIB:");
        if (SIB == null){ System.out.println(); }
        else { System.out.println(SIB.toString()); }

        System.out.print("PRB:");
        if (PRB == null){ System.out.println(); }
        else { System.out.println(PRB.toString()); }

        System.out.print("ADB:");
        if (ADB_reg == null){ System.out.println(); }
        else { System.out.println("<" + ADB_reg + "," + ADB_value + ">"); }

        System.out.print("REB:");
        if (REB_reg[0] != null && REB_reg[1] != null){
            System.out.println("<" + REB_reg[0] + "," + REB_reg[0] + ">,<" + REB_reg[1] + "," + REB_reg[1] + ">");
        } else if (REB_reg[0] != null) {
            System.out.println("<" + REB_reg[0] + "," + REB_reg[0] + ">");
        } else if (REB_reg[1] != null) {
            System.out.println("<" + REB_reg[1] + "," + REB_reg[1] + ">");
        } else {
            System.out.println();
        }

        System.out.print("RGF:");
        String s = "";
        for (Register r: Register.values()){
            Integer value = RGF.get(r);
            if (value != null){
                s += "<" + r + "," + value + ">,";
            }
        }
        System.out.println(s.substring(0,s.length()-1)); // removes last comma

        System.out.print("DAM:");
        s = "";
        for (i = 0; i < 16; i++){
            Integer value = DAM.get(i);
            if (value != null){
                s += "<" + i + "," + value + ">,";
            }
        }
        System.out.println(s.substring(0,s.length()-1)); // removes last comma

        System.out.println();
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
        return "<" + opcode + "," + destination + "," + source1 + "," + source2 + ">";
    }

}