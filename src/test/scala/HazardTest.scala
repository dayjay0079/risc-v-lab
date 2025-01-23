import chisel3._
import chiseltest._
import lib.ReadAssembly
import org.scalatest.flatspec.AnyFlatSpec
class HazardTest extends AnyFlatSpec with ChiselScalatestTester {
  "Hazard Test" should "pass" in {
    val MEM_SIZE = 1024
    val FREQ = 50000000
    val BAUD = 9600
    val LED_CNT = 16
    val PROGRAM: Seq[Int] = ReadAssembly.readBin("assembly/Hz_test.bin")
    test(new Top(PROGRAM, MEM_SIZE, FREQ, BAUD, LED_CNT)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      val pc_max = 0x50
      while (dut.io.pc.peekInt <= pc_max) {
        dut.clock.step()
      }
      // dut.clock.step(50)
    }
  }
}

