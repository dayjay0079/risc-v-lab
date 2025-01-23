import chisel3._
import chiseltest._
import lib.ReadAssembly
import org.scalatest.flatspec.AnyFlatSpec

class PipelineTest extends AnyFlatSpec with ChiselScalatestTester {
  "Full Pipeline Test" should "pass" in {
    val MEM_SIZE = 1024
    val FREQ = 50000000
    val BAUD = 9600
    val LED_CNT = 16
    val PROGRAM: Seq[Int] = ReadAssembly.readBin("assembly/loop_array.bin")
    test(new Top(PROGRAM, MEM_SIZE, FREQ, BAUD, LED_CNT)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      while(dut.io.regs(17).peekInt != 10) {
        dut.clock.step()
      }
      dut.io.regs(10).expect(31.S)
    }
  }
}
