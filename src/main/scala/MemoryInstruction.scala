import chisel3._
import chisel3.util._

class MemoryInstruction(program: Seq[Int]) extends Module {
  val io = IO(new Bundle {
    val pc = Input(UInt(32.W))
    val instruction = Output(UInt(32.W))
  })

  // Initialize the instruciton memory as a vector of wires
  val IM = WireDefault(VecInit(program.map(value => (value & 0xFFFFFFFFL).asUInt(32.W)))) // Type: Vec
  
  // Delay the output by 1 clock cycle
  io.instruction := RegNext(IM((io.pc >> 2.U).asUInt))
}
