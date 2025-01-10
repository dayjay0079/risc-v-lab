import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class ALUTest_All extends AnyFlatSpec with ChiselScalatestTester {
  val R_Type = "b0110011".U     // Arithmetic/Logic
  val I_Type_1 = "b0010011".U   // Arithmetic/Logic immediate
  val I_Type_2 = "b0000011".U   // Load
  val I_Type_3 = "b1100111".U   // jalr
  val S_Type = "b0100011".U     // Store
  val B_Type = "b1100011".U     // Branch

  var testType = R_Type

  "Instruction test" should "pass" in {
    test(new ALU_Control).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      //ADD
      dut.io.input.ctrl.opcode.poke(testType)
      dut.io.input.ctrl.funct3.poke("x0".U)
      dut.io.input.ctrl.funct7.poke("x00".U)
      dut.io.input.data1.poke(30891.S)
      dut.io.input.data2.poke(2653.S)
      dut.io.result.expect((30891+2653).S)

      //SUB
      dut.io.input.ctrl.opcode.poke(testType)
      dut.io.input.ctrl.funct3.poke("x0".U)
      dut.io.input.ctrl.funct7.poke("x20".U)
      dut.io.input.data1.poke(1025.S)
      dut.io.input.data2.poke(915.S)
      dut.io.result.expect((1025-915).S)

      //XOR
      dut.io.input.ctrl.opcode.poke(testType)
      dut.io.input.ctrl.funct3.poke("x4".U)
      dut.io.input.ctrl.funct7.poke("x00".U)
      dut.io.input.data1.poke(1025.S)
      dut.io.input.data2.poke(915.S)
      dut.io.result.expect((1025^915).S)

      //OR
      dut.io.input.ctrl.opcode.poke(testType)
      dut.io.input.ctrl.funct3.poke("x6".U)
      dut.io.input.ctrl.funct7.poke("x00".U)
      dut.io.input.data1.poke(1025.S)
      dut.io.input.data2.poke(915.S)
      dut.io.result.expect((1025|915).S)

      //AND
      dut.io.input.ctrl.opcode.poke(testType)
      dut.io.input.ctrl.funct3.poke("x7".U)
      dut.io.input.ctrl.funct7.poke("x00".U)
      dut.io.input.data1.poke(1025.S)
      dut.io.input.data2.poke(915.S)
      dut.io.result.expect((1025&915).S)

      //Shift Left Logical
      dut.io.input.ctrl.opcode.poke(testType)
      dut.io.input.ctrl.funct3.poke("x1".U)
      dut.io.input.ctrl.funct7.poke("x00".U)
      //Regular case (max shifted by 31)
      dut.io.input.data1.poke(12.S)
      dut.io.input.data2.poke(3.S)
      dut.io.result.expect(96.S)
      //Test for overflow (shifted by 32 and above)
      dut.io.input.data1.poke(1.S)
      dut.io.input.data2.poke(524288.S)
      dut.io.result.expect(0.S)

      //Shift Right Logical
      dut.io.input.ctrl.opcode.poke(testType)
      dut.io.input.ctrl.funct3.poke("x5".U)
      dut.io.input.ctrl.funct7.poke("x00".U)
      dut.io.input.data1.poke(12.S)
      dut.io.input.data2.poke(2.S)
      dut.io.result.expect(3.S) // 12 >> 2 = 3

      //Shift Right Logical (Arithmetic)
      dut.io.input.ctrl.opcode.poke(testType)
      dut.io.input.ctrl.funct3.poke("x5".U)
      dut.io.input.ctrl.funct7.poke("x20".U)
      dut.io.input.data1.poke(-24.S)
      dut.io.input.data2.poke(3.S)
      dut.io.result.expect(-3.S) // -24 >> 3 = -3

      //Set Less Than
      dut.io.input.ctrl.opcode.poke(testType)
      dut.io.input.ctrl.funct3.poke("x2".U)
      dut.io.input.ctrl.funct7.poke("x00".U)
      dut.io.input.data1.poke(-12.S)
      dut.io.input.data2.poke(3.S)
      dut.io.result.expect(1.S) // (data1 < data2)?1:0

      //Set Less Than (Unsigned)
      dut.io.input.ctrl.opcode.poke(testType)
      dut.io.input.ctrl.funct3.poke("x3".U)
      dut.io.input.ctrl.funct7.poke("x00".U)
      dut.io.input.data1.poke(-12.S)
      dut.io.input.data2.poke(3.S)
      dut.io.result.expect(0.S) // (|data1| < |data2|)?1:0

      testType = B_Type
      //Branch ==
      dut.io.input.ctrl.opcode.poke(testType)
      dut.io.input.ctrl.funct3.poke("x0".U)
      dut.io.input.data1.poke(12.S)
      dut.io.input.data2.poke(12.S)
      dut.io.check.expect(1.B)

      //Branch !=
      dut.io.input.ctrl.opcode.poke(testType)
      dut.io.input.ctrl.funct3.poke("x1".U)
      //Equal
      dut.io.input.data1.poke(12.S)
      dut.io.input.data2.poke(12.S)
      dut.io.check.expect(0.B)
      //Not-Equal
      dut.io.input.data1.poke(12.S)
      dut.io.input.data2.poke(0.S)
      dut.io.check.expect(1.B)

      //Branch <
      dut.io.input.ctrl.opcode.poke(testType)
      dut.io.input.ctrl.funct3.poke("x4".U)
      dut.io.input.data1.poke(2.S)
      dut.io.input.data2.poke(12.S)
      dut.io.check.expect(1.B)

      // Branch >=
      dut.io.input.ctrl.opcode.poke(testType)
      dut.io.input.ctrl.funct3.poke("x5".U)
      dut.io.input.data1.poke(12.S)
      dut.io.input.data2.poke(2.S)
      dut.io.check.expect(1.B)

      //Branch < (Unsigned)
      dut.io.input.ctrl.opcode.poke(testType)
      dut.io.input.ctrl.funct3.poke("x6".U)
      dut.io.input.data1.poke(1.S)
      dut.io.input.data2.poke(-12.S)
      dut.io.check.expect(1.B)

      // Branch >= (Unsigned)
      dut.io.input.ctrl.opcode.poke(testType)
      dut.io.input.ctrl.funct3.poke("x7".U)
      dut.io.input.data1.poke(-12.S)
      dut.io.input.data2.poke(1.S)
      dut.io.check.expect(1.B)

      testType = I_Type_1
      //ADD
      dut.io.input.ctrl.opcode.poke(testType)
      dut.io.input.ctrl.funct3.poke("x0".U)
      dut.io.input.data1.poke(30891.S)
      dut.io.input.imm.poke(2653.S)
      dut.io.result.expect((30891+2653).S)

      //XOR
      dut.io.input.ctrl.opcode.poke(testType)
      dut.io.input.ctrl.funct3.poke("x4".U)
      dut.io.input.data1.poke(1025.S)
      dut.io.input.imm.poke(915.S)
      dut.io.result.expect((1025^915).S)

      //OR
      dut.io.input.ctrl.opcode.poke(testType)
      dut.io.input.ctrl.funct3.poke("x6".U)
      dut.io.input.data1.poke(1025.S)
      dut.io.input.imm.poke(915.S)
      dut.io.result.expect((1025|915).S)

      //AND
      dut.io.input.ctrl.opcode.poke(testType)
      dut.io.input.ctrl.funct3.poke("x7".U)
      dut.io.input.data1.poke(1025.S)
      dut.io.input.imm.poke(915.S)
      dut.io.result.expect((1025&915).S)

      //Shift Left Logical
      dut.io.input.ctrl.opcode.poke(testType)
      dut.io.input.ctrl.funct3.poke("x1".U)
      //Regular case (max shifted by 31)
      dut.io.input.data1.poke(12.S)
      dut.io.input.imm.poke(3.S)
      dut.io.result.expect((12 << 3).S)
      //Test for overflow (shifted by 32 and above)
      dut.io.input.data1.poke(1.S)
      dut.io.input.imm.poke(64.S)
      dut.io.result.expect(0.S)

      //Shift Right Logical
      dut.io.input.ctrl.opcode.poke(testType)
      dut.io.input.ctrl.funct3.poke("x5".U)
      dut.io.input.data1.poke(12.S)
      dut.io.input.imm.poke(2.S)
      dut.io.result.expect(3.S) // 12 >> 2 = 3

      //Shift Right Logical (Arithmetic)
      dut.io.input.ctrl.opcode.poke(testType)
      dut.io.input.ctrl.funct3.poke("x5".U)
      dut.io.input.data1.poke(-24.S)
      dut.io.input.imm.poke((3 + 1024).S)
      dut.io.result.expect(-3.S) // -24 >> 3 = -3

      //Set Less Than
      dut.io.input.ctrl.opcode.poke(testType)
      dut.io.input.ctrl.funct3.poke("x2".U)
      dut.io.input.data1.poke(-12.S)
      dut.io.input.imm.poke(3.S)
      dut.io.result.expect(1.S) // (data1 < data2)?1:0

      //Set Less Than (Unsigned)
      dut.io.input.ctrl.opcode.poke(testType)
      dut.io.input.ctrl.funct3.poke("x3".U)
      dut.io.input.data1.poke(-12.S)
      dut.io.input.imm.poke(3.S)
      dut.io.result.expect(0.S) // (|data1| < |data2|)?1:0

      //Calculate index (All Load/Store Methods)
      dut.io.input.ctrl.opcode.poke(I_Type_2)
      dut.io.input.ctrl.funct3.poke("x0".U)
      dut.io.input.data1.poke(4096.S)
      dut.io.input.imm.poke(0.S)
      dut.io.result.expect((0 + 4096).S)
    }
  }
}
