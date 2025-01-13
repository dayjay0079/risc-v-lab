import chisel3._
import chiseltest._
import lib.ReadAssembly
import org.scalatest.flatspec.AnyFlatSpec

class MemoryTest extends AnyFlatSpec with ChiselScalatestTester {
  "Memory test" should "pass" in {
    val FPGA = false
    val MEM_SIZE = 1024
    val FREQ = 50000000
    val BAUD = 9600
    val PROGRAM: Seq[Int] = ReadAssembly.readBin("assembly/memTest.bin")
    test(new Top(PROGRAM, FPGA, MEM_SIZE, FREQ, BAUD)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.step(50)
    }
  }
}
