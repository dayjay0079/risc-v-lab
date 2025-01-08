import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class InstructionTest extends AnyFlatSpec with ChiselScalatestTester {
  val R_INSTRUCTION: UInt = "b00000000000000001000000010110011".U(32.W) //add  x1, x1, x0

  val I_INSTRUCTION: UInt = "b00000000010100000000000010010011".U(32.W) //addi x1, x0, 5

  val S_INSTRUCTION: UInt = "b00000000000100010010000000100011".U(32.W) //sw   x1, 0(x2)

  val B_INSTRUCTION: UInt = "b00001000001000001001000001100011".U(32.W) //bne  x1, x2, 0x80

  val U_INSTRUCTION: UInt = "b00000000001111010000100110110111".U(32.W) //lui  x1, 976

  val J_INSTRUCTION: UInt = "b00100101000000000001000011101111".U(32.W) //jal  x1, 0x01250

  "Instruction test" should "pass" in {
    test(new AssignFields).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      // Testing R-Type Instruction
      dut.io.instruction.poke(R_INSTRUCTION)
      dut.io.output.opcode.expect("b0110011".U)
      dut.io.output.rd.expect(1.U)              // destination register x1
      dut.io.output.rs1.expect(1.U)             // addition 1 register x1
      dut.io.output.rs2.expect(0.U)             // addition 2 register x0
      dut.io.output.funct3.expect("x0".U)       // "ADD" function
      dut.io.output.funct7.expect("x00".U)

      // Testing I-Type Instruction
      dut.io.instruction.poke(I_INSTRUCTION)
      dut.io.output.opcode.expect("b0010011".U)
      dut.io.output.rd.expect(1.U)              // destination register x1
      dut.io.output.rs1.expect(0.U)             // addition register x0
      dut.io.output.funct3.expect("x0".U)       // "ADDI" function
      dut.io.output.imm.expect(5.S)             // addition value

      // Testing S-Type Instruction
      dut.io.instruction.poke(S_INSTRUCTION)
      dut.io.output.opcode.expect("b0100011".U)
      dut.io.output.rs1.expect(2.U)             // address register x2
      dut.io.output.rs2.expect(1.U)             // destination register x1
      dut.io.output.funct3.expect("x02".U)       // "Store Word" function
      dut.io.output.imm.expect(0.S)             // offset-value = 0


      // Testing B-Type Instruction
      dut.io.instruction.poke(B_INSTRUCTION)
      dut.io.output.opcode.expect("b1100011".U)
      dut.io.output.rs1.expect(1.U)             // source register x1
      dut.io.output.rs2.expect(2.U)             // source register x2
      dut.io.output.funct3.expect("x1".U)       // "Branch !=" function
      dut.io.output.imm.expect(128.S)           // Jump offset "x80"

      // Testing U-Type Instruction
      dut.io.instruction.poke(U_INSTRUCTION)
      dut.io.output.opcode.expect("b0110111".U)
      dut.io.output.rd.expect("b10011".U)       // destination register x1
      dut.io.output.imm.expect((976 << 12).S)   // imm = 976

      // Testing J-Type Instruction
      dut.io.instruction.poke(J_INSTRUCTION)
      dut.io.output.opcode.expect("b1101111".U)
      dut.io.output.rd.expect(1.U)              // destination register x1
      dut.io.output.imm.expect(4688.S)          // Jump offset "x1250"


    }
  }
}
