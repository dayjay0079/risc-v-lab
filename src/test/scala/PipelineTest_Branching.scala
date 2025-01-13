import chisel3._
import chiseltest._
import lib.ReadAssembly
import org.scalatest.flatspec.AnyFlatSpec

class PipelineTest_Branching extends AnyFlatSpec with ChiselScalatestTester {
  "Pipeline Test" should "pass" in {
    val FPGA = false
    val MEM_SIZE = 1024
    val FREQ = 50000000
    val BAUD = 9600
    val PROGRAM: Seq[Int] = ReadAssembly.readBin("assembly/branching.bin")
    test(new Top(PROGRAM, FPGA, MEM_SIZE, FREQ, BAUD)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      while(dut.io.pc.peekInt <= 128) {
        //Initialization
        if(dut.io.pc.peekInt === 28) {
          println("addi x1, x1, 1")
          dut.io.regs(1).expect(1.S)
        }

        if(dut.io.pc.peekInt === 48) {
          println("addi x2, x0, 1")
          dut.io.regs(2).expect(1.S)
        }

        if(dut.io.pc.peekInt === 68) {
          println("beq x2, x1, jump")
          dut.io.branch.expect(1.B)
        }

        if(dut.io.pc.peekInt === 88) {
          println("addi x1, x1, 1")
          dut.io.regs(1).expect(2.S)
        }

        if(dut.io.pc.peekInt === 108) {
          println("addi x2, x0, 1")
          dut.io.regs(2).expect(1.S)
        }

        if(dut.io.pc.peekInt === 128) {
          println("beq x2, x1, jump")
          dut.io.branch.expect(0.B)
        }



        dut.clock.step()
      }
    }
  }
}
