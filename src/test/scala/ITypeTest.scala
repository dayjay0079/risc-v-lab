import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class ITypeTest extends AnyFlatSpec with ChiselScalatestTester {
  val I_Type_1 = "b0010011".U   // Arithmetic/Logic immediate
  val I_Type_2 = "b0000011".U   // Load
  val I_Type_3 = "b1100111".U   // jalr
  val S_Type = "b0100011".U     // Store

  val testType = I_Type_1

  "Instruction test" should "pass" in {
    test(new ALU_Control).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      //ADD
      dut.io.input.opcode.poke(testType)
      dut.io.input.funct3.poke("x0".U)
      dut.io.input.data1.poke(30891.S)
      dut.io.input.imm.poke(2653.S)
      dut.io.result.expect((30891+2653).S)

      //XOR
      dut.io.input.opcode.poke(testType)
      dut.io.input.funct3.poke("x4".U)
      dut.io.input.data1.poke(1025.S)
      dut.io.input.imm.poke(915.S)
      dut.io.result.expect((1025^915).S)

      //OR
      dut.io.input.opcode.poke(testType)
      dut.io.input.funct3.poke("x6".U)
      dut.io.input.data1.poke(1025.S)
      dut.io.input.imm.poke(915.S)
      dut.io.result.expect((1025|915).S)

      //AND
      dut.io.input.opcode.poke(testType)
      dut.io.input.funct3.poke("x7".U)
      dut.io.input.data1.poke(1025.S)
      dut.io.input.imm.poke(915.S)
      dut.io.result.expect((1025&915).S)

      //Shift Left Logical
      dut.io.input.opcode.poke(testType)
      dut.io.input.funct3.poke("x1".U)
      //Regular case (max shifted by 31)
      dut.io.input.data1.poke(12.S)
      dut.io.input.imm.poke(3.S)
      dut.io.result.expect((12 << 3).S)
      //Test for overflow (shifted by 32 and above)
      dut.io.input.data1.poke(1.S)
      dut.io.input.imm.poke(64.S)
      dut.io.result.expect(0.S)

      //Shift Right Logical
      dut.io.input.opcode.poke(testType)
      dut.io.input.funct3.poke("x5".U)
      dut.io.input.data1.poke(12.S)
      dut.io.input.imm.poke(2.S)
      dut.io.result.expect(3.S) // 12 >> 2 = 3

      //Shift Right Logical (Arithmetic)
      dut.io.input.opcode.poke(testType)
      dut.io.input.funct3.poke("x5".U)
      dut.io.input.data1.poke(-24.S)
      dut.io.input.imm.poke((3 + 1024).S)
      dut.io.result.expect(-3.S) // -24 >> 3 = -3

      //Set Less Than
      dut.io.input.opcode.poke(testType)
      dut.io.input.funct3.poke("x2".U)
      dut.io.input.data1.poke(-12.S)
      dut.io.input.imm.poke(3.S)
      dut.io.result.expect(1.S) // (data1 < data2)?1:0

      //Set Less Than (Unsigned)
      dut.io.input.opcode.poke(testType)
      dut.io.input.funct3.poke("x3".U)
      dut.io.input.data1.poke(-12.S)
      dut.io.input.imm.poke(3.S)
      dut.io.result.expect(0.S) // (|data1| < |data2|)?1:0

      //Calculate index (All Load/Store Methods)
      dut.io.input.opcode.poke(I_Type_2)
      dut.io.input.funct3.poke("x0".U)
      dut.io.input.data1.poke(4096.S)
      dut.io.input.imm.poke(0.S)
      dut.io.result.expect((0 + 4096).S)
    }
  }
}
