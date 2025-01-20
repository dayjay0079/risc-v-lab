import chisel3._
import chiseltest._
import lib.ReadAssembly
import org.scalatest.flatspec.AnyFlatSpec

class GolTest extends AnyFlatSpec with ChiselScalatestTester {
  "Gol test" should "pass" in {
    val FPGA = true
    val MEM_SIZE = 1024
    val FREQ = 100000000
    val BAUD = 9600
    val LED_CNT = 16
    val PROGRAM: Seq[Int] = ReadAssembly.readBin("assembly/gol.bin")
    test(new Top(PROGRAM, FPGA, MEM_SIZE, FREQ, BAUD, LED_CNT)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(0)
      dut.io.switches.poke("x5555".U)
      dut.io.buttons.poke("b0101".U)
      dut.clock.step(1000)
      dut.io.buttons.poke("b0001".U)
      dut.clock.step(30000)
    }
  }
}
