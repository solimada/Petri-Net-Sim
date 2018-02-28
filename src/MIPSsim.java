import java.util.*;

public class MIPSsim {

    public static final String REGISTERFILE = "registers.txt";
    public static final String DATAFILE = "datamemory.txt";
    public static final String INSTRUCTIONFILE = "instructions.txt";

    private List<InstructionToken> instructionMemory = new ArrayList<InstructionToken>();
    private List<RegisterToken> registerFile = new ArrayList<RegisterToken>();
    private List<AddressToken> dataMemory = new ArrayList<AddressToken>();


    public MIPSsim(){

    }

    public void readAndDecode(){

    }
    public void Issue(){

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

