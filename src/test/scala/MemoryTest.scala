import chisel3._
import chisel3.util._
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
      while(dut.io.regs(9).peekInt == 0) {
        dut.clock.step()
      }
      dut.io.regs(6).expect(0xFFFFFF86)
      dut.io.regs(7).expect(0x00000086)
      dut.io.regs(8).expect(2004)
      dut.io.regs(9).expect(12)
    }
  }
}
