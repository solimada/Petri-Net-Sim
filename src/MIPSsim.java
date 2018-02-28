import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class MIPSsim {

    private static final String REGISTERFILE = "registers.txt";
    private static final String DATAFILE = "datamemory.txt";
    private static final String INSTRUCTIONFILE = "instructions.txt";

    private List<InstructionToken> INM = new ArrayList<InstructionToken>();
    InstructionToken INB;
    InstructionToken AIB;
    InstructionToken SIB;
    InstructionToken PRB;
    RegisterToken ADB;
    RegisterToken REB;
    private List<RegisterToken> RGF = new ArrayList<RegisterToken>();
    private List<AddressToken> DAM = new ArrayList<AddressToken>();




    public MIPSsim(){
        try {
            List<String> temp = Files.readAllLines(Paths.get(REGISTERFILE));
            for (String t:temp) { RGF.add(new RegisterToken(t)); }
            temp = Files.readAllLines(Paths.get(DATAFILE));
            for (String t:temp) { DAM.add(new AddressToken(t)); }
            temp = Files.readAllLines(Paths.get(INSTRUCTIONFILE));
            for (String t:temp) { INM.add(new InstructionToken(t)); }
        } catch (IOException e) {
            System.out.println("Failed to parse files");
            e.printStackTrace();
        }
    }

    public void readAndDecode(){

    }
    public void issue(){

    }
    public void asu(){

    }
    public void mult1(){

    }
    public void mult2(){

    }
    public void addr(){

    }
    public void store(){

    }
    public void write(){

    }
    public void print(){

    }
}

enum Instruction {ADD, SUB, MUL, ST}
enum Register {R0, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15}

class InstructionToken{
    Instruction opcode;
    Register destination;
    Register source1;
    String source2;
    boolean isStore = false;

    public InstructionToken(String token){
        String temp [] = token.replace("<","").replace(">","").split(",");
        opcode = Instruction.valueOf(temp[0]);
        destination = Register.valueOf(temp[1]);
        source1 = Register.valueOf(temp[2]);
        source2 = temp[3];
        if(opcode == Instruction.ST){
            isStore = true;
        }
    }

}

class RegisterToken{
    Register registerName;
    int value;
    public RegisterToken(String token){
        String temp [] = token.replace("<","").replace(">","").split(",");
        registerName = Register.valueOf(temp[0]);
        value = Integer.parseInt(temp[1]);
    }
}

class AddressToken{
    int address;
    int value;
    public AddressToken(String token){
        String temp [] = token.replace("<","").replace(">","").split(",");
        address = Integer.parseInt(temp[0]);
        value = Integer.parseInt(temp[1]);
    }
}

