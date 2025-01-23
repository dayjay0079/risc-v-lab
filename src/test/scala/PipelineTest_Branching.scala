import chisel3._
import chiseltest._
import lib.ReadAssembly
import org.scalatest.flatspec.AnyFlatSpec

class PipelineTest_Branching extends AnyFlatSpec with ChiselScalatestTester {
  "Pipeline Branch Test" should "pass" in {
    val MEM_SIZE = 1024
    val FREQ = 50000000
    val BAUD = 9600
    val LED_CNT = 16
    val PROGRAM: Seq[Int] = ReadAssembly.readBin("assembly/branching.bin")
    test(new Top(PROGRAM, MEM_SIZE, FREQ, BAUD, LED_CNT)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.step(50)

      while(dut.io.pc.peekInt <= 0) { //200
        dut.clock.step()
      }

      while(dut.io.pc.peekInt <= 0) {
        //Initialization
        if(dut.io.pc.peekInt === 28) {
          println("addi x1, x1, 1")
          dut.io.regs(1).expect(1.S)
        }

        if(dut.io.pc.peekInt === 48) {
          println("addi x2, x0, 1")
          dut.io.regs(2).expect(1.S)
        }

        if(dut.io.pc.peekInt === 88) {
          println("addi x1, x1, 1")
          dut.io.regs(1).expect(2.S)
        }

        if(dut.io.pc.peekInt === 108) {
          println("addi x2, x0, 1")
          dut.io.regs(2).expect(1.S)
        }

        dut.clock.step(10)
      }
    }
  }
}
