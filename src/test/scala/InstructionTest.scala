  import chisel3._
  import chiseltest._
  import org.scalatest.flatspec.AnyFlatSpec

class InstructionTest extends AnyFlatSpec with ChiselScalatestTester {
  val R_INSTRUCTION = "b0110011".U(32.W)

  val I_INSTRUCTION = "b0010011".U(32.W)

  val S_INSTRUCTION = "b0100011".U(32.W)

  val B_INSTRUCTION = "b1100011".U()

  "Instruction test" should "pass" in {
    test(new AssignFields).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      // Testing R-Type Instruction
      dut.io.instruction.poke(R_INSTRUCTION)
      dut.io.output.opcode.expect("b0110011".U)
      dut.io.output.rs1.expect("b0".U)
      dut.io.output.rs2.expect("b0".U)
      dut.io.output.rd.expect("b0".U)
      dut.io.output.funct3.expect("b0".U)
      dut.io.output.funct3.expect("b0".U)
      dut.io.output.imm.expect("b0".U)

      // Testing I-Type Instruction


      // Testing S-Type Instruction


      // Testing B-Type Instruction


      // Testing U-Type Instruction


      // Testing J-Type Instruction
    }

  }
}
