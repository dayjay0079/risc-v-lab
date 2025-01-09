package lib;

import java.io.*;

public class ReadAssembly {
    public static int[] readBin(String path) {
        try {
            File file = new File(path);
            InputStream in = new DataInputStream(new FileInputStream(file));

            // Store file in byte array
            byte[] program =  in.readAllBytes();
            in.close();

            // Store binary instructions as integers
            int instructionCount = program.length / 4;
            int[] instructions = new int[instructionCount];
            for (int i = 0; i < instructionCount; i++) {
                int instruction = 0;
                for (int j = 0; j < 4; j++) {
                    instruction |= ((int)program[i*4 + j] & 0xff) << (j*8);
                }
                instructions[i] = instruction;
            }

            return instructions;
        } catch (Exception e) {
            System.out.println("File error");
            return new int[] {0};
        }
    }
}
