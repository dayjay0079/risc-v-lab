import chisel3._
import chiseltest._
import lib.ReadAssembly
import org.scalatest.flatspec.AnyFlatSpec

class PipelineTest_Algebraic extends AnyFlatSpec with ChiselScalatestTester {
  "Pipeline Algebraic Test" should "pass" in {
    val MEM_SIZE = 1024
    val FREQ = 50000000
    val BAUD = 9600
    val LED_CNT = 16
    val PROGRAM: Seq[Int] = ReadAssembly.readBin("assembly/algebraic.bin")
    test(new Top(PROGRAM, MEM_SIZE, FREQ, BAUD, LED_CNT)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      while(dut.io.pc.peekInt <= 408) {
        //Initialization
        if(dut.io.pc.peekInt === 28) {
          println("addi x1, x0, 12")
          dut.io.regs(1).expect(12.S)
        }

        //R-Type Test
        if(dut.io.pc.peekInt === 48) {
          println("add x1, x1, x1")
          dut.io.regs(1).expect(24.S)
        }
        if(dut.io.pc.peekInt === 68) {
          println("sub x2, x0, x1")
          dut.io.regs(2).expect(-24.S)
        }
        if(dut.io.pc.peekInt === 88) {
          println("xor x3, x2, x1")
          dut.io.regs(3).expect(-16.S)
        }
        if(dut.io.pc.peekInt === 108) {
          println("or  x4, x2, x1")
          dut.io.regs(4).expect(-8.S)
        }
        if(dut.io.pc.peekInt === 128) {
          println("and x5, x2, x1")
          dut.io.regs(5).expect(8.S)
        }
        if(dut.io.pc.peekInt === 148) {
          println("sll x6, x2, x5")
          dut.io.regs(6).expect(-6144.S)
        }
        if(dut.io.pc.peekInt === 168) {
          println("srl x7, x6, x5")
          dut.io.regs(7).expect(16777192.S)
        }
        if(dut.io.pc.peekInt === 188) {
          println("sra x8, x6, x5")
          dut.io.regs(8).expect(-24.S)
        }
        if(dut.io.pc.peekInt === 208) {
          println("slt x9, x4, x1")
          dut.io.regs(9).expect(1.S)
        }
        if(dut.io.pc.peekInt === 228) {
          println("sltu x10, x4, x1")
          dut.io.regs(10).expect(0.S)
        }

        //I-type Arithmetic Test
        if(dut.io.pc.peekInt === 248) {
          println("xori x11, x2, 24")
          dut.io.regs(11).expect(-16.S)
        }
        if(dut.io.pc.peekInt === 268) {
          println("ori  x12, x2, 24")
          dut.io.regs(12).expect(-8.S)
        }
        if(dut.io.pc.peekInt === 288) {
          println("andi x13, x2, 24")
          dut.io.regs(13).expect(8.S)
        }
        if(dut.io.pc.peekInt === 308) {
          println("slli x14, x2, 8")
          dut.io.regs(14).expect(-6144.S)
        }
        if(dut.io.pc.peekInt === 328) {
          println("srli x15, x6, 8")
          dut.io.regs(15).expect(16777192.S)
        }
        if(dut.io.pc.peekInt === 348) {
          println("srai x16, x6, 8")
          dut.io.regs(16).expect(-24.S)
        }
        if(dut.io.pc.peekInt === 368) {
          println("slti x17, x4, 24")
          dut.io.regs(17).expect(1.S)
        }
        if(dut.io.pc.peekInt === 388) {
          println("sltiu x18, x4, 24")
          dut.io.regs(18).expect(0.S)
        }
        if(dut.io.pc.peekInt === 408) {
          println("addi x3, x1, 10")
          dut.io.regs(3).expect(34.S)
        }

        dut.clock.step()
      }
    }
  }
}
