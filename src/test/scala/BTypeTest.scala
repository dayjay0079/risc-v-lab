import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class BTypeTest extends AnyFlatSpec with ChiselScalatestTester {
  val B_Type = "b1100011".U     // Branch

  val testType = B_Type

  "Instruction test" should "pass" in {
    test(new ALU).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      //Branch ==
      dut.io.input.opcode.poke(testType)
      dut.io.input.funct3.poke("x0".U)
      dut.io.input.data1.poke(12.S)
      dut.io.input.data2.poke(12.S)
      dut.io.check.expect(1.B)

      //Branch !=
      dut.io.input.opcode.poke(testType)
      dut.io.input.funct3.poke("x1".U)
        //Equal
      dut.io.input.data1.poke(12.S)
      dut.io.input.data2.poke(12.S)
      dut.io.check.expect(0.B)
        //Not-Equal
      dut.io.input.data1.poke(12.S)
      dut.io.input.data2.poke(0.S)
      dut.io.check.expect(1.B)

      //Branch <
      dut.io.input.opcode.poke(testType)
      dut.io.input.funct3.poke("x4".U)
      dut.io.input.data1.poke(2.S)
      dut.io.input.data2.poke(12.S)
      dut.io.check.expect(1.B)

      // Branch >=
      dut.io.input.opcode.poke(testType)
      dut.io.input.funct3.poke("x5".U)
      dut.io.input.data1.poke(12.S)
      dut.io.input.data2.poke(2.S)
      dut.io.check.expect(1.B)

      //Branch < (Unsigned)
      dut.io.input.opcode.poke(testType)
      dut.io.input.funct3.poke("x6".U)
      dut.io.input.data1.poke(1.S)
      dut.io.input.data2.poke(-12.S)
      dut.io.check.expect(1.B)

      // Branch >= (Unsigned)
      dut.io.input.opcode.poke(testType)
      dut.io.input.funct3.poke("x7".U)
      dut.io.input.data1.poke(-12.S)
      dut.io.input.data2.poke(1.S)
      dut.io.check.expect(1.B)
    }
  }
}
