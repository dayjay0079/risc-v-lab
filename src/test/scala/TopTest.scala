import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class TopTest extends AnyFlatSpec with ChiselScalatestTester {
  "Top test" should "pass" in {
    val FPGA = false
    val MEM_SIZE = 1024
    val FREQ = 50000000
    val BAUD = 9600
    test(new Top(FPGA, MEM_SIZE, FREQ, BAUD)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.step(10)
    }
  }
}
