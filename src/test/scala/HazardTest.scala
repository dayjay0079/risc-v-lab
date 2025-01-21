import chisel3._
import chiseltest._
import lib.ReadAssembly
import org.scalatest.flatspec.AnyFlatSpec
class HazardTest extends AnyFlatSpec with ChiselScalatestTester {
  "Hazard Test" should "pass" in {
    val FPGA = false
    val MEM_SIZE = 1024
    val FREQ = 50000000
    val BAUD = 9600
    val PROGRAM: Seq[Int] = ReadAssembly.readBin("assembly/Hz_test.bin")
    test(new Top(PROGRAM, FPGA, MEM_SIZE, FREQ, BAUD)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      val pc_max = 0x50
      while (dut.io.pc.peekInt <= pc_max) {
        dut.clock.step()
      }
      // dut.clock.step(50)
    }
  }
}

