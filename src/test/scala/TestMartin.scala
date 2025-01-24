import chisel3._
import chiseltest._
import lib.ReadAssembly
import org.scalatest.flatspec.AnyFlatSpec

class TestMartin extends AnyFlatSpec with ChiselScalatestTester {
  "AddPos" should "pass" in {
    val PROGRAM_NAME = "addpos"
    val PROGRAM: Seq[Int] = ReadAssembly.readBin("tests/simple/" + PROGRAM_NAME + ".bin")
    val PROGRAM_LEN = PROGRAM.length
    val RESULT: Seq[Int] = ReadAssembly.readBin("tests/simple/" + PROGRAM_NAME + ".res")
    test(new Top(PROGRAM, 1024, 100000000, 9600, 16)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      while(dut.io.pc.peekInt != 0x10) {
        dut.clock.step()
      }
      dut.clock.step(5)
      for(i: Int <- 0 until 32) {
        dut.io.regs(i).expect(RESULT(i).S)
      }
    }
  }
}
